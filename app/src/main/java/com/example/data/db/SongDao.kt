package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs ORDER BY id ASC")
    suspend fun getAllSongsOnce(): List<Song>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT 30")
    fun getRecentlyPlayedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT 30")
    fun getMostPlayedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSongById(id: Long): Flow<Song?>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongByIdOnce(id: Long): Song?

    @Query("SELECT * FROM songs WHERE mediaUri = :mediaUri LIMIT 1")
    suspend fun getSongByMediaUri(mediaUri: String): Song?

    @Query("SELECT * FROM songs WHERE sourceKey = :sourceKey LIMIT 1")
    suspend fun getSongBySourceKey(sourceKey: String): Song?

    @Query("SELECT * FROM songs WHERE mediaUri IN (:mediaUris)")
    suspend fun getSongsByMediaUris(mediaUris: List<String>): List<Song>

    @Query("SELECT * FROM songs WHERE sourceKey IN (:sourceKeys)")
    suspend fun getSongsBySourceKeys(sourceKeys: List<String>): List<Song>

    @Query("UPDATE songs SET isAvailable = 0 WHERE isMediaStoreItem = 1")
    suspend fun markMediaStoreSongsUnavailable()

    @Query("UPDATE songs SET isAvailable = 1 WHERE sourceKey IN (:sourceKeys)")
    suspend fun markSourcesAvailable(sourceKeys: List<String>)

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchSongs(query: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE ASC")
    fun searchSongsByTitleOrArtist(query: String): Flow<List<Song>>

    @Query("""
        SELECT * FROM songs 
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%') 
        ORDER BY 
            CASE WHEN :sort = 'TITLE' THEN LOWER(title) END ASC,
            CASE WHEN :sort = 'ARTIST' THEN LOWER(artist) END ASC,
            CASE WHEN :sort = 'DURATION' THEN durationMs END DESC,
            CASE WHEN :sort = 'RECENTLY_ADDED' THEN addedTimestamp END DESC
    """)
    fun searchAndSortSongs(query: String, sort: String): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongs(songs: List<Song>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreSongs(songs: List<Song>)

    @Update
    suspend fun updateSong(song: Song)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun recordSongPlayed(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: Long)

    @Query("DELETE FROM songs")
    suspend fun clearAllSongs()

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    // FTS5 Full-Text Search queries
    @Query("""
        SELECT songs.* FROM songs
        JOIN songs_fts ON songs.rowid = songs_fts.rowid
        WHERE songs_fts MATCH :query
        ORDER BY
            CASE WHEN songs_fts MATCH :query THEN 1 ELSE 0 END DESC,
            songs.title ASC
    """)
    fun searchSongsFts(query: String): Flow<List<Song>>

    @Query("""
        SELECT songs.* FROM songs
        JOIN songs_fts ON songs.rowid = songs_fts.rowid
        WHERE songs_fts MATCH :query
        ORDER BY songs.title ASC
    """)
    suspend fun searchSongsFtsOnce(query: String): List<Song>

    @Query("""
        SELECT songs.* FROM songs
        JOIN songs_fts ON songs.rowid = songs_fts.rowid
        WHERE songs_fts MATCH :query
        ORDER BY
            CASE WHEN :sort = 'TITLE' THEN LOWER(songs.title) END ASC,
            CASE WHEN :sort = 'ARTIST' THEN LOWER(songs.artist) END ASC,
            CASE WHEN :sort = 'ALBUM' THEN LOWER(songs.album) END ASC,
            CASE WHEN :sort = 'RECENTLY_ADDED' THEN songs.addedTimestamp END DESC
    """)
    fun searchAndSortSongsFts(query: String, sort: String): Flow<List<Song>>
}
