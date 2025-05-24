package com.example.weather.model.GeoCoadingApiModels

data class CityNameItem(
    val country: String,
    val lat: Double,
    val local_names: LocalNames,
    val lon: Double,
    val name: String,
    val state: String
)