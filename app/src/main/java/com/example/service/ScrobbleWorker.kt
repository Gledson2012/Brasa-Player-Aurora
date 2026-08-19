package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
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
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that processes pending scrobbles queue.
 *
 * Runs when network is available and processes scrobbles in batches.
 * Each scrobble is retried up to three times before being discarded.
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
         * Uses a unique appendable chain so concurrent enqueue calls do not
         * create parallel workers or cancel the worker currently in progress.
         */
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ScrobbleWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30L,
                    TimeUnit.SECONDS
                )
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    workRequest
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context.applicationContext)
                .cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getInstance(applicationContext)
            val scrobbleDao = database.scrobbleDao()
            val lastFmDataStore = LastFmPreferencesDataStore(applicationContext)
            val settings = lastFmDataStore.settingsFlow.first()

            if (!settings.enabled || !settings.isAuthenticated) {
                Log.d(TAG, "Last.fm not configured, skipping scrobble processing")
                return@withContext Result.success()
            }

            val client = LastFmClient(settings)
            scrobbleDao.deleteExhaustedScrobbles(MAX_RETRIES)
            val pendingScrobbles = scrobbleDao.getScrobblesReadyForRetry(MAX_RETRIES, BATCH_SIZE)

            if (pendingScrobbles.isEmpty()) {
                Log.d(TAG, "No pending scrobbles to process")
                return@withContext Result.success()
            }

            Log.d(TAG, "Processing ${pendingScrobbles.size} pending scrobbles")
            val successfullySubmitted = mutableListOf<Long>()
            var failedCount = 0

            for (scrobble in pendingScrobbles) {
                try {
                    client.scrobble(
                        song = Song(
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
                    failedCount++
                    Log.w(TAG, "Failed to scrobble: ${scrobble.artist} - ${scrobble.title}", e)
                    scrobbleDao.markAttemptFailed(scrobble.id, e.message?.take(500))
                }
            }

            if (successfullySubmitted.isNotEmpty()) {
                scrobbleDao.deletePendingScrobbles(successfullySubmitted)
                Log.d(TAG, "Successfully submitted ${successfullySubmitted.size} scrobbles")
            }

            // Remove records that reached the per-item retry limit.
            scrobbleDao.deleteExhaustedScrobbles(MAX_RETRIES)
            val remainingCount = scrobbleDao.getScrobblesReadyForRetryOnce()
            if (failedCount > 0 && remainingCount > 0) {
                // Network/API failures use WorkManager's exponential backoff.
                return@withContext Result.retry()
            }
            if (remainingCount > 0) {
                // A successful page can be followed by another page without
                // cancelling this worker or creating parallel work.
                enqueue(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing scrobble queue", e)
            Result.retry()
        }
    }
}
