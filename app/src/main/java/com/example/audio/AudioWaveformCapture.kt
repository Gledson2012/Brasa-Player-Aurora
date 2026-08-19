package com.example.audio

import android.media.audiofx.Visualizer
import kotlin.math.abs

class AudioWaveformCapture(
    private val onSamples: (List<Float>) -> Unit
) {
    private var visualizer: Visualizer? = null

    fun attach(sessionId: Int) {
        release()
        if (sessionId == 0) return

        try {
            visualizer = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveform ?: return
                            if (waveform.isEmpty()) return
                            val bucketCount = 96
                            val samples = List(bucketCount) { bucket ->
                                val start = bucket * waveform.size / bucketCount
                                val end = ((bucket + 1) * waveform.size / bucketCount).coerceAtLeast(start + 1)
                                waveform.copyOfRange(start, end)
                                    .map { abs(it.toInt()) / 128f }
                                    .average()
                                    .toFloat()
                                    .coerceIn(0.05f, 1f)
                            }
                            onSamples(samples)
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) = Unit
                    },
                    50_000,
                    true,
                    false
                )
                enabled = true
            }
        } catch (_: Exception) {
            // Some devices do not expose a Visualizer session. The engine's
            // simulated waveform remains active in that case.
            release()
        }
    }

    fun release() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {
            // Audio effects are best-effort and device-dependent.
        } finally {
            visualizer = null
        }
    }
}
