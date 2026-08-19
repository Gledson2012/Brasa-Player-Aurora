package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.LyricsEntity

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics WHERE songId = :songId")
    suspend fun getLyricsOnce(songId: Long): LyricsEntity?

    @Query("SELECT * FROM lyrics")
    suspend fun getAllLyrics(): List<LyricsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLyrics(lyrics: LyricsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreLyrics(lyrics: List<LyricsEntity>)

    @Query("DELETE FROM lyrics WHERE songId = :songId")
    suspend fun deleteLyrics(songId: Long)

    @Query("DELETE FROM lyrics")
    suspend fun clearAllLyrics()
}
