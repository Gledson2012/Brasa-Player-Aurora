package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [Index(value = ["sourceKey"], unique = true)]
)
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String = "",
    val coverDrawableName: String = "cover_synthwave",
    val coverUri: String? = null,
    val genre: String = "Geral",
    /** Stable identity for imported files; null is used by bundled synthetic tracks. */
    val sourceKey: String? = null,
    val isAvailable: Boolean = true,
    val isMediaStoreItem: Boolean = false,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val synthPreset: String? = null
)
