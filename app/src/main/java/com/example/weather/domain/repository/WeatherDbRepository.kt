package com.example.weather.domain.repository

import com.example.weather.data.local.db.dao.WeatherDao
import com.example.weather.data.local.db.entity.FavouriteEntity
import com.example.weather.data.local.db.entity.UnitEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WeatherDbRepository @Inject constructor(private val dao: WeatherDao){

    // Favorites
//    fun getAllFavorite(): Flow<List<Favorite>> = weatherDao.getAllFavorite()
//    suspend fun addFavorite(favorite: Favorite) = weatherDao.addFavorite(favorite)
//    suspend fun removeFavorite(favorite: Favorite) = weatherDao.removeFavorite(favorite)
//
//    // Unit
    fun getUnit(): Flow<List<UnitEntity>> = dao.getUnit()
    suspend fun addUnit(unit: UnitEntity) = dao.addUnit(unit)
    suspend fun removeUnit(unit: UnitEntity) = dao.removeUnit(unit)
    suspend fun deleteAll() = dao.deleteAll()

    fun observeAll() = dao.observeFavorites()
    suspend fun upsert(f: FavouriteEntity) = dao.upsert(f)
    suspend fun exists(id: String) = dao.exists(id)
    suspend fun touch(id: String, ts: Long) = dao.touch(id, ts)
    suspend fun delete(id: String) = dao.delete(id)


}