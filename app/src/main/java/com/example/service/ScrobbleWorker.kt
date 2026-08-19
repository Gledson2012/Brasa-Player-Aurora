package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.datastore.LastFmPreferencesDataStore
import com.example.data.db.AppDatabase
import com.example.data.lastfm.LastFmClient
import com.example.data.model.LastFmSettings
import com.example.data.model.PendingScrobbleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * WorkManager worker that processes pending scrobbles queue.
 *
 * Runs when network is available and processes scrobbles in batches.
 * Each scrobble is retried up to 3 times before being discarded.
 */
class ScrobbleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ScrobbleWorker"
        private const val WORK_NAME = "scrobble_queue_processor"
        private const val MAX_RETRIES = 3
        private const val BATCH_SIZE = 10

        /**
         * Enqueue a one-time work request to process pending scrobbles.
         * Uses ExistingWorkPolicy.KEEP to avoid duplicate work requests.
         */
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ScrobbleWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getInstance(applicationContext)
            val scrobbleDao = database.scrobbleDao()
            val lastFmDataStore = LastFmPreferencesDataStore(applicationContext)
            val settings = lastFmDataStore.settingsFlow.first()

            if (settings.apiKey.isBlank() || settings.sessionKey.isBlank()) {
                Log.d(TAG, "Last.fm not configured, skipping scrobble processing")
                return@withContext Result.success()
            }

            val client = LastFmClient(settings)
            val pendingScrobbles = scrobbleDao.getScrobblesReadyForRetry(MAX_RETRIES, BATCH_SIZE)

            if (pendingScrobbles.isEmpty()) {
                Log.d(TAG, "No pending scrobbles to process")
                return@withContext Result.success()
            }

            Log.d(TAG, "Processing ${pendingScrobbles.size} pending scrobbles")
            val successfullySubmitted = mutableListOf<Long>()

            for (scrobble in pendingScrobbles) {
                try {
                    client.scrobble(
                        song = com.example.data.model.Song(
                            title = scrobble.title,
                            artist = scrobble.artist,
                            album = scrobble.album,
                            durationMs = scrobble.durationSeconds * 1000L
                        ),
                        timestampSeconds = scrobble.timestampSeconds
                    )
                    successfullySubmitted.add(scrobble.id)
                    Log.d(TAG, "Successfully scrobbled: ${scrobble.artist} - ${scrobble.title}")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to scrobble: ${scrobble.artist} - ${scrobble.title}", e)
                    val updatedScrobble = scrobble.copy(
                        retryCount = scrobble.retryCount + 1,
                        lastError = e.message?.take(500)
                    )
                    scrobbleDao.updatePendingScrobble(updatedScrobble)
                }
            }

            if (successfullySubmitted.isNotEmpty()) {
                scrobbleDao.deletePendingScrobbles(successfullySubmitted)
                Log.d(TAG, "Successfully submitted ${successfullySubmitted.size} scrobbles")
            }

            // If there are more scrobbles pending, schedule another run
            val remainingCount = scrobbleDao.getScrobblesReadyForRetryOnce()
            if (remainingCount > 0) {
                enqueue(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing scrobble queue", e)
            Result.retry()
        }
    }
}
