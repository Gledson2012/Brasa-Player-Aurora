package com.example.data.backup

import com.example.data.model.CustomPresetEntity
import com.example.data.model.LyricsEntity
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import com.example.data.model.UserSettingsEntity
import com.example.data.repository.MusicRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertDoesNotThrow
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupManagerTest {

    private fun manager() = BackupManager(
        repository = mockk(relaxed = true),
        themeDataStore = mockk(relaxed = true)
    )

    private fun song(
        id: Long = 1,
        title: String = "Neon Horizon",
        artist: String = "Cyber Wave",
        album: String = "Outrun 1984",
        sourceKey: String? = "content://media/external/audio/media/1"
    ) = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = 204000L,
        mediaUri = "content://media/external/audio/media/1",
        sourceKey = sourceKey,
        isAvailable = true,
        isMediaStoreItem = true,
        genre = "Synthwave",
        isFavorite = true,
        playCount = 5,
        lastPlayedTimestamp = 1000L,
        addedTimestamp = 2000L,
        coverDrawableName = "cover_synthwave",
        coverUri = "content://media/external/audio/albumart/1",
        synthPreset = "synthwave"
    )

    @Test
    fun `songToJson / jsonToSong round-trips every field`() {
        val song = song(id = 42, sourceKey = "content://media/external/audio/media/42")
        val restored = manager().jsonToSong(manager().songToJson(song))

        assertEquals(42, restored.id)
        assertEquals("Neon Horizon", restored.title)
        assertEquals("Cyber Wave", restored.artist)
        assertEquals("Outrun 1984", restored.album)
        assertEquals(204000L, restored.durationMs)
        assertEquals("content://media/external/audio/media/42", restored.sourceKey)
        assertEquals(true, restored.isAvailable)
        assertEquals(true, restored.isMediaStoreItem)
        assertEquals("Synthwave", restored.genre)
        assertEquals(true, restored.isFavorite)
        assertEquals(5, restored.playCount)
        assertEquals(1000L, restored.lastPlayedTimestamp)
        assertEquals(2000L, restored.addedTimestamp)
        assertEquals("cover_synthwave", restored.coverDrawableName)
        assertEquals("content://media/external/audio/albumart/1", restored.coverUri)
        assertEquals("synthwave", restored.synthPreset)
    }

    @Test
    fun `songToJson / jsonToSong falls back to mediaUri when sourceKey is absent`() {
        val song = song(id = 7, sourceKey = null)
        val restored = manager().jsonToSong(manager().songToJson(song))

        assertEquals("content://media/external/audio/media/7", restored.sourceKey)
        assertEquals("Faixa Desconhecida", restored.title)
        assertEquals("Artista Desconhecido", restored.artist)
        assertEquals("Álbum Desconhecido", restored.album)
    }

    @Test
    fun `playlistToJson / jsonToPlaylist round-trips fields`() {
        val playlist = Playlist(
            id = 3L, name = "Favoritas", description = "Músicas favoritas",
            gradientIndex = 2, iconName = "favorite", createdAt = 1000L, isSmart = false
        )
        val restored = manager().jsonToPlaylist(manager().playlistToJson(playlist))

        assertEquals(3L, restored.id)
        assertEquals("Favoritas", restored.name)
        assertEquals("Músicas favoritas", restored.description)
        assertEquals(2, restored.gradientIndex)
        assertEquals("favorite", restored.iconName)
        assertEquals(false, restored.isSmart)
    }

    @Test
    fun `crossRefToJson / jsonToCrossRef round-trips fields`() {
        val ref = PlaylistSongCrossRef(playlistId = 1, songId = 2, orderIndex = 3, addedAt = 4000L)
        val restored = manager().jsonToCrossRef(manager().crossRefToJson(ref))

        assertEquals(1, restored.playlistId)
        assertEquals(2, restored.songId)
        assertEquals(3, restored.orderIndex)
        assertEquals(4000L, restored.addedAt)
    }

    @Test
    fun `settingsToJson / jsonToSettings round-trips fields`() {
        val settings = UserSettingsEntity(
            id = 1, themeName = "MIDNIGHT_OLED", visualizerStyle = "BARS",
            albumArtStyle = "VINYL_ROTATION", dynamicColors = false, equalizerEnabled = true,
            currentPresetId = "custom_1", band0 = -2, band1 = 3, band2 = 0, band3 = 1,
            band4 = 2, bassBoost = 25, virtualizer = 40, balance = 0.25f,
            playbackSpeed = 1.5f, crossfadeSeconds = 4, repeatMode = "ONE", isShuffle = true,
            lastPlayedSongId = 7L, lastPlaybackPositionMs = 12000L
        )
        val restored = manager().jsonToSettings(manager().settingsToJson(settings))

        assertEquals(1, restored.id)
        assertEquals("custom_1", restored.currentPresetId)
        assertEquals(-2, restored.band0)
        assertEquals(3, restored.band1)
        assertEquals(0, restored.band2)
        assertEquals(1, restored.band3)
        assertEquals(2, restored.band4)
        assertEquals(25, restored.bassBoost)
        assertEquals(40, restored.virtualizer)
        assertEquals(0.25f, restored.balance, 0.001f)
        assertEquals(1.5f, restored.playbackSpeed, 0.001f)
        assertEquals(4, restored.crossfadeSeconds)
        assertEquals("ONE", restored.repeatMode)
        assertEquals(true, restored.isShuffle)
        assertEquals(7L, restored.lastPlayedSongId)
        assertEquals(12000L, restored.lastPlaybackPositionMs)
    }

    @Test
    fun `presetToJson / jsonToPreset round-trips custom preset with negative bands`() {
        val preset = CustomPresetEntity(
            id = 10, name = "Heavy Bass", band0 = 6, band1 = 4, band2 = -3,
            band3 = -1, band4 = 1, bassBoost = 55, virtualizer = 15
        )
        val restored = manager().jsonToPreset(manager().presetToJson(preset))

        assertEquals(10, restored.id)
        assertEquals("Heavy Bass", restored.name)
        assertEquals(6, restored.band0)
        assertEquals(4, restored.band1)
        assertEquals(-3, restored.band2)
        assertEquals(-1, restored.band3)
        assertEquals(1, restored.band4)
        assertEquals(55, restored.bassBoost)
        assertEquals(15, restored.virtualizer)
    }

    @Test
    fun `lyricsToJson / jsonToLyrics round-trips fields`() {
        val lyrics = LyricsEntity(
            songId = 9L, content = "[00:10.50] Verse 1", isSynced = false, source = "Editor"
        )
        val restored = manager().jsonToLyrics(manager().lyricsToJson(lyrics))

        assertEquals(9L, restored.songId)
        assertEquals("[00:10.50] Verse 1", restored.content)
        assertEquals(false, restored.isSynced)
        assertEquals("Editor", restored.source)
    }

    @Test
    fun `validateSnapshot accepts a valid snapshot without throwing`() {
        val snapshot = MusicRepository.BackupSnapshot(
            songs = listOf(song(id = 11)),
            playlists = listOf(Playlist(id = 111, name = "Favoritas", isSmart = false)),
            crossRefs = listOf(PlaylistSongCrossRef(playlistId = 111, songId = 11)),
            userSettings = UserSettingsEntity(id = 1),
            customPresets = emptyList(),
            lyrics = emptyList()
        )
        assertDoesNotThrow { manager().validateSnapshot(snapshot) }
    }

    @Test
    fun `validateSnapshot rejects duplicate sourceKeys`() {
        val snapshot = MusicRepository.BackupSnapshot(
            songs = listOf(song(id = 1), song(id = 2, sourceKey = "content://media/external/audio/media/1")),
            playlists = emptyList(), crossRefs = emptyList(),
            userSettings = UserSettingsEntity(id = 1), customPresets = emptyList(), lyrics = emptyList()
        )
        assertThrows(IllegalArgumentException::class.java) { manager().validateSnapshot(snapshot) }
    }

    @Test
    fun `validateSnapshot rejects invalid text length`() {
        val song = song(id = 3).copy(title = "A".repeat(1001), artist = "B".repeat(1001))
        val snapshot = MusicRepository.BackupSnapshot(
            songs = listOf(song), playlists = emptyList(), crossRefs = emptyList(),
            userSettings = UserSettingsEntity(id = 1), customPresets = emptyList(), lyrics = emptyList()
        )
        assertThrows(IllegalArgumentException::class.java) { manager().validateSnapshot(snapshot) }
    }

    @Test
    fun `validateSnapshot rejects duplicate song ids`() {
        val snapshot = MusicRepository.BackupSnapshot(
            songs = listOf(song(id = 3), song(id = 3)),
            playlists = emptyList(), crossRefs = emptyList(),
            userSettings = UserSettingsEntity(id = 1), customPresets = emptyList(), lyrics = emptyList()
        )
        assertThrows(IllegalArgumentException::class.java) { manager().validateSnapshot(snapshot) }
    }

    @Test
    fun `validateSnapshot rejects crossRef referencing an unknown song`() {
        val snapshot = MusicRepository.BackupSnapshot(
            songs = listOf(song(id = 11)),
            playlists = listOf(Playlist(id = 111, name = "F")),

            crossRefs = listOf(PlaylistSongCrossRef(playlistId = 111, songId = 999)),
            userSettings = UserSettingsEntity(id = 1), customPresets = emptyList(), lyrics = emptyList()
        )
        assertThrows(IllegalArgumentException::class.java) { manager().validateSnapshot(snapshot) }
    }

    @Test
    fun `validateSnapshot rejects lyrics whose song does not exist`() {
        val snapshot = MusicRepository.BackupSnapshot(
            songs = listOf(song(id = 11)),
            playlists = emptyList(), crossRefs = emptyList(),
            userSettings = UserSettingsEntity(id = 1),
            customPresets = emptyList(),
            lyrics = listOf(LyricsEntity(songId = 77, content = "x"))
        )
        assertThrows(IllegalArgumentException::class.java) { manager().validateSnapshot(snapshot) }
    }

    @Test
    fun `validateSnapshot rejects duplicate lyrics for the same song`() {
        val snapshot = MusicRepository.BackupSnapshot(
            songs = listOf(song(id = 11)),
            playlists = emptyList(), crossRefs = emptyList(),
            userSettings = UserSettingsEntity(id = 1),
            customPresets = emptyList(),
            lyrics = listOf(LyricsEntity(songId = 11, content = "a"), LyricsEntity(songId = 11, content = "b"))
        )
        assertThrows(IllegalArgumentException::class.java) { manager().validateSnapshot(snapshot) }
    }

    @Test
    fun `validateSnapshot rejects preset with out-of-range band`() {
        val preset = CustomPresetEntity(id = 1, name = "x", band0 = 11, band1 = 0, band2 = 0, band3 = 0, band4 = 0)
        val snapshot = MusicRepository.BackupSnapshot(
            songs = emptyList(), playlists = emptyList(), crossRefs = emptyList(),
            userSettings = UserSettingsEntity(id = 1), customPresets = listOf(preset), lyrics = emptyList()
        )
        assertThrows(IllegalArgumentException::class.java) { manager().validateSnapshot(snapshot) }
    }

    @Test
    fun `validateSnapshot rejects userSettings with an unknown repeatMode`() {
        val settings = UserSettingsEntity(id = 1, repeatMode = "INVALID")
        val snapshot = MusicRepository.BackupSnapshot(
            songs = emptyList(), playlists = emptyList(), crossRefs = emptyList(),
            userSettings = settings, customPresets = emptyList(), lyrics = emptyList()
        )
        assertThrows(IllegalArgumentException::class.java) { manager().validateSnapshot(snapshot) }
    }
}
