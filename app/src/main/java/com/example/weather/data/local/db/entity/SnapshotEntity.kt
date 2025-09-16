package com.example.weather.data.local.db.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row = latest weather snapshot for (location_id, unit).
 * Store the exact JSON response and just enough metadata to run SWR.
 */
@Entity(
    tableName = "weather_snapshot",
    indices = [
        Index(value = ["location_id", "unit"], unique = true),   // one row per place+unit
        Index(value = ["location_id", "unit", "as_of_epoch"])    // fast lookups/comparisons
    ]
)
data class SnapshotEntity(
    @PrimaryKey
    @ColumnInfo(name = "snapshot_id")
    val snapshotId: String,                 // e.g., "$locationId|$unit"

    @ColumnInfo(name = "location_id")
    val locationId: String,                 // provider id or rounded "lat,lon"

    @ColumnInfo(name = "unit")
    val unit: String,                       // "metric" | "imperial"

    // Freshness / validation
    @ColumnInfo(name = "as_of_epoch")
    val asOfEpoch: Long,                    // seconds from API (e.g., current.dt)

    @ColumnInfo(name = "fetched_at_ms")
    val fetchedAtMs: Long,                  // device wall-clock when saved

    @ColumnInfo(name = "ttl_ms")
    val ttlMs: Long,                        // whole-snapshot TTL, e.g., 15 * 60 * 1000

    @ColumnInfo(name = "expires_at_ms")
    val expiresAtMs: Long,                  // fetchedAtMs + ttlMs (precomputed for quick checks)

    @ColumnInfo(name = "etag")
    val eTag: String? = null,               // nullable; server validator

    @ColumnInfo(name = "last_modified")
    val lastModified: String? = null,       // nullable; server validator

    @ColumnInfo(name = "city_label")
    val cityLabel: String,                   // City name

    @ColumnInfo(name = "city_lat")
    val cityLat: Double,                   // City Lat

    @ColumnInfo(name = "city_lon")
    val cityLon: Double,                   // City Lon

    // Payload (store exact JSON "as is")
    @ColumnInfo(name = "payload_json")
    val payloadBlob: ByteArray,                // full OneCall-style response as TEXT

    @ColumnInfo(name = "payload_version")
    val payloadVersion: Int = 1             // bump if your DTO/JSON shape changes
) {
    fun isExpired(nowMs: Long): Boolean = nowMs > expiresAtMs

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SnapshotEntity

        if (asOfEpoch != other.asOfEpoch) return false
        if (fetchedAtMs != other.fetchedAtMs) return false
        if (ttlMs != other.ttlMs) return false
        if (expiresAtMs != other.expiresAtMs) return false
        if (payloadVersion != other.payloadVersion) return false
        if (snapshotId != other.snapshotId) return false
        if (locationId != other.locationId) return false
        if (cityLabel != other.cityLabel) return false
        if (cityLat != other.cityLat) return false
        if (cityLon != other.cityLon) return false
        if (unit != other.unit) return false
        if (eTag != other.eTag) return false
        if (lastModified != other.lastModified) return false
        if (!payloadBlob.contentEquals(other.payloadBlob)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = asOfEpoch.hashCode()
        result = 31 * result + fetchedAtMs.hashCode()
        result = 31 * result + ttlMs.hashCode()
        result = 31 * result + expiresAtMs.hashCode()
        result = 31 * result + payloadVersion
        result = 31 * result + snapshotId.hashCode()
        result = 31 * result + locationId.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + cityLabel.hashCode()
        result = 31 * result + cityLat.hashCode()
        result = 31 * result + cityLon.hashCode()
        result = 31 * result + (eTag?.hashCode() ?: 0)
        result = 31 * result + (lastModified?.hashCode() ?: 0)
        result = 31 * result + payloadBlob.contentHashCode()
        return result
    }

}

fun buildSnapshotId(locationId: String, unit: String): String = "$locationId|$unit"
