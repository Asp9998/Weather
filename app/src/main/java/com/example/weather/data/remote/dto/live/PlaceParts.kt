package com.example.weather.data.remote.dto.live

data class PlaceParts(
    val city: String?,         // locality
    val region: String?,       // adminArea / province/state
    val country: String?,      // country name
    val fullAddress: String?   // formatted single line
)