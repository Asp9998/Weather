package com.example.weather.network

import com.example.weather.model.GeoCoadingApiModels.CityName
import com.example.weather.model.OneCallApiModels.Weather
import com.example.weather.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface WeatherApi {

    // GET LAT AND LON USING CITY NAME
    @GET(value = "geo/1.0/direct")
    suspend fun getCityInfo(
        @Query("q") query: String,
        @Query("limit") limit: String = "1",
        @Query("appid") appid: String = Constants.API_KEY
    ): CityName


    // GET WEATHER USING LAT AND LOG
    @GET(value = "data/3.0/onecall")
    suspend fun getWeather(
        @Query("lat") latitude: Double = 49.8955367,
        @Query("lon") longitude: Double = -97.1384584,
        @Query("exclude") exclude: String = "minutely,alerts",
        @Query("units") units: String = "metric",
        @Query("appid") appid: String = Constants.API_KEY
    ): Weather
}