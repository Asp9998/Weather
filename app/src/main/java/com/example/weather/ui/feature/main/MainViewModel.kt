package com.example.weather.ui.feature.main

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.core.async.DispatcherProvider
import com.example.weather.core.util.DataOrException
import com.example.weather.data.local.dataStore.LastLocationDataStore
import com.example.weather.data.remote.dto.onecall.Weather
import com.example.weather.domain.repository.LocationRepo
import com.example.weather.domain.repository.SnapshotRepository
import com.example.weather.domain.repository.WeatherDbRepository
import com.example.weather.presentation.WeatherVisualSpec
import com.example.weather.presentation.mapWeatherToSpec
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

/**
 * Offline-first VM:
 *  - Always seeds UI from last cached snapshot (instant render)
 *  - Then silently revalidates in background (SWR via SnapshotRepository)
 *  - Persists "last location" whenever a snapshot is displayed
 *
 * Public API surface kept small & predictable:
 *  - bindFromLastLocationOrCurrent(unit)
 *  - observeCurrentLocation(unit)
 *  - observeLocation(lat, lon, unit, labelIfKnown)
 *  - refresh(force)
 *  - refreshCityName()  (optional; for title polish when live location used)
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val weatherDbRepository: WeatherDbRepository,
    private val locationRepo: LocationRepo,
    private val snapShotRepo: SnapshotRepository,
    private val lastStore: LastLocationDataStore,
    private val dp: DispatcherProvider
) : ViewModel() {



    // background
    private val _visualSpec = MutableStateFlow<WeatherVisualSpec?>(null)
    val visualSpec: StateFlow<WeatherVisualSpec?> = _visualSpec

    private fun currentReduceMotion(): Boolean {
        // power saver → reduce motion (or read from settings)
        // quick check; move to a Util as needed
        return false
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UI State
    // ─────────────────────────────────────────────────────────────────────────────


    private val _locked = MutableStateFlow(false)
    fun lockToSelection(lock: Boolean) { _locked.value = lock }

    private val _cityName = MutableStateFlow<String?>(null)
    val cityName: StateFlow<String?> = _cityName

    var ui by mutableStateOf(DataOrException<Weather, Boolean, Exception>(loading = false))
        private set

    // ─────────────────────────────────────────────────────────────────────────────
    // Session state for current binding
    // ─────────────────────────────────────────────────────────────────────────────

    private var observeJob: Job? = null
    private var currentLocationId: String? = null
    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private var currentUnit: String = "metric"

    // ─────────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * App cold start: render instantly from disk if possible,
     * otherwise attempt live location; if that fails and no cache exists,
     * fall back to a known default (Toronto).
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun bindFromLastLocationOrCurrent(unitFallback: String = currentUnit) = viewModelScope.launch(dp.io) {
        currentUnit = unitFallback
        val last = lastStore.readOnce()
        Log.d("DATAWEATHER", "MainScreen: last : $last")


        if (last != null) {
            withContext(dp.main) {
                observeLocation(
                    lat = last.lat,
                    lon = last.lon,
                    unit = last.unit.ifBlank { unitFallback },
                    labelIfKnown = last.cityLabel
                )
            }
            return@launch
        }

        // No "last" persisted → try current location; it will prefer latest cached row if GPS is null.
        withContext(dp.main) {
            observeCurrentLocation(unitFallback)
        }
    }

    /**
     * Bind to live location if available. If GPS is unavailable:
     *  - Prefer latest cached snapshot row (whatever user saw last)
     *  - Otherwise fall back to Toronto
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun observeCurrentLocation(unit: String = currentUnit) = viewModelScope.launch(dp.io) {
        if (_locked.value) return@launch
        currentUnit = unit

        // Try quick fix (doesn't throw if permission is missing)
        val gp = locationRepo.tryGetQuickLocationNoThrow()
        Log.d("DATAWEATHER", "observeCurrentLocation: $gp")

        if (gp == null) {
            // Prefer the latest cached snapshot for this unit (fast, offline)
            val latest = snapShotRepo.getLatestSnapshotRowForUnit(unit)
            if (latest != null) {
                withContext(dp.main) {
                    observeLocation(
                        lat = latest.cityLat,
                        lon = latest.cityLon,
                        unit = unit,
                        labelIfKnown = latest.cityLabel
                    )
                }
                return@launch
            }

            // Absolute fallback only when there's truly no cache
            withContext(dp.main) {
                _cityName.value = "Toronto, CA"
                observeLocation(43.6532, -79.3832, unit, labelIfKnown = "Toronto, CA")
            }
            return@launch
        }

        // Got live coords → compute a friendly label (non-blocking if geocoder missing)
        val place = locationRepo.reverseGeocodeCity(gp.lat, gp.lon)
        val label = place?.city ?: "Current location"

        withContext(dp.main) {

            observeLocation(gp.lat, gp.lon, unit, labelIfKnown = label)
        }
    }

    /**
     * Bind to a specific place (favorites or search).
     * Seeds UI from cache (no spinner if exists), observes updates, and revalidates silently.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun observeLocation(
        lat: Double,
        lon: Double,
        unit: String = currentUnit,
        labelIfKnown: String? = null
    ) {
        currentUnit = unit
        currentLat = lat
        currentLon = lon

        val locationID = locationKey(lat, lon)
        currentLocationId = locationID

        // VM: before reading cache

        // 1) Seed from cache (with meta). Never show "loading" over existing content.
        viewModelScope.launch(dp.io) {
            val cached = snapShotRepo.getWeatherOnceWithMeta(locationID, unit)
            Log.d("DATAWEATHER", "observeLocation: ${cached?.meta?.lat}, ${cached?.meta?.lon}")
            if (cached != null) {
                withContext(dp.main) {
                    _cityName.value = cached.meta.cityLabel
                    // VM: before reading cache

                    setData(cached.weather)
                }
                // Persist "last location" only after we actually show it
                lastStore.save(locationID, unit, cached.meta.cityLabel, cached.meta.lat, cached.meta.lon)
                Log.d("DATAWEATHER", "observeLocation: $locationID, ${cached.meta.lat}, ${cached.meta.lon} }")
            } else if (ui.data == null) {
                withContext(dp.main) { setLoading() }
            }
        }



        // 2) Observe updates (non-null) and persist last location on each emission.
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            snapShotRepo.observeWeatherWithMeta(locationID, unit).collect { wm ->
                if (wm != null) {
                    _cityName.value = wm.meta.cityLabel
                    setData(wm.weather)
                    viewModelScope.launch(dp.io) {
                        lastStore.save(locationID, unit, wm.meta.cityLabel, wm.meta.lat, wm.meta.lon)
                    }
                }
            }
        }

        // 3) Silent revalidation (SWR). Pass a label on first write so the snapshot stores it.
        viewModelScope.launch(dp.io) {
            try {
                snapShotRepo.refreshIfNeeded(
                    locationId = locationID,
                    unit = unit,
                    lat = lat,
                    lon = lon,
                    cityLabel = labelIfKnown,
                    cityLat = lat,
                    cityLon = lon
                )
            } catch (e: Exception) {
                if (ui.data == null) setError(e)
            }
        }

        warmFavoritesInBackground(unit)
    }

    /**
     * Manual refresh (pull-to-refresh or unit change).
     * Uses the current bound location and forces a network revalidation.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun refresh(force: Boolean = false) = viewModelScope.launch(dp.io) {
        val locId = currentLocationId ?: return@launch
        val lat = currentLat ?: return@launch
        val lon = currentLon ?: return@launch
        try {
            snapShotRepo.refreshIfNeeded(
                locationId = locId,
                unit = currentUnit,
                lat = lat,
                lon = lon,
                force = force,
                cityLabel = _cityName.value,
                cityLat = lat,
                cityLon = lon
            )
        } catch (e: Exception) {
            if (ui.data == null) setError(e)
        }
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun warmFavoritesInBackground(unit: String) = viewModelScope.launch(dp.io) {
        val favs = weatherDbRepository.observeAll().first()          // or pinned + recent subset
        snapShotRepo.refreshFavoritesIfAllowed(favs, unit, maxParallel = 3)
    }


    /**
     * Upsert a favorite and refresh its snapshot in background (no UI block).
     * Does NOT alter live-location logic; purely a saved-city action.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun addFavorite(
        lat: Double,
        lon: Double,
        label: String,
        unit: String
    ) = viewModelScope.launch(dp.io) {
        val id = locationKey(lat, lon)
        val now = System.currentTimeMillis()

        // 2) Kick off snapshot refresh silently (don’t await)
        launch(dp.io) {
            try {
                snapShotRepo.refreshIfNeeded(
                    locationId = id,
                    unit       = unit,
                    lat        = lat,
                    lon        = lon,
                    cityLabel  = label,
                    cityLat    = lat,
                    cityLon    = lon,
                    force      = true
                )
            } catch (_: Exception) {
                // keep silent; UI will still show cache when available
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────────

    private fun locationKey(lat: Double, lon: Double): String =
        String.Companion.format(Locale.US, "%.4f,%.4f", lat, lon)

    private fun setData(data: Weather) {
        ui = DataOrException(data = data, loading = false)
        _visualSpec.value = mapWeatherToSpec(data, reduceMotion = currentReduceMotion())
    }

    private fun setLoading() {
        // Only used when we truly have nothing to show
        ui = DataOrException(loading = true)
    }

    private fun setError(e: Exception) {
        ui = DataOrException(loading = false, e = e)
    }


}