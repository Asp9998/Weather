package com.example.weather.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings_tbl")
data class UnitEntity(
    @PrimaryKey
    @ColumnInfo(name = "unit")
    val unit: String
)