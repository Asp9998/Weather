package com.example.weather.data.remote.dto.onecall

data class Weather(
    val current: Current? = null,
    val daily: List<Daily>? = null,
    val hourly: List<Hourly>? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val minutely: List<Minutely>? = null,
    val timezone: String? = null,
    val timezone_offset: Int? = null
)