package com.example.weather.domain.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.example.weather.core.async.DispatcherProvider
import com.example.weather.data.remote.dto.live.GeoPoint
import com.example.weather.data.remote.dto.live.PlaceParts
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * LocationRepo
 *
 * - Fast path location:
 *   1) Use last known location if it's fresh (instant).
 *   2) Otherwise request a single current fix with a short timeout.
 *
 * - Reverse geocoding to city/region/country using Android Geocoder.
 *
 * Notes:
 * - This class uses the **application context** (no Activity context) to avoid leaks.
 * - No UI/Compose code here—keep repository platform-agnostic.
 */
class LocationRepo @Inject constructor(
    @ApplicationContext private val appContext: Context,
    // If you already provide these via Hilt @Provides, remove the defaults and inject them.
    private val fused: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext),
    private val settings: SettingsClient =
        LocationServices.getSettingsClient(appContext),
    private val dp: DispatcherProvider
) {
    private companion object {
        const val LAST_KNOWN_MAX_AGE_MIN = 30       // accept cached fix up to 30 min old
        const val QUICK_TIMEOUT_MS = 1_500L         // snappy window for current fix
        const val FALLBACK_TIMEOUT_MS = 5_000L      // second chance window
    }

    /**
     * Returns a quick location or null if unavailable within our windows.
     *
     * Requires either COARSE or FINE at runtime.
     */
    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    ])
    suspend fun getQuickLocation(): GeoPoint? {
        // 1) Try last known (instant when device has a recent fix)
        val last: Location? = fused.lastLocation.await()
        if (last?.isFresh(LAST_KNOWN_MAX_AGE_MIN) == true) {
            return GeoPoint(last.latitude, last.longitude)
        }

        // 2) Ensure device location settings are OK (GPS/network enabled).
        // If this throws a ResolvableApiException, show resolution in UI layer.
        runCatching {
            val req = LocationSettingsRequest.Builder()
                .addLocationRequest(
                    LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 0).build()
                )
                .build()
            settings.checkLocationSettings(req).await()
        }

        // 3) Request a single fresh fix. Balanced is fine for weather (uses Wi-Fi/cell if coarse).
        val currentReq = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()
        val token = CancellationTokenSource()

        val fresh = withTimeoutOrNull(QUICK_TIMEOUT_MS) {
            fused.getCurrentLocation(currentReq, token.token).await()
        } ?: withTimeoutOrNull(FALLBACK_TIMEOUT_MS) {
            fused.getCurrentLocation(currentReq, token.token).await()
        }

        return fresh?.let { GeoPoint(it.latitude, it.longitude) }
    }

    /**
     * Safe wrapper that never throws if permission is missing.
     * Returns null on: no permission, SecurityException, settings off, or timeout.
     */
    suspend fun tryGetQuickLocationNoThrow(): GeoPoint? {
        if (!hasAnyLocationPermission()) return null
        return try { getQuickLocation() } catch (_: SecurityException) { null }
    }

    /**
     * Reverse geocode lat/lon into city/region/country/fullAddress.
     * Runs on IO dispatcher and returns null if Geocoder backend is unavailable or fails.
     */
    suspend fun reverseGeocodeCity(
        lat: Double,
        lon: Double,
        locale: Locale = Locale.getDefault()
    ): PlaceParts? = withContext(dp.io) {
        if (!Geocoder.isPresent()) return@withContext null
        val geocoder = Geocoder(appContext, locale)
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(
                        lat, lon, 1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                val a = addresses.firstOrNull()
                                cont.resume(a?.toPlaceParts())
                            }

                            override fun onError(errorMessage: String?) {
                                cont.resume(null)
                            }
                        }
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val a = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                a?.toPlaceParts()
            }
        } catch (_: IOException) {
            null
        }
    }

    // --- Helpers ---

    private fun hasAnyLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun Location.isFresh(minutes: Int): Boolean {
        val ageNanos = SystemClock.elapsedRealtimeNanos() - this.elapsedRealtimeNanos
        val maxAge = minutes * 60L * 1_000_000_000
        return ageNanos in 0..maxAge
    }

    private fun Address.toPlaceParts() = PlaceParts(
        city = locality ?: subAdminArea ?: subLocality,
        region = adminArea ?: subAdminArea,
        country = countryName,
        fullAddress = getAddressLine(0)
    )
}