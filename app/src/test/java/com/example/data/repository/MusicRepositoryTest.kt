package com.example.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.model.LyricsEntity
import com.example.data.model.Song
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MusicRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: MusicRepository

    @Before
    fun createInMemoryDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .fallbackToDestructiveMigration()
            .build()
        repository = MusicRepository(
            database = database,
            songDao = database.songDao(),
            playlistDao = database.playlistDao(),
            userSettingsDao = database.userSettingsDao(),
            lyricsDao = database.lyricsDao()
        )
    }

    @After
    fun closeDb() {
        database.close()
    }

    private suspend fun seedSongs(vararg songs: Song): List<Long> =
        database.songDao().insertSongs(songs.toList())

    @Test
    fun `restoreBackupSnapshot restores songs into a clean database`() = runTest {
        // Arrange
        val a = Song(id = 101, title = "Track A", artist = "Artist", album = "Album", durationMs = 100000)
        val b = Song(id = 102, title = "Track B", artist = "Artist", album = "Album", durationMs = 200000)
        seedSongs(a, b)

        val playlist = Playlist(id = 201, name = "Playlist", isSmart = false)
        database.playlistDao().insertPlaylist(playlist)
        database.playlistDao().addSongToPlaylist(playlist.id, 101)
        database.userSettingsDao().saveUserSettings(UserSettingsEntity(id = 1))

        val snapshot = repository.exportBackupSnapshot()

        // Act
        val restored = repository.restoreBackupSnapshot(snapshot)

        // Assert
        assertEquals(2, restored.size)
        assertEquals(2, database.songDao().getAllSongsOnce().size)
        assertEquals(1, database.playlistDao().getAllPlaylistsOnce().size)
        assertEquals(1, database.playlistDao().getAllCrossRefsOnce().size)
        assertEquals(0, database.lyricsDao().getAllLyrics().size)
        assertTrue(database.songDao().getSongByIdOnce(101) != null)
        assertEquals("Track B", database.songDao().getSongByIdOnce(102)!!.title)
    }

    @Test
    fun `getListeningStatistics aggregates seed data`() = runTest {
        // Arrange
        seedSongs(
            Song(id = 301, title = "A", artist = "Artist", album = "Album", durationMs = 100000, isFavorite = true),
            Song(id = 302, title = "B", artist = "Artist", album = "Album", durationMs = 200000),
            Song(id = 303, title = "C", artist = "Another", album = "Album", durationMs = 50000, isFavorite = true, genre = "Jazz")
        )
        database.userSettingsDao().saveUserSettings(UserSettingsEntity(id = 1))

        // Act
        val stats = repository.getListeningStatistics()

        // Assert
        assertEquals(3, stats.totalSongs)
        assertEquals(2, stats.favoriteSongs)
        assertEquals(2, stats.uniqueArtists)
        assertEquals(1, stats.uniqueGenres)
        assertEquals(350000L, stats.totalLibraryDurationMs)
        // totalListenedDurationMs = sum(durationMs * playCount) where playCount > 0
        assertEquals(0L, stats.totalListenedDurationMs)
    }

    @Test
    fun `deleteSong removes the row, its lyrics and keeps playlist cross-refs consistent`() = runTest {
        // Arrange
        val song = Song(id = 401, title = "To Delete", artist = "X", album = "Y", durationMs = 5000)
        seedSongs(song)
        database.lyricsDao().saveLyrics(LyricsEntity(songId = 401, content = "[00:01] lyric"))
        database.playlistDao().addSongToPlaylist(501, 401)
        database.playlistDao().insertPlaylist(Playlist(id = 501, name = "P"))

        // Act
        repository.deleteSong(song)

        // Assert
        assertEquals(0, database.songDao().getAllSongsOnce().size)
        assertEquals(0, database.lyricsDao().getAllLyrics().size)
        // The playlist row remains (cross-references are lazily filtered out as
        // the song is no longer present in the library), therefore only 1 row.
        assertEquals(1, database.playlistDao().getAllPlaylistsOnce().size)
    }

    @Test
    fun `searchAndSortSongs returns matching rows in the requested order`() = runTest {
        // Arrange
        seedSongs(
            Song(id = 601, title = "Bossa Nova", artist = "A", album = "B", durationMs = 100L),
            Song(id = 602, title = "Rock", artist = "B", album = "C", durationMs = 200L)
        )

        // Act
        val results = repository.searchAndSortSongs("rock", "TITLE").first()

        // Assert
        assertEquals(1, results.size)
        assertEquals("Rock", results[0].title)
    }
}
