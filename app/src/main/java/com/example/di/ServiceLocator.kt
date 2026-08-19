package com.example.di

import android.app.Application
import android.content.Context
import com.example.audio.AudioPlayerEngine
import com.example.audio.CoverArtCache
import com.example.data.backup.BackupManager
import com.example.data.datastore.LastFmPreferencesDataStore
import com.example.data.datastore.ThemePreferencesDataStore
import com.example.data.db.AppDatabase
import com.example.data.lastfm.ScrobbleQueueManager
import com.example.data.repository.MusicRepository
import com.example.ui.viewmodel.MusicViewModel

/**
 * Application-scoped dependencies used by the app.
 *
 * This keeps construction explicit and avoids annotation processing that is not
 * compatible with the current Android Gradle Plugin setup.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    val repository: MusicRepository by lazy {
        MusicRepository(
            database = database,
            songDao = database.songDao(),
            playlistDao = database.playlistDao(),
            userSettingsDao = database.userSettingsDao(),
            lyricsDao = database.lyricsDao()
        )
    }

    val themeDataStore: ThemePreferencesDataStore by lazy {
        ThemePreferencesDataStore(appContext)
    }

    val lastFmDataStore: LastFmPreferencesDataStore by lazy {
        LastFmPreferencesDataStore(appContext)
    }

    val backupManager: BackupManager by lazy {
        BackupManager(repository, themeDataStore)
    }

    val audioPlayerEngine: AudioPlayerEngine by lazy {
        AudioPlayerEngine.getOrCreateInstance(appContext)
    }

    val scrobbleQueueManager: ScrobbleQueueManager by lazy {
        ScrobbleQueueManager(
            scrobbleDao = database.scrobbleDao(),
            lastFmDataStore = lastFmDataStore,
            context = appContext
        )
    }

    val coverArtCache: CoverArtCache by lazy {
        CoverArtCache(appContext)
    }

    fun musicViewModelFactory(application: Application): MusicViewModel.Factory =
        MusicViewModel.Factory(
            application = application,
            repository = repository,
            themeDataStore = themeDataStore,
            lastFmDataStore = lastFmDataStore,
            backupManager = backupManager,
            playerEngine = audioPlayerEngine
        )
}

object ServiceLocator {
    @Volatile
    private var appContainer: AppContainer? = null

    fun initialize(context: Context) {
        get(context)
    }

    fun get(context: Context): AppContainer {
        return appContainer ?: synchronized(this) {
            appContainer ?: AppContainer(context).also { appContainer = it }
        }
    }
}
