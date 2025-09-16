package com.example.weather.data.remote.dto.geocoading

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CityNameItem(
    val country: String,
    val lat: Double,
//    val local_names: LocalNames,
    val lon: Double,
    val name: String,
    val state: String,
    @Json(name = "local_names") val localNames: Map<String, String>? = null
)