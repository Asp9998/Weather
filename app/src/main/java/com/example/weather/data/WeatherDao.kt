package com.example.weather.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weather.model.FavoriteModel.Favorite
import com.example.weather.model.settingsModel.Unit
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // favorites
    @Query("SELECT * from fav_tbl")
    fun getAllFavorite(): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Delete
    suspend fun removeFavorite(favorite: Favorite)


    // Units

    @Query("SELECT * from settings_tbl")
    fun getUnit(): Flow<List<Unit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUnit(unit: Unit)

    @Delete
    suspend fun removeUnit(unit: Unit)

    @Query("DELETE FROM settings_tbl")
    suspend fun deleteAll()


}