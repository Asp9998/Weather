package com.example.weather.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weather.data.local.db.entity.FavouriteEntity
import com.example.weather.data.local.db.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // favorites
    @Query("SELECT * from fav_tbl")
    fun getAllFavorite(): Flow<List<FavouriteEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addFavorite(favorite: FavouriteEntity)

    @Delete
    suspend fun removeFavorite(favorite: FavouriteEntity)


    // Units

    @Query("SELECT * from settings_tbl")
    fun getUnit(): Flow<List<UnitEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addUnit(unit: UnitEntity)

    @Delete
    suspend fun removeUnit(unit: UnitEntity)

    @Query("DELETE FROM settings_tbl")
    suspend fun deleteAll()


    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsert(f: FavouriteEntity)

    @Query("UPDATE fav_tbl SET last_viewed_ms = :ts WHERE location_id = :locationId")
    suspend fun touch(locationId: String, ts: Long)

    @Query("DELETE FROM fav_tbl WHERE location_id = :locationId")
    suspend fun delete(locationId: String)

    @Query("""
        SELECT * FROM fav_tbl
        ORDER BY pinned DESC, last_viewed_ms DESC
    """)
    fun observeFavorites(): Flow<List<FavouriteEntity>>

    @Query("SELECT * FROM fav_tbl WHERE location_id = :locationId LIMIT 1")
    suspend fun getById(locationId: String): FavouriteEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM fav_tbl WHERE location_Id = :id)")
    suspend fun exists(id: String): Boolean


}