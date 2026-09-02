package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.RemoteViews
import com.aistudio.musicplayer.qtzvka.R
import com.example.audio.AudioPlayerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Home screen widget showing current song info and playback controls.
 * Updates reactively when the song changes via StateFlow collection.
 */
class MusicWidgetProvider : AppWidgetProvider() {

    companion object {
        private var scope: CoroutineScope? = null

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isNotEmpty()) {
                val intent = Intent(context, MusicWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
        startObserving(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        startObserving(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        scope?.let {
            // Only cancel if no widgets remain
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicWidgetProvider::class.java)
            if (appWidgetManager.getAppWidgetIds(componentName).isEmpty()) {
                (it as? kotlinx.coroutines.CoroutineScope)?.let { /* scope.cancel() */ }
            }
        }
    }

    private fun startObserving(context: Context) {
        if (scope != null) return

        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val engine = AudioPlayerEngine.getExistingInstance() ?: return

        scope?.launch {
            engine.currentSong.collectLatest {
                updateAllWidgets(context)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_music_player)
        val engine = AudioPlayerEngine.getExistingInstance()
        val song = engine?.currentSong?.value
        val isPlaying = engine?.isPlaying?.value == true

        // Update song info
        if (song != null) {
            views.setTextViewText(R.id.widget_title, song.title)
            views.setTextViewText(R.id.widget_artist, song.artist)

            // Try to load album art
            val coverUri = song.coverUri
            if (!coverUri.isNullOrBlank()) {
                try {
                    val bitmap: Bitmap? = context.contentResolver.openInputStream(Uri.parse(coverUri))?.use { stream ->
                        android.graphics.BitmapFactory.decodeStream(stream)
                    }
                    if (bitmap != null) {
                        views.setImageViewBitmap(R.id.widget_album_art, bitmap)
                    } else {
                        views.setImageViewResource(R.id.widget_album_art, R.drawable.cover_synthwave)
                    }
                } catch (_: Exception) {
                    views.setImageViewResource(R.id.widget_album_art, R.drawable.cover_synthwave)
                }
            } else {
                val drawableId = when (song.coverDrawableName) {
                    "cover_synthwave" -> R.drawable.cover_synthwave
                    "cover_lofi" -> R.drawable.cover_lofi
                    "cover_acoustic" -> R.drawable.cover_acoustic
                    "cover_electronic" -> R.drawable.cover_electronic
                    else -> R.drawable.cover_synthwave
                }
                views.setImageViewResource(R.id.widget_album_art, drawableId)
            }
        } else {
            views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_no_song))
            views.setTextViewText(R.id.widget_artist, context.getString(R.string.widget_default_artist))
            views.setImageViewResource(R.id.widget_album_art, R.drawable.cover_synthwave)
        }

        // Play/Pause button
        if (isPlaying) {
            views.setImageViewResource(R.id.widget_play_pause, android.R.drawable.ic_media_pause)
        } else {
            views.setImageViewResource(R.id.widget_play_pause, android.R.drawable.ic_media_play)
        }

        // Button PendingIntents
        views.setOnClickPendingIntent(
            R.id.widget_play_pause,
            createActionIntent(context, WidgetActionReceiver.ACTION_PLAY_PAUSE)
        )
        views.setOnClickPendingIntent(
            R.id.widget_next,
            createActionIntent(context, WidgetActionReceiver.ACTION_NEXT)
        )
        views.setOnClickPendingIntent(
            R.id.widget_prev,
            createActionIntent(context, WidgetActionReceiver.ACTION_PREV)
        )

        // Tap on the widget opens the app
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingLaunch = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_album_art, pendingLaunch)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun createActionIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
