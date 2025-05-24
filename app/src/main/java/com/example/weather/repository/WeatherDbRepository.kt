package com.example.weather.repository

import com.example.weather.data.WeatherDao
import com.example.weather.model.FavoriteModel.Favorite
import com.example.weather.model.settingsModel.Unit
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WeatherDbRepository @Inject constructor(private val weatherDao: WeatherDao){

    // Favorites
    fun getAllFavorite(): Flow<List<Favorite>> = weatherDao.getAllFavorite()
    suspend fun addFavorite(favorite: Favorite) = weatherDao.addFavorite(favorite)
    suspend fun removeFavorite(favorite: Favorite) = weatherDao.removeFavorite(favorite)

    // Unit
    fun getUnit(): Flow<List<Unit>> = weatherDao.getUnit()
    suspend fun addUnit(unit: Unit) = weatherDao.addUnit(unit)
    suspend fun removeUnit(unit: Unit) = weatherDao.removeUnit(unit)
    suspend fun deleteAll() = weatherDao.deleteAll()


}