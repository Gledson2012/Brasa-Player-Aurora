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
        if (sessionId == 0 || sessionId == currentSessionId) return
        currentSessionId = sessionId

        release()

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
        currentState = state
        outputGain = calculateOutputGain(state)

        // Apply to hardware effects if attached
        try {
            equalizer?.enabled = state.isEnabled
            bassBoost?.enabled = state.isEnabled
            virtualizer?.enabled = state.isEnabled

            if (state.isEnabled) {
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
                        if (targetIndex >= state.bandLevels.size || numBands == 0) return@forEachIndexed
                        val closestBand = (0 until numBands).minByOrNull { band ->
                            kotlin.math.abs((eq.getCenterFreq(band.toShort()) / 1000) - targetHz)
                        } ?: return@forEachIndexed
                        val milliBels = (state.bandLevels[targetIndex] * 1000)
                            .toShort()
                            .coerceIn(minLevel, maxLevel)
                        eq.setBandLevel(closestBand.toShort(), milliBels)
                    }
                }

                bassBoost?.let { bb ->
                    if (bb.strengthSupported) {
                        bb.setStrength((state.bassBoost * 10).toShort().coerceIn(0, 1000))
                    }
                }

                virtualizer?.let { virt ->
                    if (virt.strengthSupported) {
                        virt.setStrength((state.virtualizer * 10).toShort().coerceIn(0, 1000))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("EqualizerEngine", "Failed to apply hardware EQ params: ${e.message}")
        }

        // Apply to procedural synthesizer for offline synth tracks
        proceduralAudioGenerator?.let { synth ->
            for (i in 0 until 5) {
                synth.bandGains[i] = if (state.isEnabled && i < state.bandLevels.size) state.bandLevels[i].toFloat() else 0f
            }
            synth.bassBoostAmount = if (state.isEnabled) state.bassBoost / 100f else 0f
            synth.balanceGain = state.balance
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
    }
}
