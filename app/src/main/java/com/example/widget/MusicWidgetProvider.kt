package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.RemoteViews
import com.aistudio.musicplayer.qtzvka.R
import com.example.MainActivity
import com.example.audio.AudioPlayerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Home screen widget showing current song info and playback controls.
 * Updates reactively when the song changes via StateFlow collection.
 */
class MusicWidgetProvider : AppWidgetProvider() {

    companion object {
        private var scope: CoroutineScope? = null
        private var observationJob: Job? = null

        private fun getScope(): CoroutineScope =
            scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main)
                .also { scope = it }

        /**
         * Starts reactive widget updates if a widget exists and the playback
         * engine is already alive. Safe to call repeatedly.
         */
        fun ensureObserving(context: Context) {
            if (observationJob?.isActive == true) return

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicWidgetProvider::class.java)
            if (appWidgetManager.getAppWidgetIds(componentName).isEmpty()) return

            val engine = AudioPlayerEngine.getExistingInstance() ?: return
            observationJob = getScope().launch {
                combine(engine.currentSong, engine.isPlaying) { song, playing -> song to playing }
                    .collectLatest {
                        updateAllWidgets(context)
                    }
            }
        }

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
        getScope().launch {
            for (widgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, widgetId)
            }
        }
        ensureObserving(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ensureObserving(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        observationJob = null
        scope?.cancel()
        scope = null
    }

    private suspend fun updateWidget(
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

            // Try to load album art. Decode off the main thread and downsample
            // large artwork: RemoteViews bitmaps travel through Binder and can
            // otherwise exceed the transaction limit or cause an OOM.
            val coverUri = song.coverUri
            if (!coverUri.isNullOrBlank()) {
                val bitmap = withContext(Dispatchers.IO) {
                    runCatching { decodeWidgetArtwork(context, Uri.parse(coverUri)) }.getOrNull()
                }
                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.widget_album_art, bitmap)
                } else {
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
            ?: Intent(context, MainActivity::class.java)
        val pendingLaunch = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_album_art, pendingLaunch)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun decodeWidgetArtwork(context: Context, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (
            bounds.outWidth / sampleSize > 256 ||
            bounds.outHeight / sampleSize > 256
        ) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    private fun createActionIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            this.action = action
            // A force-stopped app is in the "stopped" package state. Without
            // this flag the system silently drops the widget broadcast and the
            // home screen controls cannot cold-start playback.
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        }
        return PendingIntent.getBroadcast(
            context, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
