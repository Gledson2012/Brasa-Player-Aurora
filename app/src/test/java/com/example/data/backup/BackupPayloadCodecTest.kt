package com.example.data.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupPayloadCodecTest {

    @Test
    fun `writes and reads standard gzip`() {
        val payload = "{\"version\":2,\"songs\":[]}".toByteArray()
        val output = ByteArrayOutputStream()

        BackupPayloadCodec.writeGzip(output, payload)

        val encoded = output.toByteArray()
        assertTrue(encoded[0] == 0x1f.toByte() && encoded[1] == 0x8b.toByte())
        assertEquals(String(payload), BackupPayloadCodec.decode(ByteArrayInputStream(encoded)))
    }

    @Test
    fun `reads legacy prefixed gzip`() {
        val payload = "{\"version\":1}".toByteArray()
        val gzip = ByteArrayOutputStream().also {
            BackupPayloadCodec.writeGzip(it, payload)
        }.toByteArray()
        val legacy = byteArrayOf(0x1f.toByte(), 0x8b.toByte()) + gzip

        assertEquals(String(payload), BackupPayloadCodec.decode(ByteArrayInputStream(legacy)))
    }

    @Test
    fun `reads legacy plain json`() {
        val payload = "{\"version\":1}".toByteArray()

        assertEquals(String(payload), BackupPayloadCodec.decode(ByteArrayInputStream(payload)))
    }
}
