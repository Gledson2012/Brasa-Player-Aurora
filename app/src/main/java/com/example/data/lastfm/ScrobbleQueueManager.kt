package com.example.data.lastfm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.datastore.LastFmPreferencesDataStore
import com.example.data.db.ScrobbleDao
import com.example.data.model.PendingScrobbleEntity
import com.example.data.model.Song
import com.example.service.ScrobbleWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Manages the offline scrobble queue for Last.fm.
 *
 * When the user listens to music offline, scrobbles are queued locally
 * and submitted when connectivity is restored. This ensures no scrobbles
 * are ever lost.
 */
class ScrobbleQueueManager(
    private val scrobbleDao: ScrobbleDao,
    private val lastFmDataStore: LastFmPreferencesDataStore,
    private val context: Context
) {
    companion object {
        private const val TAG = "ScrobbleQueueManager"
    }

    val pendingCount: Flow<Int>
        get() = scrobbleDao.getPendingCount()

    /**
     * Queue a scrobble for submission to Last.fm.
     * If online and Last.fm is configured, attempts immediate submission.
     * Otherwise, stores locally for later processing.
     */
    suspend fun queueScrobble(song: Song, timestampSeconds: Long = System.currentTimeMillis() / 1000L) {
        val settings = lastFmDataStore.settingsFlow.first()

        if (!settings.enabled || !settings.isAuthenticated) {
            Log.d(TAG, "Last.fm not configured, skipping scrobble")
            return
        }

        // Check for duplicate
        val existing = scrobbleDao.findDuplicate(song.id, timestampSeconds)
        if (existing != null) {
            Log.d(TAG, "Scrobble already queued: ${song.artist} - ${song.title}")
            return
        }

        val pendingScrobble = PendingScrobbleEntity(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            durationSeconds = song.durationMs / 1000L,
            timestampSeconds = timestampSeconds
        )

        // Try immediate submission if online
        if (isOnline()) {
            try {
                val client = LastFmClient(settings)
                client.scrobble(song, timestampSeconds)
                Log.d(TAG, "Successfully scrobbled immediately: ${song.artist} - ${song.title}")
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Immediate scrobble failed, queuing for later", e)
            }
        }

        // Queue for later processing
        val insertedId = scrobbleDao.insertPendingScrobble(pendingScrobble)
        if (insertedId == -1L) {
            Log.d(TAG, "Scrobble was queued concurrently: ${song.artist} - ${song.title}")
            return
        }
        Log.d(TAG, "Queued scrobble: ${song.artist} - ${song.title}")

        // Schedule WorkManager to process queue when online
        ScrobbleWorker.enqueue(context.applicationContext)
    }

    /**
     * Update now playing status (not queued - only sent when online)
     */
    suspend fun updateNowPlaying(song: Song) {
        if (!isOnline()) return

        val settings = lastFmDataStore.settingsFlow.first()
        if (!settings.enabled || !settings.isAuthenticated) return

        try {
            val client = LastFmClient(settings)
            client.updateNowPlaying(song)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update now playing", e)
        }
    }

    /**
     * Process any remaining scrobbles in the queue
     */
    suspend fun processPendingScrobbles() {
        ScrobbleWorker.enqueue(context.applicationContext)
    }

    /**
     * Clear all pending scrobbles (for logout or reset)
     */
    suspend fun clearPendingScrobbles() {
        ScrobbleWorker.cancel(context.applicationContext)
        scrobbleDao.clearAllPendingScrobbles()
    }

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
