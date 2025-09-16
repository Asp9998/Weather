package com.example.weather.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weather.data.local.db.dao.SnapshotDao
import com.example.weather.data.local.db.entity.SnapshotEntity
import com.example.weather.data.local.db.dao.WeatherDao
import com.example.weather.data.local.db.entity.FavouriteEntity
import com.example.weather.data.local.db.entity.UnitEntity

@Database(entities = [FavouriteEntity::class, UnitEntity::class, SnapshotEntity::class],

    version = 5, exportSchema = false)
abstract class WeatherDatabase: RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun snapshotDao(): SnapshotDao
}