package com.example.audio

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.data.model.EqualizerState
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

class AudioPlayerEngine(
    private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private var forwardingPlayer: ForwardingPlayer? = null
    private var mediaSession: MediaSession? = null

    private val synthGenerator = ProceduralAudioGenerator()
    val equalizerEngine = EqualizerEngine()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()
    private var librarySongs: List<Song> = emptyList()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _crossfadeSeconds = MutableStateFlow(0)
    val crossfadeSeconds: StateFlow<Int> = _crossfadeSeconds.asStateFlow()

    private val _visualizerAmplitudes = MutableStateFlow(FloatArray(32) { 0.1f })
    val visualizerAmplitudes: StateFlow<FloatArray> = _visualizerAmplitudes.asStateFlow()

    private val _waveformSamples = MutableStateFlow(List(96) { 0.12f })
    val waveformSamples: StateFlow<List<Float>> = _waveformSamples.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()
    private val waveformCapture = AudioWaveformCapture { samples -> _waveformSamples.value = samples }

    // Sleep Timer Engine States
    private val _sleepTimerRemainingSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSeconds: StateFlow<Int?> = _sleepTimerRemainingSeconds.asStateFlow()

    private val _sleepTimerEndAtTrackEnd = MutableStateFlow(false)
    val sleepTimerEndAtTrackEnd: StateFlow<Boolean> = _sleepTimerEndAtTrackEnd.asStateFlow()

    private var sleepTimerJob: Job? = null
    private val engineJob = SupervisorJob()
    private val engineScope = CoroutineScope(Dispatchers.Main + engineJob)

    private var progressTickerJob: Job? = null
    private var crossfadeTransitionJob: Job? = null
    private var fadeInJob: Job? = null
    private var isCrossfadeTransitioning = false
    private var fadeInNextTrack = false
    private var isUsingSynth = false

    private var onSongChangedCallback: ((Song) -> Unit)? = null
    private var onFavoriteToggleCallback: ((Song) -> Unit)? = null

    companion object {
        @Volatile
        private var INSTANCE: AudioPlayerEngine? = null

        fun getOrCreateInstance(context: Context): AudioPlayerEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioPlayerEngine(
                    context.applicationContext
                ).also { INSTANCE = it }
            }
        }

        fun getExistingInstance(): AudioPlayerEngine? = INSTANCE
    }

    init {
        initExoPlayer()
    }

    private fun initExoPlayer(): ExoPlayer {
        val existing = exoPlayer
        if (existing != null) return existing

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> if (!isUsingSynth) handleTrackCompletion()
                    Player.STATE_READY -> {
                        val dur = player.duration
                        if (dur > 0) {
                            _durationMs.value = dur
                        }
                    }
                    Player.STATE_BUFFERING -> {}
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isUsingSynth) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startProgressTicker()
                    } else {
                        progressTickerJob?.cancel()
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("AudioPlayerEngine", "ExoPlayer error: ${error.errorCodeName}", error)
                // Synthetic demo tracks use ExoPlayer only as a metadata bridge. Their
                // empty MediaItem can fail without meaning that the generated audio failed.
                if (!isUsingSynth) {
                    reportPlaybackError("Não foi possível reproduzir esta faixa.")
                }
            }
        })

        exoPlayer = player
        // Queue navigation and repeat behavior are implemented by this engine.
        // ExoPlayer only contains the currently playing item, so allowing it to
        // repeat that item would bypass playNext()/handleTrackCompletion().
        player.repeatMode = Player.REPEAT_MODE_OFF

        // Build ForwardingPlayer to bridge system MediaSession commands
        forwardingPlayer = object : ForwardingPlayer(player) {
            override fun play() {
                this@AudioPlayerEngine.resume()
            }

            override fun pause() {
                this@AudioPlayerEngine.pause()
            }

            override fun seekToNext() {
                this@AudioPlayerEngine.playNext()
            }

            override fun seekToNextMediaItem() {
                this@AudioPlayerEngine.playNext()
            }

            override fun seekToPrevious() {
                this@AudioPlayerEngine.playPrevious()
            }

            override fun seekToPreviousMediaItem() {
                this@AudioPlayerEngine.playPrevious()
            }

            override fun seekTo(positionMs: Long) {
                this@AudioPlayerEngine.seekTo(positionMs)
            }

            override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
                this@AudioPlayerEngine.seekTo(positionMs)
            }

            override fun stop() {
                this@AudioPlayerEngine.pause()
            }

            override fun setMediaItem(mediaItem: MediaItem) {
                if (!this@AudioPlayerEngine.playMediaItem(mediaItem.mediaId)) {
                    super.setMediaItem(mediaItem)
                }
            }

            override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
                if (this@AudioPlayerEngine.playMediaItem(mediaItem.mediaId)) {
                    this@AudioPlayerEngine.seekTo(startPositionMs)
                } else {
                    super.setMediaItem(mediaItem, startPositionMs)
                }
            }

            override fun getRepeatMode(): Int {
                return when (_repeatMode.value) {
                    RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                    RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                    RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                }
            }

            override fun setRepeatMode(@Player.RepeatMode repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                this@AudioPlayerEngine.setRepeatMode(mode)
            }

            override fun getShuffleModeEnabled(): Boolean {
                return _isShuffle.value
            }

            override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
                this@AudioPlayerEngine.setShuffleMode(shuffleModeEnabled)
            }

            override fun isCommandAvailable(command: Int): Boolean {
                return when (command) {
                    Player.COMMAND_SET_REPEAT_MODE,
                    Player.COMMAND_SET_SHUFFLE_MODE,
                    Player.COMMAND_PLAY_PAUSE,
                    Player.COMMAND_SEEK_TO_NEXT,
                    Player.COMMAND_SEEK_TO_PREVIOUS -> true
                    else -> super.isCommandAvailable(command)
                }
            }

            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(Player.COMMAND_SET_REPEAT_MODE)
                    .add(Player.COMMAND_SET_SHUFFLE_MODE)
                    .build()
            }
        }

        return player
    }

    fun getExoPlayer(): Player {
        initExoPlayer()
        return forwardingPlayer ?: exoPlayer!!
    }

    fun attachMediaSession(session: MediaSession?) {
        this.mediaSession = session
    }

    fun detachMediaSession() {
        this.mediaSession = null
    }

    fun setOnSongChangedListener(listener: ((Song) -> Unit)?) {
        onSongChangedCallback = listener
    }

    fun setOnFavoriteToggleListener(listener: ((Song) -> Unit)?) {
        onFavoriteToggleCallback = listener
    }

    fun triggerFavoriteToggle(song: Song) {
        onFavoriteToggleCallback?.invoke(song)
    }

    fun buildMediaItem(song: Song): MediaItem {
        val artworkUri = song.coverUri
            ?.takeIf { it.isNotBlank() }
            ?.toUri()
            ?: "android.resource://${context.packageName}/drawable/${song.coverDrawableName}".toUri()

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setDisplayTitle(song.title)
            .setArtworkUri(artworkUri)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .build()

        return MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.mediaUri?.takeIf { it.isNotBlank() }?.toUri() ?: Uri.EMPTY)
            .setMediaMetadata(metadata)
            .build()
    }

    fun setQueue(songs: List<Song>, startIndex: Int = 0, autoPlay: Boolean = true) {
        _queue.value = songs
        if (songs.isNotEmpty() && startIndex in songs.indices) {
            val song = songs[startIndex]
            if (autoPlay) {
                playSong(song)
            } else {
                _currentSong.value = song
                _durationMs.value = song.durationMs
                _currentPositionMs.value = 0L
                isUsingSynth = song.mediaUri.isNullOrBlank()

                val mediaItem = buildMediaItem(song)
                val player = initExoPlayer()
                player.setMediaItem(mediaItem)
                if (!song.mediaUri.isNullOrBlank()) {
                    player.prepare()
                }
            }
        }
    }

    fun setLibrarySongs(songs: List<Song>) {
        librarySongs = songs
        val songsById = songs.associateBy { it.id }
        if (_queue.value.isNotEmpty()) {
            _queue.value = _queue.value.map { queued -> songsById[queued.id] ?: queued }
        }
        _currentSong.value?.let { current ->
            songsById[current.id]?.let { _currentSong.value = it }
        }
        if (_queue.value.isEmpty() && songs.isNotEmpty()) {
            setQueue(songs, startIndex = 0, autoPlay = false)
        }
    }

    fun playMediaItem(mediaId: String): Boolean {
        val queuedSong = _queue.value.firstOrNull { it.id.toString() == mediaId }
        if (queuedSong != null) {
            playSong(queuedSong)
            return true
        }

        val libraryIndex = librarySongs.indexOfFirst { it.id.toString() == mediaId }
        if (libraryIndex < 0) return false
        setQueue(librarySongs, startIndex = libraryIndex, autoPlay = true)
        return true
    }

    fun updateSongMetadata(song: Song) {
        librarySongs = librarySongs.map { librarySong ->
            if (librarySong.id == song.id) song else librarySong
        }
        _queue.value = _queue.value.map { queuedSong ->
            if (queuedSong.id == song.id) song else queuedSong
        }
        if (_currentSong.value?.id == song.id) {
            _currentSong.value = song
            exoPlayer?.let { player ->
                val index = player.currentMediaItemIndex
                if (index != C.INDEX_UNSET && index < player.mediaItemCount) {
                    player.replaceMediaItem(index, buildMediaItem(song))
                }
            }
        }
    }

    fun removeSong(songId: Long) {
        val oldQueue = _queue.value
        val current = _currentSong.value
        val wasCurrent = current?.id == songId
        val wasPlaying = _isPlaying.value
        val oldIndex = oldQueue.indexOfFirst { it.id == songId }
        val updatedQueue = oldQueue.filterNot { it.id == songId }

        librarySongs = librarySongs.filterNot { it.id == songId }
        _queue.value = updatedQueue

        if (!wasCurrent) return

        val replacement = if (oldIndex >= 0) {
            updatedQueue.getOrNull(oldIndex)
                ?: updatedQueue.getOrNull(oldIndex - 1)
        } else {
            updatedQueue.firstOrNull()
        }

        if (replacement == null) {
            stopCurrentPlayback()
            exoPlayer?.clearMediaItems()
            isUsingSynth = false
            _currentSong.value = null
            _durationMs.value = 0L
            _currentPositionMs.value = 0L
            _isPlaying.value = false
            return
        }

        if (wasPlaying) {
            playSong(replacement)
        } else {
            setQueue(updatedQueue, startIndex = updatedQueue.indexOf(replacement), autoPlay = false)
        }
    }

    fun playSong(song: Song) {
        crossfadeTransitionJob?.cancel()
        crossfadeTransitionJob = null
        isCrossfadeTransitioning = false
        fadeInJob?.cancel()
        fadeInJob = null
        val shouldFadeIn = fadeInNextTrack
        fadeInNextTrack = false
        stopCurrentPlayback()
        _playbackError.value = null
        _waveformSamples.value = List(96) { 0.12f }
        _currentSong.value = song
        _durationMs.value = song.durationMs
        _currentPositionMs.value = 0L

        if (!song.isAvailable) {
            reportPlaybackError(
                "O arquivo de \"${song.title}\" não está disponível. Faça uma nova importação para corrigir o acesso."
            )
            return
        }

        onSongChangedCallback?.invoke(song)

        if (!song.mediaUri.isNullOrBlank()) {
            playUri(song, shouldFadeIn)
        } else {
            playSynth(song, startPositionMs = 0L, fadeIn = shouldFadeIn)
        }
    }

    private fun playSynth(
        song: Song,
        startPositionMs: Long = 0L,
        fadeIn: Boolean = false
    ) {
        isUsingSynth = true
        _isPlaying.value = true
        _currentPositionMs.value = startPositionMs.coerceIn(0L, song.durationMs.coerceAtLeast(0L))
        val preset = song.synthPreset ?: "synthwave"

        // Set metadata on ExoPlayer to keep MediaSession notification updated
        val mediaItem = buildMediaItem(song)
        val player = initExoPlayer()
        try {
            // Synthetic tracks are rendered by AudioTrack. ExoPlayer only carries
            // their metadata for the MediaSession and must not prepare Uri.EMPTY.
            player.setMediaItem(mediaItem)
            // Keep the media item marked as ready-to-play for MediaSession and
            // task-removal handling, without preparing Uri.EMPTY in ExoPlayer.
            player.playWhenReady = true
            player.volume = 1f
        } catch (e: Exception) {
            Log.w("AudioPlayerEngine", "Could not update synthetic track metadata", e)
        }
        val targetGain = equalizerEngine.getOutputGain()
        synthGenerator.setMasterVolume(if (fadeIn) 0f else targetGain)

        synthGenerator.setSpeed(_playbackSpeed.value)
        synthGenerator.start(
            preset = preset,
            durationMs = song.durationMs,
            startPositionMs = _currentPositionMs.value,
            scope = engineScope,
            onProgress = { current, total ->
                _currentPositionMs.value = current
                _durationMs.value = total
                _visualizerAmplitudes.value = synthGenerator.visualizerAmplitudes.copyOf()
                pushWaveformSample(synthGenerator.visualizerAmplitudes.average().toFloat())
                maybeStartCrossfade(current, total)
            },
            onCompletion = {
                handleTrackCompletion()
            }
        )

        equalizerEngine.bindAudioSession(synthGenerator.getAudioSessionId(), synthGenerator)
        waveformCapture.attach(synthGenerator.getAudioSessionId())
        if (fadeIn) startFadeIn(targetGain)
    }

    private fun playUri(song: Song, fadeIn: Boolean = false) {
        isUsingSynth = false
        try {
            val player = initExoPlayer()
            val mediaItem = buildMediaItem(song)
            player.setMediaItem(mediaItem)
            player.playbackParameters = PlaybackParameters(_playbackSpeed.value)
            val targetGain = equalizerEngine.getOutputGain()
            player.volume = if (fadeIn) 0f else targetGain
            player.prepare()
            player.play()

            _isPlaying.value = true
            _durationMs.value = song.durationMs

            equalizerEngine.bindAudioSession(player.audioSessionId, null)
            waveformCapture.attach(player.audioSessionId)
            startProgressTicker()
            if (fadeIn) startFadeIn(targetGain)
        } catch (e: Exception) {
            Log.e("AudioPlayerEngine", "Failed to play URI via ExoPlayer: ${song.mediaUri}", e)
            reportPlaybackError(
                "Não foi possível abrir o arquivo de áudio. Ele pode ter sido removido ou o acesso expirou."
            )
        }
    }

    private fun reportPlaybackError(message: String) {
        _isPlaying.value = false
        progressTickerJob?.cancel()
        exoPlayer?.stop()
        _playbackError.value = message
    }

    fun clearPlaybackError() {
        _playbackError.value = null
    }

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = engineScope.launch(Dispatchers.Main) {
            while (isActive && _isPlaying.value) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        val current = player.currentPosition
                        val total = player.duration
                        if (current >= 0) {
                            _currentPositionMs.value = current
                        }
                        if (total > 0) {
                            _durationMs.value = total
                        }
                        updateSimulatedVisualizer(current)
                        maybeStartCrossfade(current, total)
                    }
                } ?: run {
                    if (isUsingSynth) {
                        updateSimulatedVisualizer(_currentPositionMs.value)
                    }
                }
                delay(60)
            }
            if (isActive) {
                resetVisualizer()
            }
        }
    }

    private fun updateSimulatedVisualizer(currentMs: Long) {
        val currentArr = _visualizerAmplitudes.value.copyOf()
        val time = currentMs / 100.0
        for (i in currentArr.indices) {
            val target = (kotlin.math.abs(kotlin.math.sin(time * (1.2 + i * 0.1) + i)).toFloat() * 0.75f + 0.15f)
            currentArr[i] = (currentArr[i] * 0.6f + target * 0.4f).coerceIn(0.1f, 1.0f)
        }
        _visualizerAmplitudes.value = currentArr
        pushWaveformSample(currentArr.average().toFloat())
    }

    private fun resetVisualizer() {
        val resetArr = FloatArray(32) { 0.1f }
        _visualizerAmplitudes.value = resetArr
        _waveformSamples.value = List(96) { 0.12f }
    }

    private fun pushWaveformSample(amplitude: Float) {
        val samples = _waveformSamples.value.toMutableList()
        if (samples.isNotEmpty()) samples.removeAt(0)
        samples += amplitude.coerceIn(0.05f, 1f)
        _waveformSamples.value = samples
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _isPlaying.value = false
        crossfadeTransitionJob?.cancel()
        crossfadeTransitionJob = null
        isCrossfadeTransitioning = false
        fadeInJob?.cancel()
        fadeInJob = null
        if (isUsingSynth) {
            synthGenerator.pause()
        }
        exoPlayer?.pause()
        restorePlaybackVolume()
        progressTickerJob?.cancel()
    }

    fun resume() {
        val current = _currentSong.value ?: return
        val isSyntheticSong = current.mediaUri.isNullOrBlank()

        if (isSyntheticSong) {
            val synthSessionId = synthGenerator.getAudioSessionId()
            if (isUsingSynth && synthSessionId != 0) {
                _isPlaying.value = true
                synthGenerator.resume(
                    scope = engineScope,
                    onProgress = { pos, dur ->
                        _currentPositionMs.value = pos
                        _durationMs.value = dur
                        _visualizerAmplitudes.value = synthGenerator.visualizerAmplitudes.copyOf()
                        pushWaveformSample(synthGenerator.visualizerAmplitudes.average().toFloat())
                        maybeStartCrossfade(pos, dur)
                    },
                    onCompletion = { handleTrackCompletion() }
                )
            } else {
                // A queue restored with autoPlay=false has not initialized the
                // synth yet. Start it from the saved position instead of asking
                // ExoPlayer to play Uri.EMPTY.
                playSynth(current, _currentPositionMs.value)
            }
        } else {
            isUsingSynth = false
            _isPlaying.value = true
            exoPlayer?.let { player ->
                player.play()
                startProgressTicker()
            } ?: playSong(current)
        }
    }

    fun seekTo(positionMs: Long) {
        val pos = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(0L))
        _currentPositionMs.value = pos
        if (isUsingSynth) {
            synthGenerator.seekTo(pos)
        }
        exoPlayer?.seekTo(pos)
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return
        val current = _currentSong.value
        val currentIndex = q.indexOfFirst { it.id == current?.id }
        val playableIndices = q.indices.filter { q[it].isAvailable }
        if (playableIndices.isEmpty()) {
            reportPlaybackError("Nenhuma faixa disponível na fila de reprodução.")
            return
        }

        val nextIndex = if (_isShuffle.value && playableIndices.size > 1) {
            var r = playableIndices.random()
            while (r == currentIndex && playableIndices.size > 1) {
                r = playableIndices.random()
            }
            r
        } else {
            playableIndices.firstOrNull { it > currentIndex } ?: playableIndices.first()
        }

        playSong(q[nextIndex])
    }

    fun playPrevious() {
        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val q = _queue.value
        if (q.isEmpty()) return
        val current = _currentSong.value
        val currentIndex = q.indexOfFirst { it.id == current?.id }
        val playableIndices = q.indices.filter { q[it].isAvailable }
        if (playableIndices.isEmpty()) {
            reportPlaybackError("Nenhuma faixa disponível na fila de reprodução.")
            return
        }

        val prevIndex = playableIndices.lastOrNull { it < currentIndex } ?: playableIndices.last()

        playSong(q[prevIndex])
    }

    private fun handleTrackCompletion() {
        if (isCrossfadeTransitioning) return
        if (_sleepTimerEndAtTrackEnd.value) {
            _sleepTimerEndAtTrackEnd.value = false
            pause()
            return
        }

        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                _currentSong.value?.let { playSong(it) }
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                val q = _queue.value
                val current = _currentSong.value
                val currentIndex = q.indexOfFirst { it.id == current?.id }
                val nextIndex = q.indices.firstOrNull {
                    it > currentIndex && q[it].isAvailable
                }
                if (nextIndex != null) {
                    playSong(q[nextIndex])
                } else {
                    stopCurrentPlayback()
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                }
            }
        }
    }

    // Sleep Timer Engine Controls
    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerEndAtTrackEnd.value = false
        if (minutes <= 0) {
            _sleepTimerRemainingSeconds.value = null
            return
        }

        val totalSeconds = minutes * 60
        _sleepTimerRemainingSeconds.value = totalSeconds

        sleepTimerJob = engineScope.launch {
            var remaining = totalSeconds
            while (isActive && remaining > 0) {
                delay(1000)
                remaining -= 1
                _sleepTimerRemainingSeconds.value = remaining
            }

            // Sleep timer reached zero: smooth fade-out and pause background playback
            if (isActive) {
                fadeAndPause()
                _sleepTimerRemainingSeconds.value = null
            }
        }
    }

    fun setSleepTimerUntilTrackEnd() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSeconds.value = null
        _sleepTimerEndAtTrackEnd.value = true
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSeconds.value = null
        _sleepTimerEndAtTrackEnd.value = false
    }

    private suspend fun fadeAndPause() {
        try {
            val originalVolume = exoPlayer?.volume ?: equalizerEngine.getOutputGain()
            val originalSynthVolume = equalizerEngine.getOutputGain()
            val steps = 10
            for (i in steps downTo 0) {
                val factor = (i.toFloat() / steps)
                exoPlayer?.volume = factor * originalVolume
                synthGenerator.setMasterVolume(factor * originalSynthVolume)
                delay(150)
            }
            pause()
            exoPlayer?.volume = originalVolume
            synthGenerator.setMasterVolume(originalSynthVolume)
        } catch (e: Exception) {
            pause()
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
        // The engine owns the queue and advances it manually. ExoPlayer must
        // never loop its single metadata/media item behind our back.
        exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
    }

    fun cycleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        setRepeatMode(nextMode)
    }

    fun setShuffleMode(enabled: Boolean) {
        _isShuffle.value = enabled
        exoPlayer?.shuffleModeEnabled = enabled
    }

    fun toggleShuffle() {
        setShuffleMode(!_isShuffle.value)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (isUsingSynth) {
            synthGenerator.setSpeed(speed)
        } else {
            exoPlayer?.playbackParameters = PlaybackParameters(speed)
        }
    }

    fun setCrossfadeSeconds(seconds: Int) {
        _crossfadeSeconds.value = seconds.coerceIn(0, 12)
    }

    fun syncEqualizer(state: EqualizerState) {
        equalizerEngine.applyState(state, if (isUsingSynth) synthGenerator else null)
        val gain = equalizerEngine.getOutputGain()
        if (isUsingSynth) {
            synthGenerator.setMasterVolume(gain)
        } else {
            exoPlayer?.volume = gain
        }
    }

    private fun stopCurrentPlayback() {
        progressTickerJob?.cancel()
        synthGenerator.stop()
        waveformCapture.release()
        exoPlayer?.stop()
    }

    private fun maybeStartCrossfade(currentMs: Long, totalMs: Long) {
        val crossfadeMs = _crossfadeSeconds.value * 1_000L
        if (
            crossfadeMs <= 0L || totalMs <= 0L || currentMs <= 0L ||
            totalMs - currentMs > crossfadeMs || isCrossfadeTransitioning
        ) return

        isCrossfadeTransitioning = true
        crossfadeTransitionJob = engineScope.launch(Dispatchers.Main) {
            try {
                val steps = 10
                val originalVolume = exoPlayer?.volume ?: 1f
                val originalSynthVolume = equalizerEngine.getOutputGain()
                repeat(steps) { step ->
                    if (!isActive || !_isPlaying.value) return@launch
                    val factor = 1f - ((step + 1).toFloat() / steps)
                    exoPlayer?.volume = originalVolume * factor
                    synthGenerator.setMasterVolume(originalSynthVolume * factor)
                    delay((crossfadeMs / steps).coerceAtLeast(20L))
                }
                if (isActive && _isPlaying.value) {
                    crossfadeTransitionJob = null
                    fadeInNextTrack = true
                    handleTrackCompletion()
                    if (!_isPlaying.value) fadeInNextTrack = false
                }
            } finally {
                isCrossfadeTransitioning = false
            }
        }
    }

    private fun startFadeIn(targetGain: Float) {
        fadeInJob?.cancel()
        val transitionMs = (_crossfadeSeconds.value * 1_000L).coerceAtLeast(200L)
        fadeInJob = engineScope.launch(Dispatchers.Main) {
            try {
                val steps = 10
                repeat(steps) { step ->
                    if (!isActive || !_isPlaying.value) return@launch
                    val factor = (step + 1).toFloat() / steps
                    val gain = targetGain * factor
                    if (isUsingSynth) {
                        synthGenerator.setMasterVolume(gain)
                    } else {
                        exoPlayer?.volume = gain
                    }
                    delay((transitionMs / steps).coerceAtLeast(20L))
                }
            } finally {
                if (isActive && _isPlaying.value) restorePlaybackVolume()
                fadeInJob = null
            }
        }
    }

    private fun restorePlaybackVolume() {
        val gain = equalizerEngine.getOutputGain()
        if (isUsingSynth) synthGenerator.setMasterVolume(gain)
        exoPlayer?.volume = gain
    }

    fun release() {
        stopCurrentPlayback()
        engineJob.cancel()
        onSongChangedCallback = null
        onFavoriteToggleCallback = null
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
        forwardingPlayer = null
        equalizerEngine.release()
        waveformCapture.release()
    }
}
