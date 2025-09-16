package com.example.weather.presentation

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ui/visual/WeatherVisualSpec.kt
enum class Overlay { NONE, RAIN, SNOW, FOG, STORM }

data class WeatherVisualSpec(
    val key: String,                 // stable id like "day_clear", "night_rain"
    val palette: List<Color>,        // 2–3 gradient stops
    val overlay: Overlay = Overlay.NONE,
    val overlayIntensity: Float = 0f,// 0..1
    val cloudAmount: Float = 0f,     // 0..1
    val starField: Boolean = false,
    val lightning: Boolean = false,
    val reduceMotion: Boolean = false,
)