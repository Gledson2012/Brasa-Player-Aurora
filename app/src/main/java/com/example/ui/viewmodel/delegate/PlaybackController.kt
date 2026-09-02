package com.example.ui.viewmodel.delegate

import android.app.Application
import android.net.Uri
import android.util.Log
import com.example.audio.AudioPlayerEngine
import com.example.audio.RepeatMode
import com.example.data.model.Song
import com.example.data.radio.RadiosStreamResolver
import com.example.data.repository.MusicRepository
import com.example.service.MusicPlaybackService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Encapsulates all playback-related operations that were previously scattered
 * across MusicViewModel: play/pause, queue management, seek, repeat/shuffle,
 * sleep timer, radio playback, and playback position persistence.
 */
class PlaybackController(
    private val application: Application,
    val playerEngine: AudioPlayerEngine,
    private val repository: MusicRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "PlaybackController"
    }

    // --- State exposed from player engine ---
    val isPlaying: StateFlow<Boolean> = playerEngine.isPlaying
    val currentSong: StateFlow<Song?> = playerEngine.currentSong
    val currentPositionMs: StateFlow<Long> = playerEngine.currentPositionMs
    val durationMs: StateFlow<Long> = playerEngine.durationMs
    val queue: StateFlow<List<Song>> = playerEngine.queue
    val repeatMode: StateFlow<RepeatMode> = playerEngine.repeatMode
    val isShuffle: StateFlow<Boolean> = playerEngine.isShuffle
    val playbackSpeed: StateFlow<Float> = playerEngine.playbackSpeed
    val crossfadeSeconds: StateFlow<Int> = playerEngine.crossfadeSeconds
    val visualizerAmplitudes: StateFlow<FloatArray> = playerEngine.visualizerAmplitudes
    val waveformSamples: StateFlow<List<Float>> = playerEngine.waveformSamples
    val playbackError: StateFlow<String?> = playerEngine.playbackError
    val sleepTimerRemainingSeconds: StateFlow<Int?> = playerEngine.sleepTimerRemainingSeconds
    val sleepTimerEndAtTrackEnd: StateFlow<Boolean> = playerEngine.sleepTimerEndAtTrackEnd

    private var persistJob: Job? = null
    private var radioPlaybackJob: Job? = null

    // Callbacks to ViewModel for state updates
    var onScanStatusMessage: ((String?) -> Unit)? = null
    var onFullPlayerOpen: (() -> Unit)? = null

    fun ensurePlaybackService() {
        MusicPlaybackService.startService(application)
    }

    // --- Playback Controls ---
    fun playSongFromList(songs: List<Song>, startIndex: Int) {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.setQueue(songs, startIndex = startIndex, autoPlay = true)
    }

    fun playSong(song: Song) {
        ensurePlaybackService()
        val currentQueue = queue.value
        if (currentQueue.none { it.id == song.id }) {
            playerEngine.setQueue(listOf(song) + currentQueue, 0, true)
        } else {
            playerEngine.playSong(song)
        }
    }

    fun togglePlayPause() {
        ensurePlaybackService()
        playerEngine.togglePlayPause()
        schedulePersistPlaybackPosition()
    }

    fun skipToNext() {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.playNext()
    }

    fun playNext() {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.playNext()
    }

    fun skipToPrevious() {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.playPrevious()
    }

    fun playPrevious() {
        ensurePlaybackService()
        schedulePersistPlaybackPosition()
        playerEngine.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        ensurePlaybackService()
        playerEngine.seekTo(positionMs)
        schedulePersistPlaybackPosition()
    }

    fun clearPlaybackError() {
        playerEngine.clearPlaybackError()
    }

    fun toggleRepeatMode() {
        playerEngine.cycleRepeatMode()
    }

    fun cycleRepeatMode() {
        toggleRepeatMode()
    }

    fun toggleShuffle() {
        playerEngine.toggleShuffle()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerEngine.setPlaybackSpeed(speed)
    }

    fun setCrossfadeSeconds(seconds: Int) {
        playerEngine.setCrossfadeSeconds(seconds)
    }

    fun toggleFavorite(song: Song) {
        if (isRadioSong(song)) return
        scope.launch {
            repository.toggleFavorite(song)
        }
    }

    // --- Sleep Timer ---
    fun setSleepTimer(minutes: Int) {
        playerEngine.setSleepTimer(minutes)
    }

    fun setSleepTimerUntilTrackEnd() {
        playerEngine.setSleepTimerUntilTrackEnd()
    }

    fun cancelSleepTimer() {
        playerEngine.cancelSleepTimer()
    }

    // --- Radio Playback ---
    fun playRadio(
        title: String,
        category: String,
        coverUri: String,
        radioId: String?,
        streamUrl: String? = null
    ) {
        radioPlaybackJob?.cancel()
        radioPlaybackJob = scope.launch {
            onScanStatusMessage?.invoke("Conectando a $title…")
            val sourceUrl = streamUrl?.takeIf {
                it.startsWith("http://") || it.startsWith("https://")
            }
            val playableStreamUrl = sourceUrl?.let { RadiosStreamResolver.resolve(it) }
            if (playableStreamUrl.isNullOrBlank()) {
                onScanStatusMessage?.invoke("A estação \"$title\" não está disponível para reprodução agora.")
                delay(3500)
                onScanStatusMessage?.invoke(null)
                return@launch
            }

            ensurePlaybackService()
            val stationKey = radioId ?: streamUrl ?: title
            val radioSong = Song(
                id = radioSongId(stationKey),
                title = title,
                artist = "Radios.com.br • $category",
                album = "Rádio ao vivo",
                durationMs = 0L,
                mediaUri = playableStreamUrl,
                coverUri = coverUri,
                genre = category,
                sourceKey = "radio:$stationKey",
                isAvailable = true
            )
            playerEngine.setQueue(listOf(radioSong), startIndex = 0, autoPlay = true)
            onFullPlayerOpen?.invoke()
            onScanStatusMessage?.invoke(null)
        }
    }

    fun isRadioSong(song: Song): Boolean = song.sourceKey?.startsWith("radio:") == true

    private fun radioSongId(radioId: String): Long {
        val numericId = radioId.filter { it.isDigit() }.toLongOrNull()
        val stableId = numericId?.coerceAtLeast(1L)
            ?: kotlin.math.abs(radioId.hashCode().toLong()).coerceAtLeast(1L)
        return -stableId
    }

    // --- Position Persistence ---
    suspend fun persistPlaybackPosition() {
        val song = currentSong.value ?: return
        if (isRadioSong(song)) return
        repository.updateLastPlayedState(song.id, currentPositionMs.value)
    }

    fun schedulePersistPlaybackPosition() {
        val song = currentSong.value ?: return
        if (isRadioSong(song)) return
        val position = currentPositionMs.value
        scope.launch {
            repository.updateLastPlayedState(song.id, position)
        }
    }

    fun startPositionPersistenceLoop() {
        persistJob?.cancel()
        persistJob = scope.launch {
            while (true) {
                delay(10_000)
                persistPlaybackPosition()
            }
        }
    }

    // --- Metadata ---
    fun updateSongMetadata(
        song: Song,
        title: String,
        artist: String,
        album: String,
        genre: String,
        coverUri: String?
    ) {
        val updatedSong = song.copy(
            title = title.trim().ifBlank { song.title },
            artist = artist.trim().ifBlank { "Artista Desconhecido" },
            album = album.trim().ifBlank { "Álbum Desconhecido" },
            genre = genre.trim().ifBlank { "Geral" },
            coverUri = coverUri?.trim()?.takeIf { it.isNotEmpty() }
        )
        scope.launch {
            repository.updateSong(updatedSong)
            playerEngine.updateSongMetadata(updatedSong)
        }
    }

    fun onCleared() {
        persistJob?.cancel()
        radioPlaybackJob?.cancel()
    }
}
