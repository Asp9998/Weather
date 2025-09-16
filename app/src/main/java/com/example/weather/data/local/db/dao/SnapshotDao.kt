package com.example.weather.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.weather.data.local.db.entity.SnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SnapshotDao {

    @Query("SELECT * FROM weather_snapshot WHERE location_id = :locationID AND unit = :unit LIMIT 1")
    fun observeSnapshot(locationID: String, unit: String): Flow<SnapshotEntity?>

    @Query("SELECT * FROM weather_snapshot WHERE location_id = :locationID AND unit = :unit LIMIT 1")
    suspend fun getSnapshot(locationID: String, unit: String): SnapshotEntity?

    @Upsert
    suspend fun upsertSnapshot(entity: SnapshotEntity)

    @Query("""
            UPDATE weather_snapshot 
             SET fetched_at_ms = :fetchedAtMs,
                    expires_at_ms = :expiresAtMs 
             WHERE location_id = :locationId AND unit = :unit
            """)
    suspend fun touchFreshness(
        fetchedAtMs: Long,
        expiresAtMs: Long,
        locationId: String,
        unit: String
    )

    @Query("DELETE FROM weather_snapshot")
    suspend fun clearAll()


    // Most recently fetched snapshot for a unit (so metric/imperial stays consistent)
    @Query("""
        SELECT * FROM weather_snapshot
        WHERE unit = :unit
        ORDER BY fetched_at_ms DESC
        LIMIT 1
    """)
    suspend fun getLatestForUnit(unit: String): SnapshotEntity?

    // Optional: any snapshot if unit unknown yet
    @Query("""
        SELECT * FROM weather_snapshot
        ORDER BY fetched_at_ms DESC
        LIMIT 1
    """)
    suspend fun getLatestAny(): SnapshotEntity?

}