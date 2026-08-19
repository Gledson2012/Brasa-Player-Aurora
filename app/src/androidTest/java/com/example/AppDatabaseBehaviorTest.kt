package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.db.AppDatabase
import com.example.data.model.Playlist
import com.example.data.model.LyricsEntity
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseBehaviorTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: MusicRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
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
    fun tearDown() {
        database.close()
    }

    @Test
    fun importedSourceKeyIsUnique() = runBlocking {
        val song = Song(
            title = "Arquivo local",
            artist = "Teste",
            album = "Álbum",
            durationMs = 120_000L,
            mediaUri = "content://test/audio/1",
            sourceKey = "content://test/audio/1"
        )
        val firstId = database.songDao().insertSong(song)
        assertNotNull(database.songDao().getSongBySourceKey(song.sourceKey!!))

        val duplicateId = database.songDao().insertSongs(listOf(song.copy(id = 0L))).single()
        assertEquals(-1L, duplicateId)
    }

    @Test
    fun playlistInsertAssignsStableOrderAndIgnoresDuplicates() = runBlocking {
        val songIds = database.songDao().insertSongs(
            listOf(
                Song(title = "Primeira", artist = "Teste", album = "Álbum", durationMs = 10_000L),
                Song(title = "Segunda", artist = "Teste", album = "Álbum", durationMs = 10_000L)
            )
        )
        val playlistId = database.playlistDao().insertPlaylist(Playlist(name = "Fila"))

        repository.addSongToPlaylist(playlistId, songIds[0])
        repository.addSongToPlaylist(playlistId, songIds[1])
        repository.addSongToPlaylist(playlistId, songIds[1])

        val refs = database.playlistDao().getAllCrossRefsOnce()
            .filter { it.playlistId == playlistId }
            .sortedBy { it.orderIndex }
        assertEquals(2, refs.size)
        assertEquals(listOf(0, 1), refs.map { it.orderIndex })
        assertEquals(songIds, refs.map { it.songId })
    }

    @Test
    fun deletingSongRemovesLyricsAndPlaylistReference() = runBlocking {
        val song = Song(
            title = "Faixa para excluir",
            artist = "Teste",
            album = "Álbum",
            durationMs = 10_000L
        )
        val songId = database.songDao().insertSong(song)
        val playlistId = database.playlistDao().insertPlaylist(Playlist(name = "Fila"))
        repository.addSongToPlaylist(playlistId, songId)
        database.lyricsDao().saveLyrics(
            LyricsEntity(songId = songId, content = "[00:01.00] Teste", isSynced = true, source = "Editor")
        )

        repository.deleteSong(song.copy(id = songId))

        assertNull(database.songDao().getSongByIdOnce(songId))
        assertNull(database.lyricsDao().getLyricsOnce(songId))
        assertTrue(database.playlistDao().getAllCrossRefsOnce().none { it.songId == songId })
    }
}
