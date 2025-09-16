package com.example.weather.data.remote.dto.onecall

data class Daily(
    val clouds: Int? = null,
    val dew_point: Double? = null,
    val dt: Int,
    val feels_like: FeelsLike? = null,
    val humidity: Int? = null,
    val moon_phase: Double? = null,
    val moonrise: Int? = null,
    val moonset: Int? = null,
    val pop: Double? = null,
    val pressure: Int? = null,
    val rain: Double? = null,
    val summary: String? = null,
    val sunrise: Int? = null,
    val sunset: Int? = null,
    val temp: Temp? = null,
    val uvi: Double? = null,
    val weather: List<WeatherX>? = null,
    val wind_deg: Int? = null,
    val wind_gust: Double? = null,
    val wind_speed: Double? = null
)