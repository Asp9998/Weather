package com.example.weather.ui.navigation

import android.net.Uri

object WeatherScreen {
    object MainScreen {
        const val base = "main"
        const val route = "main?lat={lat}&lon={lon}&label={label}"

        fun build(lat: Double?, lon: Double?, label: String?): String {
            // omit params when null
            return buildString {
                append(base)
                if (lat != null && lon != null) {
                    append("?lat=$lat&lon=$lon")
                    if (!label.isNullOrBlank()) {
                        append("&label=${Uri.encode(label)}")
                    }
                }
            }
        }
    }
}