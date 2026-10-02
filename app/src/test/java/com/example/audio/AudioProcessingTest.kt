package com.example.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class AudioProcessingTest {

    @Test
    fun `dB to millibels factor is 100`() {
        val gainDb = 5
        val milliBels = (gainDb * 100).toShort()
        assertEquals(500.toShort(), milliBels)

        val maxGainDb = 10
        val maxMilliBels = (maxGainDb * 100).toShort()
        assertEquals(1000.toShort(), maxMilliBels)

        val minGainDb = -10
        val minMilliBels = (minGainDb * 100).toShort()
        assertEquals((-1000).toShort(), minMilliBels)
    }

    @Test
    fun `pcm 8bit unsigned conversion correctly identifies silence`() {
        // In 8-bit unsigned PCM, 128 (0x80) is the center / silence
        val silenceByte: Byte = 128.toByte() // 0x80
        val amplitude = abs((silenceByte.toInt() and 0xFF) - 128) / 128f
        assertEquals(0.0f, amplitude, 0.001f)
    }

    @Test
    fun `pcm 8bit unsigned conversion correctly identifies full scale peaks`() {
        // Negative peak: 0
        val negPeakByte: Byte = 0.toByte()
        val negAmplitude = abs((negPeakByte.toInt() and 0xFF) - 128) / 128f
        assertEquals(1.0f, negAmplitude, 0.001f)

        // Positive peak: 255
        val posPeakByte: Byte = 255.toByte() // 0xFF
        val posAmplitude = abs((posPeakByte.toInt() and 0xFF) - 128) / 128f
        assertTrue(posAmplitude > 0.99f && posAmplitude <= 1.0f)
    }
}
