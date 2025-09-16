package com.example.weather.presentation

import androidx.compose.ui.graphics.Color
import com.example.weather.data.remote.dto.onecall.Weather

fun mapWeatherToSpec(w: Weather, reduceMotion: Boolean): WeatherVisualSpec {
    val tz = w.timezone_offset ?: 0
    val now = w.current?.dt?.toLong() ?: 0L
    val sunrise = w.current?.sunrise?.toLong() ?: Long.MAX_VALUE
    val sunset  = w.current?.sunset?.toLong() ?: Long.MIN_VALUE
    val isDay = now in sunrise..sunset

    val code = w.current?.weather?.firstOrNull()?.id ?: 800
    val intensity = (w.hourly?.getOrNull(0)?.rain?.`1h` ?: 0.0).toFloat()
    val clouds = (w.current?.clouds ?: 0) / 100f

    val (key, palette, overlay, overlayIntensity, lightning, star) = when {
        // --- Thunderstorm (200–232) ---
        code in 200..232 -> Quintuple(
            if (isDay) "day_storm" else "night_storm",
            if (isDay) listOf(Color(0xFF233A5E), Color(0xFF3E4A76)) else listOf(Color(0xFF0B1026), Color(0xFF211E47)),
            Overlay.STORM,
            intensity.coerceIn(0f, 1f),
            true,
            false
        )

// --- Drizzle (300–321) ---
        code in 300..321 -> Quintuple(
            "drizzle",
            if (isDay) listOf(Color(0xFF7AA2C2), Color(0xFFA9C0CF)) else listOf(Color(0xFF152433), Color(0xFF1E2E40)),
            Overlay.RAIN,
            (if (intensity > 0f) intensity else 0.35f).coerceIn(0f, 1f),
            false,
            !isDay && clouds < 0.3f
        )

// --- Rain (500–531) ---
        code in 500..531 -> Quintuple(
            "rain",
            if (isDay) listOf(Color(0xFF6C8FA6), Color(0xFFA9C0CF)) else listOf(Color(0xFF0E1B2A), Color(0xFF1E2E40)),
            Overlay.RAIN,
            (if (intensity > 0f) intensity else 0.45f).coerceIn(0f, 1f),
            false,
            !isDay && clouds < 0.25f
        )

// --- Snow (600–622) ---
        code in 600..622 -> Quintuple(
            if (isDay) "day_snow" else "night_snow",
            if (isDay) listOf(Color(0xFFBFD8E6), Color(0xFFE7F0F6)) else listOf(Color(0xFF0E2030), Color(0xFF2B3D4E)),
            Overlay.SNOW,
            intensity.coerceIn(0f, 1f),
            false,
            !isDay // stars only at night
        )

// --- Atmosphere (700–781) ---
// Mist (701)
        code == 701 -> Quintuple(
            "mist",
            listOf(Color(0xFFB9C3C9), Color(0xFFD7DDE1)),
            Overlay.FOG,
            0.5f,
            false,
            false
        )

// Smoke (711)
        code == 711 -> Quintuple(
            "smoke",
            listOf(Color(0xFF3A3A3A), Color(0xFF4B4B4B)),
            Overlay.FOG,
            0.6f,
            false,
            false
        )

// Haze (721)
        code == 721 -> Quintuple(
            "haze",
            listOf(Color(0xFFD6C6A4), Color(0xFFE3D5B8)),
            Overlay.FOG,
            0.5f,
            false,
            false
        )

// Sand/Dust (731, 751)
        code == 731 || code == 751 -> Quintuple(
            "dust_sand",
            listOf(Color(0xFFD2B48C), Color(0xFFE2C98F)),
            Overlay.FOG,
            0.55f,
            false,
            false
        )

// Fog (741)
        code == 741 -> Quintuple(
            "fog",
            listOf(Color(0xFFB9C3C9), Color(0xFFD7DDE1)),
            Overlay.FOG,
            0.65f,
            false,
            false
        )

// Dust (761)
        code == 761 -> Quintuple(
            "dust",
            listOf(Color(0xFF8E826D), Color(0xFFA0937D)),
            Overlay.FOG,
            0.6f,
            false,
            false
        )

// Volcanic Ash (762)
        code == 762 -> Quintuple(
            "ash",
            listOf(Color(0xFF4F4F4F), Color(0xFF5E5E5E)),
            Overlay.FOG,
            0.6f,
            false,
            false
        )

// Squall (771)
        code == 771 -> Quintuple(
            "squall",
            listOf(Color(0xFF2F4B65), Color(0xFF3B5B78)),
            Overlay.RAIN,
            0.7f,
            true,
            false
        )

// Tornado (781)
        code == 781 -> Quintuple(
            "tornado",
            listOf(Color(0xFF3B2A2A), Color(0xFF4A2F2F)),
            Overlay.STORM,
            0.8f,
            true,
            false
        )

// --- Clear (800) ---
        code == 800 -> Quintuple(
            if (isDay) "day_clear" else "night_clear",
            if (isDay) listOf(Color(0xFF87CEFA), Color(0xFFB0E0E6)) else listOf(Color(0xFF0A1433), Color(0xFF1D2B5A)),
            Overlay.NONE,
            0f,
            false,
            !isDay
        )

// --- Clouds (801–804) ---
// Few clouds
        code == 801 -> Quintuple(
            "few_clouds",
            if (isDay) listOf(Color(0xFFA0B4C0), Color(0xFFC2CFD7)) else listOf(Color(0xFF28384A), Color(0xFF3A4E61)),
            Overlay.NONE,
            clouds.coerceIn(0f, 1f),
            false,
            !isDay && clouds < 0.5f
        )

// Scattered clouds
        code == 802 -> Quintuple(
            "scattered_clouds",
            if (isDay) listOf(Color(0xFF9FB2BE), Color(0xFFB7C7D1)) else listOf(Color(0xFF223041), Color(0xFF324559)),
            Overlay.NONE,
            clouds.coerceIn(0f, 1f),
            false,
            !isDay && clouds < 0.4f
        )

// Broken clouds
        code == 803 -> Quintuple(
            "broken_clouds",
            if (isDay) listOf(Color(0xFF8A9AA8), Color(0xFFA5B0BA)) else listOf(Color(0xFF1A2633), Color(0xFF223144)),
            Overlay.NONE,
            clouds.coerceIn(0f, 1f),
            false,
            !isDay && clouds < 0.3f
        )

// Overcast
        code == 804 -> Quintuple(
            "overcast",
            if (isDay) listOf(Color(0xFF6D7A87), Color(0xFF8A98A7)) else listOf(Color(0xFF121D29), Color(0xFF1A2735)),
            Overlay.NONE,
            clouds.coerceIn(0f, 1f),
            false,
            false
        )

// --- Fallback ---
        else -> Quintuple(
            "default",
            if (isDay) listOf(Color(0xFFA0B4C0), Color(0xFFC2CFD7)) else listOf(Color(0xFF28384A), Color(0xFF3A4E61)),
            Overlay.NONE,
            0f,
            false,
            !isDay && clouds < 0.3f
        )
    }

    return WeatherVisualSpec(
        key = key,
        palette = palette,
        overlay = overlay,
        overlayIntensity = overlayIntensity,
        cloudAmount = clouds,
        starField = star,
        lightning = lightning,
        reduceMotion = reduceMotion
    )
}
// tiny helper
private data class Quintuple<A,B,C,D,E,F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)
