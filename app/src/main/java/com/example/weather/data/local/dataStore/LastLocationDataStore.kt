package com.example.weather.data.local.dataStore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

private val Context.lastLocationDataStore by preferencesDataStore(name = "last_location")

data class LastLocation (
    val locationId : String,
    val unit: String,
    val cityLabel: String,
    val lat: Double,
    val lon: Double,
    val seeMs: Long
)

class LastLocationDataStore @Inject constructor(
    @ApplicationContext private val context: Context){

    private object Keys {
        val LOCATION_ID = stringPreferencesKey("last_location_id")
        val UNIT  = stringPreferencesKey("last_unit")
        val CITY  = stringPreferencesKey("last_city")
        val LON = doublePreferencesKey("last_lon")
        val LAT = doublePreferencesKey("last_lat")
        val SEEN = longPreferencesKey("last_seen")
    }

    suspend fun save(
        locationId: String,
        unit: String,
        cityLabel: String,
        lon: Double,
        lat: Double,
        seenMs: Long = System.currentTimeMillis()
    ){
        context.lastLocationDataStore.edit { p ->
            p[Keys.LOCATION_ID] = locationId
            p[Keys.UNIT] = unit
            p[Keys.CITY] = cityLabel
            p[Keys.LON] = lon
            p[Keys.LAT] = lat
            p[Keys.SEEN] = seenMs

        }
    }

    suspend fun readOnce(): LastLocation? = withContext(Dispatchers.IO) {
        try {
            val p = context.lastLocationDataStore.data.first()
            val id  = p[Keys.LOCATION_ID] ?: return@withContext null
            val lat = p[Keys.LAT] ?: return@withContext null
            val lon = p[Keys.LON] ?: return@withContext null

            LastLocation(
                locationId = id,
                unit = p[Keys.UNIT] ?: "metric",
                cityLabel = p[Keys.CITY] ?: "",
                lat = lat,
                lon = lon,
                seeMs = p[Keys.SEEN] ?: 0L,
            )
        } catch (e: IOException) {
            null // treat as missing; avoid crashing startup
        }
    }

    val flow: Flow<LastLocation?> =
        context.lastLocationDataStore.data.map { p->
            LastLocation(
                locationId = p[Keys.LOCATION_ID] ?: return@map null,
                unit = p[Keys.UNIT] ?: "metric",
                cityLabel = p[Keys.CITY] ?: "",
                lon = p[Keys.LON] ?: 0.0,
                lat = p[Keys.LAT] ?: 0.0,
                seeMs = p[Keys.SEEN] ?: 0L,

            )
        }
}
