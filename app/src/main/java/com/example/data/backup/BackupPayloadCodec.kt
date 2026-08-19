package com.example.data.backup

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Encodes and decodes the payload of a local backup.
 *
 * The decoder accepts plain JSON, standard gzip and the prefixed-gzip format
 * produced by older app versions. Both the source and expanded payload are
 * bounded so restore cannot allocate unbounded memory for a malformed file.
 */
internal object BackupPayloadCodec {
    const val MAX_BYTES = 25L * 1024L * 1024L

    private val GZIP_MAGIC_BYTES = byteArrayOf(0x1f.toByte(), 0x8b.toByte())

    fun writeGzip(output: OutputStream, payload: ByteArray) {
        require(payload.size.toLong() <= MAX_BYTES) {
            "Backup excede o tamanho máximo permitido."
        }
        GZIPOutputStream(output).use { gzipStream ->
            gzipStream.write(payload)
        }
    }

    fun decode(input: InputStream): String {
        val bytes = readLimitedBytes(input)
        if (!hasGzipMagic(bytes, 0)) {
            return bytes.toString(Charsets.UTF_8)
        }

        // Older exports prepended the gzip magic before GZIPOutputStream wrote
        // its own header. Keep accepting that format during migration.
        val gzipOffset = if (hasGzipMagic(bytes, GZIP_MAGIC_BYTES.size)) {
            GZIP_MAGIC_BYTES.size
        } else {
            0
        }
        val compressed = ByteArrayInputStream(bytes, gzipOffset, bytes.size - gzipOffset)
        return GZIPInputStream(compressed).use { gzipStream ->
            readLimitedBytes(gzipStream).toString(Charsets.UTF_8)
        }
    }

    private fun readLimitedBytes(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= MAX_BYTES) {
                "Backup excede o tamanho máximo permitido."
            }
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private fun hasGzipMagic(bytes: ByteArray, offset: Int): Boolean =
        offset >= 0 && offset + GZIP_MAGIC_BYTES.size <= bytes.size &&
            bytes[offset] == GZIP_MAGIC_BYTES[0] &&
            bytes[offset + 1] == GZIP_MAGIC_BYTES[1]
}
