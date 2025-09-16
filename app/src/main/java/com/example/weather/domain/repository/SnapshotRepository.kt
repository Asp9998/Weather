package com.example.weather.domain.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.annotation.RequiresPermission
import com.example.weather.core.async.DispatcherProvider
import com.example.weather.data.local.db.dao.SnapshotDao
import com.example.weather.data.local.db.entity.SnapshotEntity
import com.example.weather.data.remote.WeatherService
import com.example.weather.data.local.db.entity.buildSnapshotId
import com.example.weather.data.local.db.entity.FavouriteEntity
import com.example.weather.data.remote.dto.onecall.Weather
import com.example.weather.domain.model.RefreshResult
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.Semaphore
import javax.inject.Inject

/**
 * Offline-first snapshot cache around OneCall:
 *  - UI reads from Room instantly (Weather + meta)
 *  - Silent revalidation (SWR) updates the row in background
 *  - Raw JSON is stored in a BLOB to keep UI models decoupled from DB schema
 *
 * Only the minimal surface needed by MainViewModel is kept here:
 *  - observeWeatherWithMeta(...)
 *  - getWeatherOnceWithMeta(...)
 *  - refreshIfNeeded(...)
 *  - getLatestSnapshotRowForUnit(...)
 */
class SnapshotRepository @Inject constructor(
    private val dao: SnapshotDao,
    private val weatherService: WeatherService,
    private val dp: DispatcherProvider,
    @ApplicationContext private val context: Context,
    moshi: Moshi
) {

    private val adapter = moshi.adapter(Weather::class.java)

    companion object {
        /** Whole-snapshot TTL used by SWR (15 minutes). */
        const val DEFAULT_TTL_MS: Long = 15 * 60 * 1000L
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Public API (used by ViewModel)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Observe decoded weather + metadata for a (locationId, unit).
     * - Emits immediately with cached row (if any), then on every upsert/refresh.
     * - Returns null if row is missing or decode fails (we keep the flow alive).
     */
    fun observeWeatherWithMeta(locationId: String, unit: String): Flow<WeatherWithMeta?> =
        dao.observeSnapshot(locationId, unit).map { e ->
            if (e == null) null else {
                val w = decodeOrNull(e) ?: return@map null
                WeatherWithMeta(
                    weather = w,
                    meta = WeatherMeta(
                        cityLabel   = e.cityLabel,
                        lat         = e.cityLat,
                        lon         = e.cityLon,
                        asOfEpoch   = e.asOfEpoch,
                        fetchedAtMs = e.fetchedAtMs
                    )
                )
            }
        }

    /**
     * One-shot fetch of cached weather + meta for a (locationId, unit).
     * - Returns null if no row or decode fails.
     */
    suspend fun getWeatherOnceWithMeta(locationId: String, unit: String): WeatherWithMeta? {
        val e = dao.getSnapshot(locationId, unit) ?: return null
        val w = decodeOrNull(e) ?: return null
        return WeatherWithMeta(
            weather = w,
            meta = WeatherMeta(
                cityLabel   = e.cityLabel,
                lat         = e.cityLat,
                lon         = e.cityLon,
                asOfEpoch   = e.asOfEpoch,
                fetchedAtMs = e.fetchedAtMs
            )
        )
    }

    /**
     * SWR refresh:
     *  - If missing/stale/forced: call OneCall with validators (ETag/Last-Modified)
     *  - 304 Not Modified → bump freshness (touch)
     *  - 200 OK → upsert new snapshot (including city label/coords if provided)
     *  - Fresh rows → Skip network
     *
     * @param cityLabel Optional human label (first write). Falls back to existing, then locationId.
     * @param cityLat/Lon Optional coords to persist (defaults to provided lat/lon or existing).
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun refreshIfNeeded(
        locationId: String,
        unit: String,
        lat: Double,
        lon: Double,
        ttlMs: Long = DEFAULT_TTL_MS,
        cityLabel: String? = null,
        cityLat: Double? = null,
        cityLon: Double? = null,
        force: Boolean = false
    ): RefreshResult = withContext(dp.io) {
        val now = System.currentTimeMillis()
        val existing = dao.getSnapshot(locationId, unit)

        val shouldRefresh = force || existing == null || existing.isExpired(now)
        if (!shouldRefresh) return@withContext RefreshResult.SkippedFresh

        val resp = weatherService.oneCall(
            lat = lat,
            lon = lon,
            units = unit,
            etag = existing?.eTag,
            lastModified = existing?.lastModified
        )

        // 304 → keep blob, bump freshness
        if (resp.code() == 304 && existing != null) {
            val newExpires = now + ttlMs
            dao.touchFreshness(
                fetchedAtMs = now,
                expiresAtMs = newExpires,
                locationId = locationId,
                unit = unit
            )
            return@withContext RefreshResult.NotModified
        }

        // 200 → upsert new blob
        if (resp.isSuccessful) {
            val body = resp.body() ?: throw IllegalStateException("Empty Body")
            val bytes = adapter.toJson(body).toByteArray(Charsets.UTF_8)

            val asOf        = (body.current?.dt ?: 0).toLong() // API epoch seconds
            val eTag        = resp.headers()["ETag"]
            val lastMod     = resp.headers()["Last-Modified"]

            // Persist a stable, human-friendly label on first save if you know it.
            val finalLabel  = cityLabel ?: existing?.cityLabel ?: locationId
            val finalLat    = cityLat ?: existing?.cityLat ?: lat
            val finalLon    = cityLon ?: existing?.cityLon ?: lon

            val snapshot = SnapshotEntity(
                snapshotId   = buildSnapshotId(locationId, unit),
                locationId   = locationId,
                unit         = unit,
                asOfEpoch    = asOf,
                fetchedAtMs  = now,
                ttlMs        = ttlMs,
                expiresAtMs  = now + ttlMs,
                eTag         = eTag,
                lastModified = lastMod,
                cityLabel    = finalLabel,
                cityLat      = finalLat,
                cityLon      = finalLon,
                payloadBlob  = bytes,
                payloadVersion = 1
            )
            dao.upsertSnapshot(snapshot)
            return@withContext RefreshResult.Updated
        }

        // Non-2xx and not 304
        throw HttpException(resp)
    }

    /**
     * Helper for offline startup when GPS is unavailable:
     * returns the most recent snapshot row for a unit (no decode).
     */
    suspend fun getLatestSnapshotRowForUnit(unit: String): SnapshotEntity? =
        dao.getLatestForUnit(unit)


    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun refreshFavoritesIfAllowed(
        favorites: List<FavouriteEntity>,
        unit: String,
        maxParallel: Int = 3,
        force: Boolean = false
    ) = withContext(dp.io)  {
        // Guard: only run if conditions are good
        if (!isNetworkConnected(context) || !isBatteryOkay(context)) return@withContext

        val now = System.currentTimeMillis()

        // Limit concurrency to avoid bursts / rate limits
        val sem = Semaphore(maxParallel)

        coroutineScope {
            favorites.forEach { fav ->
                launch {
                    sem.acquire()
                    try {
                        val locId = fav.locationId
                        val existing = dao.getSnapshot(locId, unit)
                        val needs = force || existing == null || existing.isExpired(now)
                        if (!needs) return@launch

                        runCatching {
                            refreshIfNeeded(
                                locationId = locId,
                                unit = unit,
                                lat = fav.cityLat,
                                lon = fav.cityLon,
                                cityLabel = fav.cityLabel,
                                cityLat = fav.cityLat,
                                cityLon = fav.cityLon
                            )
                        }.onFailure {
                            // swallow: this is best-effort background hygiene
                        }
                    } finally {
                        sem.release()
                    }
                }
            }
        }

    }




    // ─────────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────────

    /** Safe JSON→DTO decode (never throws; logs and returns null on failure). */
    private suspend fun decodeOrNull(entity: SnapshotEntity): Weather? =
        withContext(dp.default) {    // CPU
            try {
                adapter.fromJson(entity.payloadBlob.toString(Charsets.UTF_8))
            } catch (t: Throwable) {
                Log.e("SNAPSHOT", "decode failed for ${entity.snapshotId}", t)
                null
            }
        }
}


// --- helpers ---

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
private fun isNetworkConnected(ctx: Context): Boolean {
    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
}

private fun isBatteryOkay(ctx: Context): Boolean {
    // Conservative: skip if power-save mode OR battery < ~15%
    val pm = ctx.getSystemService(Context.POWER_SERVICE)
            as PowerManager
    if (pm.isPowerSaveMode) return false

    val bm = ctx.getSystemService(Context.BATTERY_SERVICE)
            as BatteryManager
    // Some OEMs don’t report accurate % here; fall back to sticky intent if needed
    val pctFromManager = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    if (pctFromManager in 1..100) return pctFromManager >= 15

    // Fallback (sticky broadcast)
    val i = ctx.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    ) ?: return true
    val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val pct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()) else 100f
    return pct >= 15f
}

/** Small meta bundle we expose next to Weather for UI needs (title, coords, freshness). */
data class WeatherMeta(
    val cityLabel: String,
    val lat: Double,
    val lon: Double,
    val asOfEpoch: Long,
    val fetchedAtMs: Long
)

data class WeatherWithMeta(
    val weather: Weather,
    val meta: WeatherMeta
)
