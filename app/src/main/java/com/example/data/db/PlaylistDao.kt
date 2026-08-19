package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.PlaylistWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists ORDER BY id ASC")
    suspend fun getAllPlaylistsOnce(): List<Playlist>

    @Query("SELECT * FROM playlist_songs ORDER BY playlistId ASC, orderIndex ASC")
    suspend fun getAllCrossRefsOnce(): List<PlaylistSongCrossRef>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylists(playlists: List<Playlist>): List<Long>

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Transaction
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val nextOrder = getNextOrderIndex(playlistId)
        insertPlaylistSongCrossRef(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderIndex = nextOrder
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)

    @Query("DELETE FROM playlist_songs")
    suspend fun clearAllCrossRefs()

    @Query("DELETE FROM playlists")
    suspend fun clearAllPlaylists()

    @Query("SELECT COALESCE(MAX(orderIndex), -1) + 1 FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getNextOrderIndex(playlistId: Long): Int

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongCountForPlaylist(playlistId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
}
