package com.example.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.data.model.EqualizerState
import kotlin.math.pow

class EqualizerEngine {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0

    private var currentState: EqualizerState = EqualizerState()
    private var outputGain: Float = 1f

    /**
     * Gain staging kept below unity prevents boosted presets from clipping the
     * output before the device's audio mixer applies its own processing.
     */
    fun getOutputGain(): Float = outputGain

    fun bindAudioSession(sessionId: Int, proceduralAudioGenerator: ProceduralAudioGenerator?) {
        if (sessionId == 0) return
        if (sessionId == currentSessionId && (equalizer != null || bassBoost != null || virtualizer != null)) return

        release()
        currentSessionId = sessionId

        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = currentState.isEnabled
            }
        } catch (e: Exception) {
            Log.w("EqualizerEngine", "Equalizer not supported on this device/session: ${e.message}")
        }

        try {
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = currentState.isEnabled
                if (strengthSupported) {
                    setStrength((currentState.bassBoost * 10).toShort()) // 0 to 1000
                }
            }
        } catch (e: Exception) {
            Log.w("EqualizerEngine", "BassBoost not supported: ${e.message}")
        }

        try {
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = currentState.isEnabled
                if (strengthSupported) {
                    setStrength((currentState.virtualizer * 10).toShort()) // 0 to 1000
                }
            }
        } catch (e: Exception) {
            Log.w("EqualizerEngine", "Virtualizer not supported: ${e.message}")
        }

        applyState(currentState, proceduralAudioGenerator)
    }

    fun applyState(state: EqualizerState, proceduralAudioGenerator: ProceduralAudioGenerator?) {
        val safeState = state.copy(
            bandLevels = state.bandLevels.map { it.coerceIn(-10, 10) },
            bassBoost = state.bassBoost.coerceIn(0, 100),
            virtualizer = state.virtualizer.coerceIn(0, 100),
            balance = state.balance.coerceIn(-1f, 1f)
        )
        currentState = safeState
        outputGain = calculateOutputGain(safeState)

        // Apply to hardware effects if attached
        try {
            equalizer?.enabled = safeState.isEnabled
            bassBoost?.enabled = safeState.isEnabled
            virtualizer?.enabled = safeState.isEnabled

            if (safeState.isEnabled) {
                equalizer?.let { eq ->
                    val numBands = eq.numberOfBands.toInt()
                    val minLevel = eq.bandLevelRange[0]
                    val maxLevel = eq.bandLevelRange[1]

                    // Device EQs expose different band counts and center
                    // frequencies. Map our stable five-band UI to the closest
                    // real hardware bands instead of assuming indexes match.
                    for (band in 0 until numBands) {
                        eq.setBandLevel(band.toShort(), 0)
                    }
                    val targetFrequenciesHz = intArrayOf(60, 230, 910, 3600, 14000)
                    targetFrequenciesHz.forEachIndexed { targetIndex, targetHz ->
                        if (targetIndex >= safeState.bandLevels.size || numBands == 0) return@forEachIndexed
                        val closestBand = (0 until numBands).minByOrNull { band ->
                            kotlin.math.abs((eq.getCenterFreq(band.toShort()) / 1000) - targetHz)
                        } ?: return@forEachIndexed
                        val milliBels = (safeState.bandLevels[targetIndex] * 1000).toShort()
                            .coerceIn(minLevel, maxLevel)
                        eq.setBandLevel(closestBand.toShort(), milliBels)
                    }
                }

                bassBoost?.let { bb ->
                    if (bb.strengthSupported) {
                        bb.setStrength((safeState.bassBoost * 10).toShort().coerceIn(0, 1000))
                    }
                }

                virtualizer?.let { virt ->
                    if (virt.strengthSupported) {
                        virt.setStrength((safeState.virtualizer * 10).toShort().coerceIn(0, 1000))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("EqualizerEngine", "Failed to apply hardware EQ params: ${e.message}")
        }

        // Apply to procedural synthesizer for offline synth tracks
        proceduralAudioGenerator?.let { synth ->
            for (i in 0 until 5) {
                synth.bandGains[i] = if (safeState.isEnabled && i < safeState.bandLevels.size) {
                    safeState.bandLevels[i].toFloat()
                } else {
                    0f
                }
            }
            synth.bassBoostAmount = if (safeState.isEnabled) safeState.bassBoost / 100f else 0f
            synth.balanceGain = safeState.balance
            synth.setMasterVolume(outputGain)
        }
    }

    private fun calculateOutputGain(state: EqualizerState): Float {
        if (!state.isEnabled) return 1f

        val strongestBand = state.bandLevels.maxOrNull()?.coerceAtLeast(0) ?: 0
        val estimatedBoostDb = strongestBand + (state.bassBoost * 0.035f) + (state.virtualizer * 0.01f)
        val headroomDb = (estimatedBoostDb - 2f).coerceIn(0f, 6f)
        return 10.0.pow((-headroomDb / 20f).toDouble()).toFloat().coerceIn(0.5f, 1f)
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            // ignore
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        currentSessionId = 0
    }
}
