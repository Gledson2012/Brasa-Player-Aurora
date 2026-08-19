package com.example

import com.example.data.lyrics.LyricsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsManagerTest {

    @Test
    fun `parseLrc supports multiple timestamps and sorts lines`() {
        val lyrics = LyricsManager.parseLrc(
            songId = 42L,
            lrcContent = """
                [00:10.50] Segunda linha
                [00:01.5][00:02.00] Primeira linha
                [00:20.000] Última linha
            """.trimIndent()
        )

        assertEquals(4, lyrics.lines.size)
        assertEquals(1_500L, lyrics.lines[0].timestampMs)
        assertEquals(2_000L, lyrics.lines[1].timestampMs)
        assertEquals("Primeira linha", lyrics.lines[0].text)
        assertEquals(10_500L, lyrics.lines[2].timestampMs)
        assertTrue(lyrics.isSynced)
    }

    @Test
    fun `toLrc formats timestamps with centiseconds`() {
        val lyrics = LyricsManager.parseLrc(1L, "[01:02.34] Teste")

        assertEquals("[01:02.34] Teste", LyricsManager.toLrc(lyrics))
    }
}
