package com.example.weather.data.remote.dto.onecall

data class Current(
    val clouds: Int? = null,
    val dew_point: Double? = null,
    val dt: Int? = null,
    val feels_like: Double? = null,
    val humidity: Int? = null,
    val pressure: Int? = null,
    val sunrise: Int? = null,
    val sunset: Int? = null,
    val temp: Double? = null,
    val uvi: Double? = null,
    val visibility: Int? = null,
    val weather: List<WeatherX>? = null,
    val wind_deg: Int? = null,
    val wind_speed: Double? = null
)