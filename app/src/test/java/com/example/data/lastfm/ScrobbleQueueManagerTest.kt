package com.example.data.lastfm

import com.example.data.db.ScrobbleDao
import com.example.data.datastore.LastFmPreferencesDataStore
import com.example.data.model.LastFmSettings
import com.example.data.model.Song
import io.mockk.coVerify
import io.mockk.every
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ScrobbleQueueManagerTest {

    private lateinit var scrobbleDao: ScrobbleDao
    private lateinit var lastFmDataStore: LastFmPreferencesDataStore
    private lateinit var manager: ScrobbleQueueManager

    private val testSong = Song(
        id = 1,
        title = "Test Song",
        artist = "Test Artist",
        album = "Test Album",
        durationMs = 200000L
    )

    @Before
    fun setup() {
        scrobbleDao = mockk(relaxed = true)
        lastFmDataStore = mockk(relaxed = true)
        manager = ScrobbleQueueManager(scrobbleDao, lastFmDataStore, mockk())
    }

    @Test
    fun pendingCount_exposesDaoFlow() = runTest {
        every { scrobbleDao.getPendingCount() } returns flowOf(5)
        val count = manager.pendingCount.first()
        assertEquals(5, count)
        verify { scrobbleDao.getPendingCount() }
    }

    @Test
    fun queueScrobble_ignoresDisabledLastFm() = runTest {
        every { lastFmDataStore.settingsFlow } returns flowOf(LastFmSettings(enabled = false))

        manager.queueScrobble(testSong, timestampSeconds = 123L)

        coVerify(exactly = 0) { scrobbleDao.insertPendingScrobble(any()) }
    }

    @Test
    fun queueScrobble_doesNotDuplicateExistingEntry() = runTest {
        val settings = LastFmSettings(
            apiKey = "key",
            apiSecret = "secret",
            sessionKey = "session",
            enabled = true
        )
        every { lastFmDataStore.settingsFlow } returns flowOf(settings)
        coEvery { scrobbleDao.findDuplicate(testSong.id, 123L) } returns mockk()

        manager.queueScrobble(testSong, timestampSeconds = 123L)

        coVerify { scrobbleDao.findDuplicate(testSong.id, 123L) }
        coVerify(exactly = 0) { scrobbleDao.insertPendingScrobble(any()) }
    }
}
