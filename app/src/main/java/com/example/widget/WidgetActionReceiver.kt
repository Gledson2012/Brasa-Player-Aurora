package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.audio.AudioPlayerEngine
import com.example.service.MusicPlaybackService

/**
 * Handles button taps from the home screen music widget.
 */
class WidgetActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PLAY_PAUSE = "com.aistudio.musicplayer.qtzvka.ACTION_WIDGET_PLAY_PAUSE"
        const val ACTION_NEXT = "com.aistudio.musicplayer.qtzvka.ACTION_WIDGET_NEXT"
        const val ACTION_PREV = "com.aistudio.musicplayer.qtzvka.ACTION_WIDGET_PREV"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Ensure the playback service is running
        MusicPlaybackService.startService(context)

        val engine = AudioPlayerEngine.getExistingInstance() ?: return

        when (intent.action) {
            ACTION_PLAY_PAUSE -> engine.togglePlayPause()
            ACTION_NEXT -> engine.playNext()
            ACTION_PREV -> engine.playPrevious()
        }

        // Force widget update after action
        MusicWidgetProvider.updateAllWidgets(context)
    }
}
