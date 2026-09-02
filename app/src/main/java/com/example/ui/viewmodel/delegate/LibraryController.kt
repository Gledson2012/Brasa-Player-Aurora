package com.example.ui.viewmodel.delegate

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.lyrics.LyricsManager
import com.example.data.lyrics.TrackLyrics
import com.example.data.model.LyricsEntity
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import android.os.Build

enum class SortOption(val title: String) {
    TITLE("Título (A-Z)"),
    ARTIST("Artista (A-Z)"),
    DURATION("Duração"),
    RECENTLY_ADDED("Adicionadas Recentemente")
}

/**
 * Encapsulates all library-related operations: search, sort, import, scan,
 * delete, relink songs, playlist CRUD, and lyrics management.
 */
class LibraryController(
    private val application: Application,
    private val repository: MusicRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "LibraryController"
    }

    // --- Search & Sort ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.TITLE)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = repository.mostPlayed
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylistsWithSongs = repository.allPlaylistsWithSongs
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val displayedSongs: StateFlow<List<Song>> = combine(
        _searchQuery.debounce(250),
        _sortOption
    ) { query, sort ->
        Pair(query.trim(), sort)
    }.flatMapLatest { (query, sort) ->
        repository.searchAndSortSongs(query, sort.name)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    // --- Audio Permission ---
    fun hasAudioPermission(context: Context): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_AUDIO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    // --- Scan & Import ---
    fun scanLocalStorage(context: Context, onMessage: (String?) -> Unit) {
        scope.launch {
            onMessage("Escaneando armazenamento...")
            try {
                val count = repository.scanDeviceAudio(context)
                if (count > 0) {
                    onMessage("$count nova(s) música(s) importada(s)!")
                } else {
                    onMessage("Nenhuma nova música encontrada no dispositivo.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning local storage", e)
                onMessage(e.message ?: "Falha ao escanear as músicas.")
            }
            delay(3500)
            onMessage(null)
        }
    }

    fun importSingleAudio(context: Context, uri: Uri, fileName: String, onMessage: (String?) -> Unit) {
        scope.launch {
            val title = fileName.substringBeforeLast(".")
            try {
                repository.importAudioUri(context, uri, title)
                onMessage("Música \"$title\" importada com sucesso!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error importing audio $uri", e)
                onMessage(e.message ?: "Falha ao importar o arquivo de áudio.")
            }
            delay(3500)
            onMessage(null)
        }
    }

    fun importAudioFiles(context: Context, uris: List<Uri>, onMessage: (String?) -> Unit) {
        if (uris.isEmpty()) return
        scope.launch {
            var importedCount = 0
            var failedCount = 0
            onMessage("Importando ${uris.size} arquivo(s)...")

            uris.forEachIndexed { index, uri ->
                try {
                    val displayName = uri.lastPathSegment
                        ?.substringAfterLast('/')
                        ?.let(Uri::decode)
                        ?.substringBeforeLast('.')
                        ?.takeIf { it.isNotBlank() }
                        ?: "Faixa importada ${index + 1}"
                    repository.importAudioUri(context, uri, displayName)
                    importedCount++
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    failedCount++
                    Log.e(TAG, "Error importing audio $uri", e)
                }
            }

            onMessage(
                when {
                    failedCount == 0 -> "$importedCount música(s) adicionada(s) à biblioteca."
                    importedCount == 0 -> "Não foi possível abrir os arquivos selecionados."
                    else -> "$importedCount música(s) adicionada(s); $failedCount arquivo(s) não puderam ser lidos."
                }
            )
            delay(3500)
            onMessage(null)
        }
    }

    fun importAudioFolder(context: Context, folderUri: Uri, onMessage: (String?) -> Unit) {
        scope.launch {
            onMessage("Lendo a pasta selecionada e suas subpastas...")
            try {
                val result = repository.importAudioFolder(context, folderUri)
                onMessage(
                    when {
                        result.discovered == 0 -> "Nenhum arquivo de áudio encontrado nessa pasta."
                        result.failed == 0 -> "${result.imported} música(s) adicionada(s) à biblioteca."
                        result.imported == 0 -> "Encontramos ${result.discovered} arquivo(s), mas não foi possível importar nenhum."
                        else -> "${result.imported} música(s) adicionada(s); ${result.failed} arquivo(s) não puderam ser lidos."
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error importing audio folder $folderUri", e)
                onMessage(e.message ?: "Falha ao importar a pasta de músicas.")
            }
            delay(3500)
            onMessage(null)
        }
    }

    // --- Delete & Relink ---
    fun deleteSong(song: Song, onMessage: (String?) -> Unit) {
        scope.launch {
            try {
                repository.deleteSong(song)
                LyricsManager.clearCache(song.id)
                onMessage("Música removida da biblioteca.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting song ${song.id}", e)
                onMessage(e.message ?: "Não foi possível remover a música.")
            }
            delay(3500)
            onMessage(null)
        }
    }

    fun relinkSong(context: Context, song: Song, uri: Uri, onMessage: (String?) -> Unit) {
        scope.launch {
            try {
                repository.relinkSong(context, song.id, uri)
                onMessage("Arquivo de \"${song.title}\" atualizado.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error relinking audio ${song.id}", e)
                onMessage(e.message ?: "Não foi possível atualizar o arquivo de áudio.")
            }
            delay(3500)
            onMessage(null)
        }
    }

    // --- Playlists ---
    fun createPlaylist(name: String, description: String = "", iconName: String = "playlist_play", gradientIndex: Int = 0) {
        scope.launch {
            val cleanName = name.trim().take(100)
            if (cleanName.isNotBlank()) {
                repository.createPlaylist(
                    cleanName,
                    description.trim().take(5000),
                    iconName.trim().take(100),
                    gradientIndex.coerceIn(0, 5)
                )
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        scope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        scope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        scope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    // --- Lyrics ---
    private val _currentLyrics = MutableStateFlow<TrackLyrics?>(null)
    val currentLyrics: StateFlow<TrackLyrics?> = _currentLyrics.asStateFlow()

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()

    private val _isLyricsViewActive = MutableStateFlow(false)
    val isLyricsViewActive: StateFlow<Boolean> = _isLyricsViewActive.asStateFlow()

    fun toggleLyricsView() {
        _isLyricsViewActive.value = !_isLyricsViewActive.value
    }

    suspend fun loadLyricsForSong(song: Song): TrackLyrics? {
        if (song.sourceKey?.startsWith("radio:") == true) return null
        return try {
            val saved = repository.getLyricsOnce(song.id)
            if (saved != null) {
                LyricsManager.parseLrc(song.id, saved.content, saved.source)
            } else {
                LyricsManager.getLyrics(song)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error loading lyrics for ${song.title}", e)
            null
        }
    }

    fun refreshLyrics(song: Song) {
        if (song.sourceKey?.startsWith("radio:") == true) return
        scope.launch {
            _isLyricsLoading.value = true
            try {
                if (repository.getLyricsOnce(song.id) == null) {
                    LyricsManager.clearCache(song.id)
                }
                val lyrics = loadLyricsForSong(song)
                _currentLyrics.value = lyrics
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing lyrics", e)
            } finally {
                _isLyricsLoading.value = false
            }
        }
    }

    fun setLyrics(lyrics: TrackLyrics?) {
        _currentLyrics.value = lyrics
    }

    fun setLyricsLoading(loading: Boolean) {
        _isLyricsLoading.value = loading
    }

    fun saveLyrics(song: Song, rawLrc: String, dismissEditor: () -> Unit) {
        if (song.sourceKey?.startsWith("radio:") == true) return
        scope.launch {
            if (rawLrc.isBlank()) {
                repository.deleteLyrics(song.id)
                LyricsManager.clearCache(song.id)
                _currentLyrics.value = TrackLyrics(song.id, emptyList(), false, "Editor")
            } else {
                val parsed = LyricsManager.parseLrc(song.id, rawLrc, "Editor")
                repository.saveLyrics(LyricsEntity(song.id, rawLrc, parsed.isSynced, "Editor"))
                LyricsManager.cache(song.id, parsed)
                _currentLyrics.value = parsed
            }
            dismissEditor()
        }
    }
}
