package com.example.weather.domain.repository

import android.util.Log
import com.example.weather.core.util.DataOrException
import com.example.weather.data.remote.WeatherApi
import com.example.weather.data.remote.dto.geocoading.CityNameItem
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val api: WeatherApi) {

    suspend fun getCities(cityName: String):
            DataOrException<List<CityNameItem>, Boolean, Exception> {

        val dataOrException = DataOrException<List<CityNameItem>, Boolean, Exception>()
        try {
            Log.d("SearchCitySolve", "Setting View model: Loading cities in repository")
            dataOrException.loading = true
            dataOrException.data = api.getCityInfo(query = cityName, limit = "50")
        } catch (e: Exception) {
            Log.d("SearchCitySolve", "Setting View model: error in repo:$e")
            dataOrException.e = e
        } finally {
            dataOrException.loading = false
        }
        return dataOrException
    }

}