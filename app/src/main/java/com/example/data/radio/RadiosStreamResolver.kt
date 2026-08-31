package com.example.data.radio

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.Locale

/**
 * Resolves the M3U links exposed by Radios.com.br into the station's real
 * audio URL. The same resolver also accepts a direct MP3/AAC/HLS URL.
 */
object RadiosStreamResolver {
    private const val TAG = "RadiosStreamResolver"
    private const val MAX_PLAYLIST_BYTES = 128 * 1024

    suspend fun resolve(sourceUrl: String): String? = withContext(Dispatchers.IO) {
        val requestedUrl = sourceUrl.trim()
        if (!isHttpUrl(requestedUrl)) return@withContext null

        val connection = try {
            (URL(requestedUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
                instanceFollowRedirects = true
                useCaches = false
                setRequestProperty(
                    "Accept",
                    "audio/mpeg,audio/aac,audio/aacp,audio/ogg," +
                        "application/vnd.apple.mpegurl,audio/x-mpegurl,text/plain;q=0.8,*/*;q=0.5"
                )
                setRequestProperty("User-Agent", "BrasaPlayer/1.2 (Android)")
                if (requestedUrl.contains("radios.com.br", ignoreCase = true)) {
                    setRequestProperty("Referer", "https://www.radios.com.br/")
                }
            }
        } catch (error: Exception) {
            Log.w(TAG, "Invalid radio URL: $requestedUrl", error)
            return@withContext null
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.w(TAG, "Radio source returned HTTP $responseCode: $requestedUrl")
                return@withContext null
            }

            val finalUrl = connection.url?.toString().orEmpty().ifBlank { requestedUrl }
            val contentType = connection.contentType.orEmpty().lowercase(Locale.ROOT)

            // A playlist endpoint may redirect straight to the live audio
            // server. Do not attempt to read that endless response as text.
            if (isDirectAudioContentType(contentType) && !isPlaylistUrl(finalUrl)) {
                return@withContext finalUrl
            }

            val isPlaylist = isPlaylistContentType(contentType) ||
                isPlaylistUrl(requestedUrl) ||
                isPlaylistUrl(finalUrl)

            if (!isPlaylist) {
                // A few streaming servers omit Content-Type. Let Media3 inspect
                // the final URL instead of consuming a live audio connection here.
                return@withContext finalUrl
            }

            val playlist = readAtMost(connection.inputStream, MAX_PLAYLIST_BYTES)
            val streamUrl = findFirstStreamUrl(playlist, finalUrl)
            if (streamUrl == null) {
                Log.w(TAG, "No playable stream found in playlist: $requestedUrl")
            }
            streamUrl
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Log.w(TAG, "Could not resolve radio source: $requestedUrl", error)
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun readAtMost(input: InputStream, maxBytes: Int): String {
        input.use { stream ->
            val buffer = ByteArray(8 * 1024)
            val output = StringBuilder()
            var remaining = maxBytes
            while (remaining > 0) {
                val read = stream.read(buffer, 0, minOf(buffer.size, remaining))
                if (read < 0) break
                output.append(String(buffer, 0, read, Charsets.UTF_8))
                remaining -= read
            }
            return output.toString()
        }
    }

    private fun findFirstStreamUrl(playlist: String, baseUrl: String): String? {
        return playlist.lineSequence()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith(";") }
            .mapNotNull { line ->
                val normalizedLine = if (
                    line.startsWith("file", ignoreCase = true) && line.contains('=')
                ) {
                    line.substringAfter('=').trim()
                } else {
                    line
                }
                when {
                    isHttpUrl(normalizedLine) -> normalizedLine
                    normalizedLine.startsWith("//") -> "https:$normalizedLine"
                    else -> runCatching { URI(baseUrl).resolve(normalizedLine).toString() }.getOrNull()
                }
            }
            .firstOrNull(::isHttpUrl)
    }

    private fun isPlaylistContentType(contentType: String): Boolean =
        contentType.contains("mpegurl") ||
            contentType.contains("m3u") ||
            contentType.contains("playlist")

    private fun isDirectAudioContentType(contentType: String): Boolean =
        contentType.startsWith("audio/") && !isPlaylistContentType(contentType)

    private fun isPlaylistUrl(url: String): Boolean {
        val normalized = url.lowercase(Locale.ROOT)
        return normalized.substringBefore('?').endsWith(".m3u") ||
            normalized.substringBefore('?').endsWith(".m3u8") ||
            normalized.substringBefore('?').endsWith(".pls")
    }

    private fun isHttpUrl(url: String): Boolean =
        url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)
}
