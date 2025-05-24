package com.example.weather.screens.main

import androidx.lifecycle.ViewModel
import com.example.weather.data.DataOrException
import com.example.weather.model.OneCallApiModels.Weather
import com.example.weather.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: WeatherRepository)
    : ViewModel(){

    suspend fun getWeatherData(lat: Double, lon:Double, unit: String):
            DataOrException<Weather, Boolean, Exception>{
        return repository.getWeatherForCity(lat = lat, lon = lon, unit = unit)
    }


}