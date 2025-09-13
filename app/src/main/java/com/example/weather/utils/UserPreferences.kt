package com.example.weather.utils

import android.content.Context
import android.util.Log
import androidx.core.content.edit

object UserPreferences {
    private const val PREF_NAME = "weather"
    private const val LATEST_SEARCHED_CITY_KEY = "latest_searched_city"
    private const val LATEST_SEARCHED_CITY_LAT_KEY = "latest_searched_city_lat"
    private const val LATEST_SEARCHED_CITY_LON_KEY = "latest_searched_city_lon"

    fun saveLatestSearchedCity(context: Context, city: String, cityLat: Double, cityLon: Double){
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit { putString (LATEST_SEARCHED_CITY_KEY, city)}
        pref.edit {putFloat(LATEST_SEARCHED_CITY_LAT_KEY, cityLat.toFloat())}
        pref.edit {putFloat(LATEST_SEARCHED_CITY_LON_KEY, cityLon.toFloat())}
    }

    fun getLatestSearchedCity(context: Context): List<Any>{
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val city =  pref.getString(LATEST_SEARCHED_CITY_KEY, "Toronto").toString()
        val cityLat = pref.getFloat(LATEST_SEARCHED_CITY_LAT_KEY, 43.6532f).toDouble()
        val cityLon = pref.getFloat(LATEST_SEARCHED_CITY_LON_KEY, -79.3832f).toDouble()

//        Log.d("LATESTSAVEDCITY", "getLatestSearchedCity: $city, $cityLat, $cityLon")
        return listOf(city, cityLat, cityLon)
    }
}