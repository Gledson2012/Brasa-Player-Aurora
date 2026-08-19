package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.MainActivity
import com.aistudio.musicplayer.qtzvka.R
import com.example.audio.AudioPlayerEngine
import com.example.data.db.AppDatabase
import com.example.data.model.Song
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Media3 library service for lock-screen controls, notifications and Android Auto browsing.
 */
class MusicPlaybackService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibraryService.MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var librarySongs: List<Song> = emptyList()

    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_FAVORITE = "com.aistudio.musicplayer.qtzvka.service.ACTION_FAVORITE"
        const val ACTION_REPEAT_MODE = "com.aistudio.musicplayer.qtzvka.service.ACTION_REPEAT_MODE"
        const val ACTION_SHUFFLE_MODE = "com.aistudio.musicplayer.qtzvka.service.ACTION_SHUFFLE_MODE"
        private const val ROOT_ID = "root"
        private const val LIBRARY_ID = "library"
        private val ALLOWED_CONTROLLER_PACKAGES = setOf(
            "com.google.android.projection.gearhead", // Android Auto
            "com.android.systemui", // Lock-screen / media controls
            "com.android.settings", // Settings / bluetooth pairing UI
            "com.google.android.apps.music", // Google Assistant media intents
            "com.google.android.googlequicksearchbox" // Google Assistant
        )

        fun startService(context: Context) {
            val intent = Intent(context, MusicPlaybackService::class.java)
            try {
                // Media3's MediaSessionService promotes itself to a foreground
                // service with a media notification as soon as playback starts.
                // Using startForegroundService() here can crash the app with
                // ForegroundServiceDidNotStartInTimeException because the service
                // may not call startForeground() within the system's 5s window
                // when the player is still empty/preparing. Plain startService()
                // lets Media3 handle the foreground promotion safely.
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("MusicPlaybackService", "Error starting MusicPlaybackService", e)
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        try {
            initializePlaybackSession()
        } catch (e: Exception) {
            Log.e("MusicPlaybackService", "Could not initialize playback service", e)
            mediaLibrarySession?.release()
            mediaLibrarySession = null
            stopSelf()
        }
    }

    /**
     * Safeguard for the rare case where Media3 has not yet published its media
     * notification (e.g. the player is empty when the service starts). Without a
     * foreground notification, Android would kill the process for services that
     * were started in the foreground. This call is a no-op once the session is
     * active but guarantees the 5-second window is never exceeded.
     */
    @OptIn(UnstableApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val notification = createIdleNotification()
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w("MusicPlaybackService", "Could not start foreground immediately: ${e.message}")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createIdleNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.playback_notification_idle_text))
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()
    }

    @OptIn(UnstableApi::class)
    private fun initializePlaybackSession() {
        createNotificationChannel()

        val engine = AudioPlayerEngine.getOrCreateInstance(applicationContext)
        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaLibrarySession = MediaLibraryService.MediaLibrarySession.Builder(
            this,
            engine.getExoPlayer(),
            MediaLibraryCallback(engine)
        )
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.playback_notification_channel_name)
            .build()
        setMediaNotificationProvider(notificationProvider)
        engine.attachMediaSession(mediaLibrarySession)
        refreshLibrary()
    }

    private fun refreshLibrary() {
        serviceScope.launch {
            try {
                AppDatabase.getInstance(applicationContext).songDao().getAllSongs()
                    .collectLatest { songs ->
                        withContext(Dispatchers.Main) {
                            librarySongs = songs
                            AudioPlayerEngine.getExistingInstance()?.setLibrarySongs(songs)
                            mediaLibrarySession?.notifyChildrenChanged(LIBRARY_ID, songs.size, null)
                        }
                    }
            } catch (error: Exception) {
                Log.e("MusicPlaybackService", "Could not observe music library", error)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.playback_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.playback_notification_channel_desc)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibraryService.MediaLibrarySession? =
        mediaLibrarySession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaLibrarySession?.player
        val engineIsPlaying = AudioPlayerEngine.getExistingInstance()?.isPlaying?.value == true
        // Synthetic tracks are rendered by AudioTrack, so ExoPlayer's
        // playWhenReady flag alone is not authoritative for them.
        if (player == null || player.mediaItemCount == 0 || (!player.playWhenReady && !engineIsPlaying)) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        AudioPlayerEngine.getExistingInstance()?.detachMediaSession()
        mediaLibrarySession?.release()
        mediaLibrarySession = null
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun browsableItem(id: String, title: String): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setUri(Uri.EMPTY)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .build()
            )
            .build()

    private inner class MediaLibraryCallback(
        private val engine: AudioPlayerEngine
    ) : MediaLibraryService.MediaLibrarySession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            // Only accept controllers we trust: our own app and well-known system/media
            // packages. The packageName may be null/blank for system-level controllers,
            // so those may be allowed as a fallback.
            val isOwnApp = controller.uid == android.os.Process.myUid()
            val isTrustedPackage = !controller.packageName.isNullOrBlank() &&
                (controller.packageName in ALLOWED_CONTROLLER_PACKAGES)
            val isSystemFallback = controller.packageName.isNullOrBlank()
            if (!isOwnApp && !isTrustedPackage && !isSystemFallback) {
                return MediaSession.ConnectionResult.reject()
            }
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands = connectionResult.availableSessionCommands.buildUpon()
                .add(SessionCommand(ACTION_FAVORITE, Bundle.EMPTY))
                .add(SessionCommand(ACTION_REPEAT_MODE, Bundle.EMPTY))
                .add(SessionCommand(ACTION_SHUFFLE_MODE, Bundle.EMPTY))
                .build()
            val playerCommands = connectionResult.availablePlayerCommands.buildUpon()
                .add(Player.COMMAND_SET_REPEAT_MODE)
                .add(Player.COMMAND_SET_SHUFFLE_MODE)
                .build()
            return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
        }

        override fun onGetLibraryRoot(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> = Futures.immediateFuture(
            LibraryResult.ofItem(browsableItem(ROOT_ID, "Music Player"), params)
        )

        override fun onGetChildren(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val items = when (parentId) {
                ROOT_ID -> listOf(browsableItem(LIBRARY_ID, "Músicas"))
                LIBRARY_ID -> (librarySongs.ifEmpty { engine.queue.value }).map(engine::buildMediaItem)
                else -> emptyList()
            }
            val from = (page * pageSize).coerceAtMost(items.size)
            val to = (from + pageSize).coerceAtMost(items.size)
            return Futures.immediateFuture(LibraryResult.ofItemList(items.subList(from, to), params))
        }

        override fun onGetItem(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val item = when (mediaId) {
                ROOT_ID -> browsableItem(ROOT_ID, "Music Player")
                LIBRARY_ID -> browsableItem(LIBRARY_ID, "Músicas")
                else -> (librarySongs.ifEmpty { engine.queue.value })
                    .firstOrNull { it.id.toString() == mediaId }
                    ?.let(engine::buildMediaItem)
            }
            return if (item != null) {
                Futures.immediateFuture(LibraryResult.ofItem(item, null))
            } else {
                Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
            }
        }

        override fun onSearch(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> = Futures.immediateFuture(LibraryResult.ofVoid(params))

        override fun onGetSearchResult(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val normalized = query.trim().lowercase()
            val items = (librarySongs.ifEmpty { engine.queue.value })
                .filter { it.title.lowercase().contains(normalized) || it.artist.lowercase().contains(normalized) }
                .map(engine::buildMediaItem)
            val from = (page * pageSize).coerceAtMost(items.size)
            val to = (from + pageSize).coerceAtMost(items.size)
            return Futures.immediateFuture(LibraryResult.ofItemList(items.subList(from, to), params))
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_FAVORITE -> engine.currentSong.value?.let(engine::triggerFavoriteToggle)
                ACTION_REPEAT_MODE -> engine.cycleRepeatMode()
                ACTION_SHUFFLE_MODE -> engine.toggleShuffle()
                else -> return super.onCustomCommand(session, controller, customCommand, args)
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val currentSong = engine.currentSong.value ?: return super.onPlaybackResumption(mediaSession, controller)
            val item = engine.buildMediaItem(currentSong)
            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(
                    listOf(item),
                    0,
                    engine.currentPositionMs.value
                )
            )
        }
    }
}
