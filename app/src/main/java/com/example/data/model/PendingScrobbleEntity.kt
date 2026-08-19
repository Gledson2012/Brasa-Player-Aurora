package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a scrobble that is pending submission to Last.fm.
 *
 * When the user listens to music offline, scrobbles are stored locally
 * and submitted when connectivity is restored via WorkManager.
 */
@Entity(
    tableName = "pending_scrobbles",
    indices = [Index(value = ["songId", "timestampSeconds"], unique = true)]
)
data class PendingScrobbleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Long,
    val timestampSeconds: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastError: String? = null
)
