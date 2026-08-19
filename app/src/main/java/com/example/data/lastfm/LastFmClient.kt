package com.example.data.lastfm

import com.example.data.model.LastFmSettings
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

data class LastFmSession(val username: String, val sessionKey: String)

class LastFmClient(private val settings: LastFmSettings) {
    companion object {
        private const val API_URL = "https://ws.audioscrobbler.com/2.0/"

        fun authorizationUrl(apiKey: String, token: String): String =
            "https://www.last.fm/api/auth/?api_key=${encode(apiKey)}&token=${encode(token)}"

        private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
    }

    suspend fun requestToken(): String = withContext(Dispatchers.IO) {
        val response = post("auth.getToken", mapOf("api_key" to settings.apiKey))
        response.getString("token")
    }

    suspend fun getSession(token: String): LastFmSession = withContext(Dispatchers.IO) {
        val response = post(
            "auth.getSession",
            mapOf("api_key" to settings.apiKey, "token" to token)
        )
        val session = response.getJSONObject("session")
        LastFmSession(session.getString("name"), session.getString("key"))
    }

    suspend fun updateNowPlaying(song: Song): Unit = withContext(Dispatchers.IO) {
        post(
            "track.updateNowPlaying",
            mapOf(
                "api_key" to settings.apiKey,
                "sk" to settings.sessionKey,
                "artist" to song.artist,
                "track" to song.title,
                "album" to song.album,
                "duration" to (song.durationMs / 1000L).coerceAtLeast(0).toString()
            )
        )
    }

    suspend fun scrobble(song: Song, timestampSeconds: Long = System.currentTimeMillis() / 1000L): Unit = withContext(Dispatchers.IO) {
        post(
            "track.scrobble",
            mapOf(
                "api_key" to settings.apiKey,
                "sk" to settings.sessionKey,
                "artist" to song.artist,
                "track" to song.title,
                "album" to song.album,
                "timestamp" to timestampSeconds.toString(),
                "duration" to (song.durationMs / 1000L).coerceAtLeast(0).toString()
            )
        )
    }

    private fun post(method: String, values: Map<String, String>): JSONObject {
        require(settings.apiKey.isNotBlank()) { "A chave da API do Last.fm não foi configurada." }
        require(settings.apiSecret.isNotBlank()) { "O segredo da API do Last.fm não foi configurado." }

        val params = values + ("method" to method)
        val signature = md5(params.toSortedMap().entries.joinToString("") { it.key + it.value } + settings.apiSecret)
        val body = (params + ("api_sig" to signature) + ("format" to "json"))
            .entries
            .joinToString("&") { "${encode(it.key)}=${encode(it.value)}" }

        val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8000
            readTimeout = 8000
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("User-Agent", "MusicPlayer/1.0")
        }

        return try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = JSONObject(response.ifBlank { "{}" })
            if (connection.responseCode !in 200..299 || json.has("error")) {
                throw IOException(json.optString("message", "Erro HTTP ${connection.responseCode}"))
            }
            json
        } finally {
            connection.disconnect()
        }
    }

    private fun md5(value: String): String = MessageDigest.getInstance("MD5")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
