package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val themeName: String = "MIDNIGHT_OLED",
    val visualizerStyle: String = "BARS",
    val albumArtStyle: String = "VINYL_ROTATION",
    val dynamicColors: Boolean = false,
    val equalizerEnabled: Boolean = true,
    val currentPresetId: String = "flat",
    val band0: Int = 0,
    val band1: Int = 0,
    val band2: Int = 0,
    val band3: Int = 0,
    val band4: Int = 0,
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val balance: Float = 0.0f,
    val playbackSpeed: Float = 1.0f,
    val crossfadeSeconds: Int = 0,
    val repeatMode: String = "ALL", // OFF, ALL, ONE
    val isShuffle: Boolean = false,
    val lastPlayedSongId: Long? = null,
    val lastPlaybackPositionMs: Long = 0L
)
