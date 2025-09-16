package com.example.weather.data.remote.dto.onecall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
//data class Hourly(
//    val clouds: Int,
//    val dew_point: Double,
//    val dt: Int,
//    val feels_like: Double,
//    val humidity: Int,
//    val pop: Double,
//    val pressure: Int,
//    val rain: Rain,
//    val temp: Double,
//    val uvi: Double,
//    val visibility: Int,
//    val weather: List<WeatherX>,
//    val wind_deg: Int,
//    val wind_gust: Double,
//    val wind_speed: Double
//)


@JsonClass(generateAdapter = true)
data class Hourly(
    val clouds: Int? = null,
    val dew_point: Double? = null,
    // dt is epoch seconds; prefer Long
    val dt: Int,
    val feels_like: Double? = null,
    val humidity: Int? = null,
    val pop: Double? = null,          // may be absent
    val pressure: Int? = null,
    val rain: Rain? = null,           // OPTIONAL → nullable
    val temp: Double,
    val uvi: Double? = null,          // often missing at night
    val visibility: Int? = null,
    val weather: List<WeatherX> = emptyList(),
    val wind_deg: Int? = null,
    val wind_gust: Double? = null,    // OPTIONAL → nullable
    val wind_speed: Double? = null
)

@JsonClass(generateAdapter = true)
data class Rain(
    @Json(name = "1h") val `1h`: Double? = null,
)
