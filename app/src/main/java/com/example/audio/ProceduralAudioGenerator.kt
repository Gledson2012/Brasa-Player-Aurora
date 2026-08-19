package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh

/**
 * High-performance polyphonic synthesizer that generates rich, melodic offline audio
 * for demo and built-in offline music library. Produces pleasant chords, basslines, and melodies.
 */
class ProceduralAudioGenerator {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val sampleRate = 44100
    private var isPlaying = false
    private var currentPositionMs = 0L
    private var currentPreset = "synthwave"
    private var trackDurationMs = 204000L
    private var playbackSpeed = 1.0f

    // Equalizer band gains in dB (-10 to +10)
    var bandGains = floatArrayOf(0f, 0f, 0f, 0f, 0f)
    var bassBoostAmount = 0.3f
    var balanceGain = 0.0f // -1.0f (left) to 1.0f (right)
    private var masterVolume = 1.0f

    // Visualizer real-time frequency amplitude values (32 bands)
    val visualizerAmplitudes = FloatArray(32) { 0.1f }

    fun setMasterVolume(vol: Float) {
        masterVolume = vol.coerceIn(0f, 1f)
    }

    fun getAudioSessionId(): Int {
        return audioTrack?.audioSessionId ?: 0
    }

    fun start(
        preset: String,
        durationMs: Long,
        startPositionMs: Long = 0L,
        scope: CoroutineScope,
        onProgress: (currentMs: Long, durationMs: Long) -> Unit,
        onCompletion: () -> Unit
    ) {
        stop()
        currentPreset = preset
        trackDurationMs = durationMs
        currentPositionMs = startPositionMs
        isPlaying = true

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback simulated timer if AudioTrack fails on container
        }

        playbackJob = scope.launch(Dispatchers.Default) {
            val samplesPerChunk = 2048
            val audioBuffer = ShortArray(samplesPerChunk * 2) // Stereo: Left and Right interleaved
            var sampleIndex = (currentPositionMs * sampleRate / 1000).toLong()

            while (isActive && isPlaying) {
                if (currentPositionMs >= trackDurationMs) {
                    isPlaying = false
                    launch(Dispatchers.Main) {
                        onCompletion()
                    }
                    break
                }

                // Generate audio chunk based on preset musical pattern
                generateChunk(
                    preset = currentPreset,
                    buffer = audioBuffer,
                    startSample = sampleIndex,
                    samplesCount = samplesPerChunk
                )

                // Write to AudioTrack
                audioTrack?.let { track ->
                    try {
                        track.write(audioBuffer, 0, audioBuffer.size)
                    } catch (e: Exception) {
                        // ignore write error
                    }
                }

                val chunkDurationMs = (samplesPerChunk * 1000L / sampleRate)
                currentPositionMs += (chunkDurationMs * playbackSpeed).toLong()
                sampleIndex += samplesPerChunk

                // Update visualizer frequency bars
                updateVisualizerData(sampleIndex, currentPreset)

                launch(Dispatchers.Main) {
                    onProgress(currentPositionMs.coerceAtMost(trackDurationMs), trackDurationMs)
                }

                // Small yield to match real-time clock
                delay((chunkDurationMs.toFloat() / (2f * playbackSpeed)).coerceAtLeast(10f).toLong())
            }
        }
    }

    private fun generateChunk(
        preset: String,
        buffer: ShortArray,
        startSample: Long,
        samplesCount: Int
    ) {
        val rootFrequencies = when (preset) {
            "synthwave" -> floatArrayOf(130.81f, 164.81f, 196.00f, 246.94f) // C3, E3, G3, B3
            "lofi" -> floatArrayOf(174.61f, 220.00f, 261.63f, 329.63f) // F3, A3, C4, E4
            "acoustic" -> floatArrayOf(196.00f, 246.94f, 293.66f, 392.00f) // G3, B3, D4, G4
            "electronic" -> floatArrayOf(110.00f, 146.83f, 164.81f, 220.00f) // A2, D3, E3, A3
            "jazz" -> floatArrayOf(146.83f, 185.00f, 220.00f, 277.18f) // D3, F#3, A3, C#4
            else -> floatArrayOf(130.81f, 164.81f, 196.00f, 220.00f)
        }

        // Convert dB to linear gain. This preserves the musical meaning of
        // the equalizer values and lets every UI band affect its own range.
        val band0Gain = dbToLinear(bandGains[0]) * (1.0f + bassBoostAmount * 0.65f)
        val band1Gain = dbToLinear(bandGains[1])
        val band2Gain = dbToLinear(bandGains[2])
        val band3Gain = dbToLinear(bandGains[3])
        val band4Gain = dbToLinear(bandGains[4])

        // Equal-power panning keeps the center at a consistent perceived
        // loudness instead of summing two full-amplitude channels.
        val pan = balanceGain.coerceIn(-1f, 1f)
        val panAngle = ((pan + 1f) * PI / 4.0)
        val leftPan = cos(panAngle).toFloat()
        val rightPan = sin(panAngle).toFloat()

        for (i in 0 until samplesCount) {
            val sample = startSample + i
            val timeSec = sample.toDouble() / sampleRate
            val beatSec = (timeSec * 2.0) // 120 BPM base
            val chordIndex = (beatSec.toInt() / 4) % rootFrequencies.size
            val rootFreq = rootFrequencies[chordIndex]

            // Main Synth / Melodic tone
            val osc1 = sin(2.0 * PI * rootFreq * timeSec)
            val osc2 = sin(2.0 * PI * (rootFreq * 1.5) * timeSec) * 0.5
            val subBass = sin(2.0 * PI * (rootFreq * 0.5) * timeSec)

            // Drum beat / Percussion envelope
            val beatPhase = beatSec - beatSec.toInt()
            val kick = if (beatPhase < 0.15) {
                sin(2.0 * PI * 60.0 * (1.0 - beatPhase / 0.15) * timeSec) * (1.0 - beatPhase / 0.15)
            } else 0.0
            val snare = if (beatSec.toInt() % 2 == 1 && beatPhase < 0.12) {
                ((Math.random() * 2.0 - 1.0) * (1.0 - beatPhase / 0.12) * 0.4)
            } else 0.0

            // Ambient arpeggio tone
            val arpNoteOffset = when ((beatSec * 4).toInt() % 4) {
                0 -> 1.0
                1 -> 1.25
                2 -> 1.5
                else -> 2.0
            }
            val arpOsc = sin(2.0 * PI * (rootFreq * 2.0 * arpNoteOffset) * timeSec) * 0.35

            val low = (subBass * 0.3 + kick * 0.4) * band0Gain
            val lowMid = osc1 * 0.35 * band1Gain
            val mid = osc2 * 0.2 * band2Gain
            val highMid = arpOsc * 0.3 * band3Gain
            val high = snare * 0.3 * band4Gain
            val mixed = low + lowMid + mid + highMid + high

            // A soft limiter avoids harsh digital clipping when several bands
            // are boosted together while retaining more detail than hard clamp.
            val limited = (tanh(mixed * 1.15) / tanh(1.15))
            val shortSample = (limited * 26000.0 * masterVolume).toInt().coerceIn(-32767, 32767).toShort()

            // Left channel
            buffer[i * 2] = (shortSample * leftPan).toInt().toShort()
            // Right channel
            buffer[i * 2 + 1] = (shortSample * rightPan).toInt().toShort()
        }
    }

    private fun dbToLinear(db: Float): Float {
        return 10.0.pow((db.coerceIn(-10f, 10f) / 20f).toDouble()).toFloat()
    }

    private fun updateVisualizerData(sampleIndex: Long, preset: String) {
        val time = sampleIndex.toDouble() / sampleRate
        for (b in visualizerAmplitudes.indices) {
            val freqWeight = sin(time * (3.0 + b * 0.8) + b * 0.4)
            val bassBoostFactor = if (b < 8) (1.0f + bassBoostAmount) else 1.0f
            val eqFactor = 1.0f + (bandGains[(b * 5 / 32).coerceIn(0, 4)] * 0.06f)
            val raw = (kotlin.math.abs(freqWeight).toFloat() * 0.7f + 0.15f) * bassBoostFactor * eqFactor
            visualizerAmplitudes[b] = raw.coerceIn(0.08f, 1.0f)
        }
    }

    fun pause() {
        isPlaying = false
        try {
            audioTrack?.pause()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun resume(scope: CoroutineScope, onProgress: (currentMs: Long, durationMs: Long) -> Unit, onCompletion: () -> Unit) {
        if (!isPlaying) {
            start(
                preset = currentPreset,
                durationMs = trackDurationMs,
                startPositionMs = currentPositionMs,
                scope = scope,
                onProgress = onProgress,
                onCompletion = onCompletion
            )
        }
    }

    fun seekTo(positionMs: Long) {
        currentPositionMs = positionMs.coerceIn(0L, trackDurationMs)
    }

    fun setSpeed(speed: Float) {
        playbackSpeed = speed.coerceIn(0.5f, 2.0f)
    }

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioTrack = null
        for (i in visualizerAmplitudes.indices) {
            visualizerAmplitudes[i] = 0.08f
        }
    }
}
