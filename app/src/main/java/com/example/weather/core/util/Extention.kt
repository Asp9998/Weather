package com.example.weather.core.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")
fun formatDate(timestamp: Int): String {
    val sdf = SimpleDateFormat("EEE, MMM d")
    val date = Date(timestamp.toLong() * 1000)

    return sdf.format(date)
}

@SuppressLint("SimpleDateFormat")
fun formatDateTime(timestamp: Int): String {
    val sdf = SimpleDateFormat("hh:mm aa")
    val date = Date(timestamp.toLong() * 1000)

    return sdf.format(date)
}

fun formatDecimals(item: Double): String {
    return "%.0f".format(item)
}
