package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 virtual table for full-text search on songs.
 *
 * Uses FTS4 (not FTS5) for better Room compatibility. Provides instant
 * search across title, artist, album, and genre fields.
 *
 * The content table is synchronized via triggers automatically managed
 * by Room's @Fts4(contentEntity) annotation.
 */
@Fts4(contentEntity = Song::class)
@Entity(tableName = "songs_fts")
data class SongFtsEntity(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "artist")
    val artist: String,

    @ColumnInfo(name = "album")
    val album: String,

    @ColumnInfo(name = "genre")
    val genre: String
)
