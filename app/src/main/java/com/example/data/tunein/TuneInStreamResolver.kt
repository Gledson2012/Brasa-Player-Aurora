package com.example.data.tunein

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

/** Resolves a TuneIn station id into the current raw stream URL. */
object TuneInStreamResolver {
    private const val TAG = "TuneInStreamResolver"
    private const val ENDPOINT = "https://opml.radiotime.com/Tune.ashx"

    suspend fun resolve(stationId: String): String? = withContext(Dispatchers.IO) {
        val encodedId = URLEncoder.encode(stationId, Charsets.UTF_8.name())
        val connection = (URL(
            "$ENDPOINT?render=json&id=$encodedId&formats=mp3,aac"
        ).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "BrasaPlayer/1.0")
        }

        try {
            if (connection.responseCode !in 200..299) return@withContext null

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            val items = JSONObject(payload).optJSONArray("body") ?: return@withContext null

            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                if (item.optString("element") != "audio") continue

                val streamUrl = item.optString("url").trim()
                if (
                    streamUrl.isBlank() ||
                    streamUrl.contains("notcompatible", ignoreCase = true) ||
                    streamUrl.contains("georestricted", ignoreCase = true)
                ) {
                    continue
                }

                // TuneIn can return http even when the resolved stream supports
                // https. Prefer the secure variant for Android network policy.
                return@withContext streamUrl.replaceFirst("http://", "https://")
            }

            null
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Log.w(TAG, "Could not resolve station $stationId", error)
            null
        } finally {
            connection.disconnect()
        }
    }
}
