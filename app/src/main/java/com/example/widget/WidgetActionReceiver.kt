package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.audio.AudioPlayerEngine
import com.example.audio.RepeatMode
import com.example.di.ServiceLocator
import com.example.service.MusicPlaybackService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles button taps from the home screen music widget.
 *
 * When the app process was killed, the engine is recreated without a queue.
 * In that case the receiver restores the last played song from Room before
 * applying the requested action, so the widget still controls playback.
 */
class WidgetActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WidgetActionReceiver"

        const val ACTION_PLAY_PAUSE = "com.aistudio.musicplayer.qtzvka.ACTION_WIDGET_PLAY_PAUSE"
        const val ACTION_NEXT = "com.aistudio.musicplayer.qtzvka.ACTION_WIDGET_NEXT"
        const val ACTION_PREV = "com.aistudio.musicplayer.qtzvka.ACTION_WIDGET_PREV"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "onReceive action=$action")
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        // Ensure the playback service is running before touching the engine.
        MusicPlaybackService.startService(appContext)

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope.launch {
            try {
                val engine = AudioPlayerEngine.getOrCreateInstance(appContext)
                Log.d(TAG, "engine currentSong=${engine.currentSong.value?.title}")
                if (engine.currentSong.value == null) {
                    restoreLastQueue(appContext, engine)
                    Log.d(TAG, "restored currentSong=${engine.currentSong.value?.title}")
                }

                when (action) {
                    ACTION_PLAY_PAUSE -> engine.togglePlayPause()
                    ACTION_NEXT -> engine.playNext()
                    ACTION_PREV -> engine.playPrevious()
                }

                // Force widget update after action
                MusicWidgetProvider.updateAllWidgets(appContext)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Could not handle widget action $action", e)
            } finally {
                pendingResult.finish()
                scope.cancel()
            }
        }
    }

    private suspend fun restoreLastQueue(
        context: Context,
        engine: AudioPlayerEngine
    ) {
        val container = ServiceLocator.get(context)
        val (songs, settings) = withContext(Dispatchers.IO) {
            container.repository.getAllSongsOnce() to
                container.repository.getUserSettingsOnce()
        }
        Log.d(TAG, "restoreLastQueue songs=${songs.size} lastPlayed=${settings.lastPlayedSongId}")
        if (songs.isEmpty()) return

        val savedIndex = settings.lastPlayedSongId
            ?.let { id -> songs.indexOfFirst { it.id == id }.takeIf { it >= 0 } }
            ?: 0
        engine.setQueue(songs, startIndex = savedIndex, autoPlay = false)
        engine.setPlaybackSpeed(settings.playbackSpeed)
        engine.setCrossfadeSeconds(settings.crossfadeSeconds)
        engine.setRepeatMode(
            runCatching { RepeatMode.valueOf(settings.repeatMode) }
                .getOrDefault(RepeatMode.ALL)
        )
        engine.setShuffleMode(settings.isShuffle)

        val restored = songs.getOrNull(savedIndex)
        if (settings.lastPlayedSongId != null && restored?.id == settings.lastPlayedSongId) {
            engine.seekTo(settings.lastPlaybackPositionMs)
        }
    }
}
