package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val gradientIndex: Int = 0,
    val iconName: String = "playlist_play",
    val createdAt: Long = System.currentTimeMillis(),
    val isSmart: Boolean = false
)
