package com.example.ui.viewmodel.delegate

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.data.backup.BackupManager
import com.example.data.datastore.LastFmPreferencesDataStore
import com.example.data.datastore.ThemePreferencesDataStore
import com.example.data.lastfm.LastFmClient
import com.example.data.lastfm.ScrobbleQueueManager
import com.example.data.model.AlbumArtStyle
import com.example.data.model.AppThemeType
import com.example.data.model.LastFmSettings
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeMode
import com.example.data.model.VisualizerStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Encapsulates all settings-related operations: theme management,
 * Last.fm authentication/scrobbling, backup/restore, and DataStore persistence.
 */
class SettingsController(
    private val application: Application,
    private val themeDataStore: ThemePreferencesDataStore,
    private val lastFmDataStore: LastFmPreferencesDataStore,
    private val backupManager: BackupManager,
    private val scrobbleQueueManager: ScrobbleQueueManager,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "SettingsController"
    }

    // --- Themes ---
    val themeConfig: StateFlow<ThemeConfig> = themeDataStore.themeConfigFlow
        .stateIn(scope, SharingStarted.Eagerly, ThemeConfig())

    val themeSettings: StateFlow<ThemeConfig> = themeConfig

    val onboardingCompleted: StateFlow<Boolean> = themeDataStore.onboardingCompletedFlow
        .stateIn(scope, SharingStarted.Eagerly, false)

    fun completeOnboarding() {
        scope.launch {
            themeDataStore.setOnboardingCompleted(true)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        scope.launch {
            themeDataStore.setThemeMode(mode)
        }
    }

    fun setPresetTheme(theme: AppThemeType) {
        scope.launch {
            themeDataStore.setPresetTheme(theme)
        }
    }

    fun setCustomTheme(
        primary: Color,
        secondary: Color,
        tertiary: Color = Color(0xFFFF007F),
        surface: Color,
        background: Color,
        isDark: Boolean = true
    ) {
        scope.launch {
            themeDataStore.setCustomTheme(primary, secondary, tertiary, surface, background, isDark)
            themeDataStore.setThemeMode(ThemeMode.CUSTOM)
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        scope.launch {
            themeDataStore.setDynamicColors(enabled)
        }
    }

    fun setVisualizerStyle(style: VisualizerStyle) {
        scope.launch {
            themeDataStore.setVisualizerStyle(style)
        }
    }

    fun setAlbumArtStyle(style: AlbumArtStyle) {
        scope.launch {
            themeDataStore.setAlbumArtStyle(style)
        }
    }

    fun resetThemeSettings() {
        scope.launch {
            themeDataStore.resetToDefault()
        }
    }

    // --- Last.fm ---
    val lastFmSettings: StateFlow<LastFmSettings> = lastFmDataStore.settingsFlow
        .stateIn(scope, SharingStarted.Eagerly, LastFmSettings())

    val pendingScrobbleCount: StateFlow<Int> = scrobbleQueueManager.pendingCount
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)

    private val _lastFmMessage = MutableStateFlow<String?>(null)
    val lastFmMessage: StateFlow<String?> = _lastFmMessage.asStateFlow()

    private val _lastFmAuthUrl = MutableStateFlow<String?>(null)
    val lastFmAuthUrl: StateFlow<String?> = _lastFmAuthUrl.asStateFlow()

    private fun setLastFmMessage(message: String?) {
        _lastFmMessage.value = message
    }

    fun requestLastFmAuthorization() {
        val settings = lastFmSettings.value
        if (settings.apiKey.isBlank() || settings.apiSecret.isBlank()) {
            setLastFmMessage("Informe a API key e o API secret antes de autorizar.")
            return
        }
        scope.launch {
            try {
                val token = LastFmClient(settings).requestToken()
                lastFmDataStore.saveAuthToken(token)
                _lastFmAuthUrl.value = LastFmClient.authorizationUrl(settings.apiKey, token)
                setLastFmMessage("Abra a página de autorização e depois conclua o login.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setLastFmMessage("Não foi possível obter o token: ${e.message ?: "erro desconhecido"}")
            }
        }
    }

    fun completeLastFmAuthorization() {
        val settings = lastFmSettings.value
        if (settings.authToken.isBlank()) {
            setLastFmMessage("Solicite um token e autorize o aplicativo primeiro.")
            return
        }
        scope.launch {
            try {
                val session = LastFmClient(settings).getSession(settings.authToken)
                lastFmDataStore.saveSession(session.username, session.sessionKey)
                _lastFmAuthUrl.value = null
                setLastFmMessage("Last.fm conectado como ${session.username}.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setLastFmMessage("Não foi possível concluir o login: ${e.message ?: "erro desconhecido"}")
            }
        }
    }

    fun saveLastFmCredentials(apiKey: String, apiSecret: String) {
        scope.launch {
            lastFmDataStore.saveCredentials(apiKey, apiSecret)
            setLastFmMessage("Credenciais salvas. Solicite um token para conectar.")
        }
    }

    fun setLastFmEnabled(enabled: Boolean) {
        scope.launch {
            lastFmDataStore.setEnabled(enabled)
            if (enabled && lastFmSettings.value.isAuthenticated) {
                scrobbleQueueManager.processPendingScrobbles()
            }
        }
    }

    fun disconnectLastFm() {
        scope.launch {
            lastFmDataStore.clear()
            _lastFmAuthUrl.value = null
            scrobbleQueueManager.clearPendingScrobbles()
            setLastFmMessage("Last.fm desconectado.")
        }
    }

    fun clearPendingScrobbles() {
        scope.launch {
            scrobbleQueueManager.clearPendingScrobbles()
            setLastFmMessage("Fila de scrobbles limpa.")
        }
    }

    fun processPendingScrobbles() {
        scope.launch {
            scrobbleQueueManager.processPendingScrobbles()
            setLastFmMessage("Processando scrobbles pendentes...")
        }
    }

    // --- Backup ---
    fun exportBackup(context: Application, uri: Uri, onMessage: (String?) -> Unit) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    backupManager.export(context, uri) { message ->
                        scope.launch(Dispatchers.Main) {
                            onMessage(message)
                        }
                    }
                }
                onMessage("Backup salvo com sucesso.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onMessage("Falha no backup: ${e.message ?: "arquivo inválido"}")
            }
            delay(3500)
            onMessage(null)
        }
    }

    fun restoreBackup(context: Application, uri: Uri, onMessage: (String?) -> Unit): List<com.example.data.model.Song>? {
        var songs: List<com.example.data.model.Song>? = null
        scope.launch {
            try {
                songs = withContext(Dispatchers.IO) {
                    backupManager.restore(context, uri) { message ->
                        scope.launch(Dispatchers.Main) {
                            onMessage(message)
                        }
                    }
                }
                onMessage("Backup restaurado: ${songs?.size ?: 0} música(s).")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onMessage("Falha na restauração: ${e.message ?: "arquivo inválido"}")
            }
            delay(3500)
            onMessage(null)
        }
        return songs
    }
}
