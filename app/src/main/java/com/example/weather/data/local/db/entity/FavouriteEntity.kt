package com.example.weather.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "fav_tbl",
    indices = [
        Index(value = ["pinned", "last_viewed_ms"]) // optional, for sorted lists
    ] )

data class FavouriteEntity(
    @PrimaryKey
    @ColumnInfo(name = "location_id")
    val locationId: String,

    @ColumnInfo(name = "city_label")
    val cityLabel: String,

    @ColumnInfo(name = "city_lat")
    val cityLat: Double,

    @ColumnInfo(name = "city_lon")
    val cityLon: Double,

    @ColumnInfo(name = "last_viewed_ms")
    val lastViewedMs: Long,

    @ColumnInfo(name = "pinned")
    val pinned: Boolean = false


)