package com.example.weather.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weather.model.FavoriteModel.Favorite
import com.example.weather.model.settingsModel.Unit

@Database(entities = [Favorite::class, Unit::class], version = 3, exportSchema = false)
abstract class WeatherDatabase: RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}