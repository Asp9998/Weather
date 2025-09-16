package com.example.weather.data.remote

import com.example.weather.data.remote.dto.onecall.Weather
import com.example.weather.core.util.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WeatherService {
    @GET("data/3.0/onecall") // or "data/3.0/onecall" if your plan supports it
    suspend fun oneCall(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("exclude") exclude: String? = "minutely,alerts",
        @Query("appid") appid: String = Constants.API_KEY,
        @Header("If-None-Match") etag: String? = null,
        @Header("If-Modified-Since") lastModified: String? = null
    ): Response<Weather>
}