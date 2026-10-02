package com.example.data.radio

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Client for interacting with the community-driven Radio Browser API
 * (inspired by https://github.com/ivandotv/radio-browser-api).
 *
 * Implements mirror failover, JSON deserialization, station searches,
 * tag filtering, top voted / clicked streams, and click registration.
 */
class RadioBrowserClient(
    private val mirrors: List<String> = DEFAULT_MIRRORS
) {
    companion object {
        private const val TAG = "RadioBrowserClient"
        private const val USER_AGENT = "BrasaPlayer-Aurora/1.2.0 (Android; https://github.com/ivandotv/radio-browser-api)"
        private const val CONNECT_TIMEOUT_MS = 6000
        private const val READ_TIMEOUT_MS = 8000
        private const val MAX_RESPONSE_BYTES = 512 * 1024

        val DEFAULT_MIRRORS = listOf(
            "de1.api.radio-browser.info",
            "nl1.api.radio-browser.info",
            "at1.api.radio-browser.info"
        )
    }

    /**
     * Fetches top-voted stations worldwide.
     */
    suspend fun getTopVoted(limit: Int = 30): List<RadioBrowserStation> = withContext(Dispatchers.IO) {
        val path = "/json/stations/topvote/$limit"
        fetchStations(path)
    }

    /**
     * Fetches top-clicked stations worldwide.
     */
    suspend fun getTopClicked(limit: Int = 30): List<RadioBrowserStation> = withContext(Dispatchers.IO) {
        val path = "/json/stations/topclick/$limit"
        fetchStations(path)
    }

    /**
     * Fetches top stations from a specific country code (e.g., "BR", "PT", "US").
     */
    suspend fun getStationsByCountry(
        countryCode: String = "BR",
        limit: Int = 35
    ): List<RadioBrowserStation> = withContext(Dispatchers.IO) {
        val encodedCode = encode(countryCode.trim().uppercase())
        val path = "/json/stations/bycountrycodeexact/$encodedCode?order=votes&reverse=true&limit=$limit&hidebroken=true"
        fetchStations(path)
    }

    /**
     * Fetches stations by musical genre/tag (e.g. "mpb", "sertanejo", "rock", "pop").
     */
    suspend fun getStationsByTag(
        tag: String,
        countryCode: String? = null,
        limit: Int = 35
    ): List<RadioBrowserStation> = withContext(Dispatchers.IO) {
        val encodedTag = encode(tag.trim().lowercase())
        val path = if (!countryCode.isNullOrBlank()) {
            val encodedCountry = encode(countryCode.trim().uppercase())
            "/json/stations/search?tag=$encodedTag&countrycode=$encodedCountry&order=votes&reverse=true&limit=$limit&hidebroken=true"
        } else {
            "/json/stations/bytag/$encodedTag?order=votes&reverse=true&limit=$limit&hidebroken=true"
        }
        fetchStations(path)
    }

    /**
     * Searches stations by name, tag, or country.
     */
    suspend fun searchStations(
        query: String = "",
        countryCode: String? = null,
        tag: String? = null,
        limit: Int = 40
    ): List<RadioBrowserStation> = withContext(Dispatchers.IO) {
        val queryParams = mutableListOf<String>()
        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotBlank()) {
            queryParams.add("name=${encode(trimmedQuery)}")
        }
        if (!countryCode.isNullOrBlank()) {
            queryParams.add("countrycode=${encode(countryCode.trim().uppercase())}")
        }
        if (!tag.isNullOrBlank()) {
            queryParams.add("tag=${encode(tag.trim().lowercase())}")
        }
        queryParams.add("order=votes")
        queryParams.add("reverse=true")
        queryParams.add("limit=$limit")
        queryParams.add("hidebroken=true")

        val path = "/json/stations/search?" + queryParams.joinToString("&")
        fetchStations(path)
    }

    /**
     * Notifies Radio Browser API that the station was clicked/played, contributing
     * to the global community statistics.
     */
    suspend fun registerClick(stationuuid: String) = withContext(Dispatchers.IO) {
        if (stationuuid.isBlank()) return@withContext
        for (mirror in mirrors) {
            val endpoint = "https://$mirror/json/url/${encode(stationuuid)}"
            try {
                val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3000
                    readTimeout = 3000
                    setRequestProperty("User-Agent", USER_AGENT)
                }
                connection.responseCode
                connection.disconnect()
                break
            } catch (e: Exception) {
                // Ignore click registration failure
            }
        }
    }

    /**
     * Attempts to query available mirrors in order until one succeeds.
     */
    private fun fetchStations(pathWithQuery: String): List<RadioBrowserStation> {
        for (mirror in mirrors) {
            val urlString = "https://$mirror$pathWithQuery"
            try {
                val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    instanceFollowRedirects = true
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", USER_AGENT)
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val json = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                        val buffer = CharArray(4096)
                        val out = java.lang.StringBuilder()
                        var totalRead = 0
                        while (totalRead < MAX_RESPONSE_BYTES) {
                            val r = reader.read(buffer, 0, minOf(buffer.size, MAX_RESPONSE_BYTES - totalRead))
                            if (r <= 0) break
                            out.append(buffer, 0, r)
                            totalRead += r
                        }
                        out.toString()
                    }
                    connection.disconnect()
                    val parsed = parseStationsJson(json)
                    if (parsed.isNotEmpty()) return parsed
                } else {
                    runCatching { Log.w(TAG, "Mirror $mirror returned HTTP $responseCode for $pathWithQuery") }
                    connection.disconnect()
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                runCatching { Log.w(TAG, "Failed to query mirror $mirror: ${error.message}") }
            }
        }
        return emptyList()
    }

    /**
     * Parses the JSON array response into strongly typed [RadioBrowserStation] items.
     * Publicly visible for unit testing without live network calls.
     */
    fun parseStationsJson(jsonString: String): List<RadioBrowserStation> {
        if (jsonString.isBlank()) return emptyList()
        val results = mutableListOf<RadioBrowserStation>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val stationuuid = obj.optString("stationuuid", "")
                val name = obj.optString("name", "").trim()
                val url = obj.optString("url", "").trim()
                val urlResolved = obj.optString("url_resolved", "").trim().ifBlank { url }

                if (name.isBlank() || urlResolved.isBlank()) continue

                results.add(
                    RadioBrowserStation(
                        stationuuid = stationuuid,
                        name = name,
                        url = url,
                        urlResolved = urlResolved,
                        homepage = obj.optString("homepage", "").trim(),
                        favicon = obj.optString("favicon", "").trim(),
                        tags = obj.optString("tags", "").trim(),
                        country = obj.optString("country", "").trim(),
                        countryCode = obj.optString("countrycode", "").trim(),
                        state = obj.optString("state", "").trim(),
                        language = obj.optString("language", "").trim(),
                        votes = obj.optInt("votes", 0),
                        codec = obj.optString("codec", "").trim(),
                        bitrate = obj.optInt("bitrate", 0),
                        clickCount = obj.optInt("clickcount", 0)
                    )
                )
            }
        } catch (e: Exception) {
            runCatching { Log.e(TAG, "Error parsing stations JSON: ${e.message}") }
        }
        return results
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
