package com.example.weather.domain.repository

import android.util.Log
import com.example.weather.core.util.DataOrException
import com.example.weather.data.remote.WeatherApi
import com.example.weather.data.remote.dto.geocoading.CityNameItem
import com.example.weather.data.remote.dto.onecall.Weather
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val api: WeatherApi) {

    suspend fun getWeatherForCity(lat: Double, lon: Double, unit: String):
            DataOrException<Weather, Boolean, Exception> {

        // Fetch city Info (latitude and longitude)
        val response = try {
            api.getWeather(latitude = lat, longitude = lon, units = unit)
        } catch (e: Exception){
            Log.d("Ex", "getWeather: $e")
            return DataOrException(e = e)   // return error if failed to get city info
        }

        // Ensure we have a valid city Result(not null)
        if(response.lon.toString().isEmpty()){
            return DataOrException(e = Exception("City not found"))   // Handle case if no city was found
        }

        // Fetch the weather using the city's latitude and longitude
        Log.d("LAT", "getWeatherForCity: ${response.lat}, ${response.lon}")
        return DataOrException(data = response)

    }

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