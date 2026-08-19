package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey
    val songId: Long,
    val content: String,
    val isSynced: Boolean = true,
    val source: String = "Editor"
)
