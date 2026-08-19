package com.example.data.lyrics

import android.util.Log
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class TrackLyrics(
    val songId: Long,
    val lines: List<LyricLine>,
    val isSynced: Boolean = true,
    val source: String = "LRC"
)

object LyricsManager {

    private val lyricsCache = ConcurrentHashMap<Long, TrackLyrics>()

    fun cache(songId: Long, lyrics: TrackLyrics) {
        lyricsCache[songId] = lyrics
    }

    fun clearCache(songId: Long) {
        lyricsCache.remove(songId)
    }

    fun toLrc(lyrics: TrackLyrics): String = lyrics.lines.joinToString("\n") { line ->
        val totalCentiseconds = line.timestampMs.coerceAtLeast(0L) / 10L
        val minutes = totalCentiseconds / 6000L
        val seconds = (totalCentiseconds / 100L) % 60L
        val centiseconds = totalCentiseconds % 100L
        "[%02d:%02d.%02d] %s".format(minutes, seconds, centiseconds, line.text)
    }

    /**
     * Parses standard LRC text into a structured TrackLyrics object.
     */
    fun parseLrc(songId: Long, lrcContent: String, source: String = "LRC"): TrackLyrics {
        val lines = mutableListOf<LyricLine>()
        val rawLines = lrcContent.lines()

        val timePattern = Pattern.compile("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?]""")

        for (line in rawLines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            var lastEnd = 0
            val matcher = timePattern.matcher(trimmed)
            var hasTimestamp = false
            val timestamps = mutableListOf<Long>()

            while (matcher.find(lastEnd)) {
                hasTimestamp = true
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                val fractionStr = matcher.group(3) ?: "0"
                val millis = when (fractionStr.length) {
                    1 -> fractionStr.toLong() * 100
                    2 -> fractionStr.toLong() * 10
                    else -> fractionStr.take(3).toLong()
                }
                val totalTimestampMs = (minutes * 60 * 1000L) + (seconds * 1000L) + millis
                timestamps += totalTimestampMs
                lastEnd = matcher.end()
            }

            val text = trimmed.substring(lastEnd).trim()
            if (text.isNotEmpty()) {
                timestamps.forEach { timestampMs ->
                    lines.add(LyricLine(timestampMs, text))
                }
            }

            if (!hasTimestamp && !trimmed.startsWith("[") && trimmed.isNotEmpty()) {
                lines.add(LyricLine(0L, trimmed))
            }
        }

        val sortedLines = lines.sortedBy { it.timestampMs }
        val isSynced = sortedLines.any { it.timestampMs > 0L }
        return TrackLyrics(songId, sortedLines, isSynced, source)
    }

    /**
     * Retrieves synchronized lyrics for a song, checking cache, built-in library, and LRCLIB online API.
     */
    suspend fun getLyrics(song: Song): TrackLyrics = withContext(Dispatchers.IO) {
        lyricsCache[song.id]?.let { return@withContext it }

        // 1. Check built-in preset lyrics
        val presetLrc = getBuiltInLrc(song)
        if (presetLrc != null) {
            val parsed = parseLrc(song.id, presetLrc, "Procedural Synth")
            lyricsCache[song.id] = parsed
            return@withContext parsed
        }

        // 2. Try fetching from public synced lyrics provider (LRCLIB)
        try {
            val onlineLyrics = fetchFromLrclib(song.artist, song.title, song.album, song.durationMs / 1000)
            if (onlineLyrics != null) {
                val parsed = parseLrc(song.id, onlineLyrics, "LRCLIB")
                lyricsCache[song.id] = parsed
                return@withContext parsed
            }
        } catch (e: Exception) {
            Log.w("LyricsManager", "Failed to fetch online lyrics for ${song.title}: ${e.message}")
        }

        // Do not present fabricated lyrics for arbitrary local files. The UI
        // can offer a retry or let the user edit real lyrics manually.
        val unavailable = TrackLyrics(song.id, emptyList(), false, "Não encontrado")
        lyricsCache[song.id] = unavailable
        return@withContext unavailable
    }

    private fun fetchFromLrclib(artist: String, title: String, album: String, durationSec: Long): String? {
        val encodedArtist = URLEncoder.encode(artist.trim(), "UTF-8")
        val encodedTitle = URLEncoder.encode(title.trim(), "UTF-8")
        val encodedAlbum = URLEncoder.encode(album.trim(), "UTF-8")
        val urlString = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTitle&album_name=$encodedAlbum&duration=$durationSec"

        val url = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 4000
            readTimeout = 4000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "BeatFlow-MusicPlayer/1.0")
        }

        return try {
            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                if (json.has("syncedLyrics") && !json.isNull("syncedLyrics")) {
                    val synced = json.getString("syncedLyrics")
                    if (synced.isNotBlank()) return synced
                }
                if (json.has("plainLyrics") && !json.isNull("plainLyrics")) {
                    return json.getString("plainLyrics")
                }
            }
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun getBuiltInLrc(song: Song): String? {
        val title = song.title.lowercase()
        return when {
            title.contains("cyberpunk") || title.contains("synthwave") -> """
                [00:00.00] ♪ Introdução Neon Synthwave ♪
                [00:08.50] Luzes de neon refletem no asfalto molhado
                [00:15.20] Cruzando a cidade em alta velocidade
                [00:23.40] Sintetizadores ecoam na escuridão
                [00:31.00] A pulsação da meia-noite toma conta
                [00:39.80] O horizonte elétrico nunca dorme
                [00:48.00] ♪ Solo de Sintetizador Analógico ♪
                [01:04.20] Frequências quentes no oscilador
                [01:12.50] Conectado na rede neural do som
                [01:21.00] Ritmo constante, graves profundos
                [01:29.40] Vibração infinita pela madrugada
                [01:38.00] ♪ Drop Synthwave Energético ♪
                [01:54.50] No reflexo do vidro, a cidade se transforma
                [02:03.00] O futuro chegou na batida
                [02:11.80] Desacelerando pelas avenidas iluminadas
                [02:22.00] ♪ Finalização Suave ♪
            """.trimIndent()

            title.contains("chill") || title.contains("lo-fi") || title.contains("beats") -> """
                [00:00.00] ♪ Vinil Crepitando & Batida Suave ♪
                [00:09.00] Uma xícara de café quente na janela
                [00:18.20] Pingos de chuva batem no vidro
                [00:27.50] Foco suave nas páginas abertas
                [00:36.80] O tempo desacelera tranquilamente
                [00:46.00] ♪ Acordes de Piano Rhodes Suaves ♪
                [00:58.20] Respiração calma, pensamentos fluindo
                [01:08.50] Linha de baixo aveludada e quente
                [01:19.00] A mente encontra seu espaço de paz
                [01:30.00] Nenhuma pressa no final da tarde
                [01:41.20] ♪ Solo Melódico Lo-Fi ♪
                [01:56.00] Deixe a música levar o estresse embora
                [02:08.00] Sintonia perfeita com o momento
                [02:20.00] ♪ Desvanecimento Calmante ♪
            """.trimIndent()

            title.contains("pulse") || title.contains("electronic") -> """
                [00:00.00] ♪ Carregando Frequências Eletrônicas ♪
                [00:07.50] 128 BPM na veia
                [00:14.20] Subindo o filtro passa-baixas
                [00:21.00] Preparando o impacto sonoro
                [00:28.50] 3... 2... 1...
                [00:30.00] ♪ DROP PRINCIPAL - Pulso Eletrizante ♪
                [00:45.00] As ondas sonoras dominam o ambiente
                [00:52.80] Sintetizadores modulam em uníssono
                [01:00.20] Energia pura em cada ciclo
                [01:08.00] ♪ Ponte Harmônica & Build-up ♪
                [01:22.50] O crescendo ganha força
                [01:30.00] ♪ SEGUNDO DROP - Intensidade Máxima ♪
                [01:45.20] Bumbo e baixo em perfeita sincronia
                [01:54.00] Ressonância cristalina no ar
                [02:05.00] ♪ Outro & Fade-out Gradual ♪
            """.trimIndent()

            title.contains("dream") || title.contains("ambient") -> """
                [00:00.00] ♪ Texturas Atmosféricas Flutuantes ♪
                [00:12.00] Expandindo o espaço mental
                [00:25.50] Ondas sonoras serenas e infinitas
                [00:39.00] Sons etéreos que dissolvem o tempo
                [00:54.20] Flutuando entre nuvens de reverberação
                [01:10.00] Harmonia cósmica em 432 Hz
                [01:26.50] Paz profunda e imersão total
                [01:44.00] O silêncio e o som se encontram
                [02:02.00] ♪ Dissolução Gradual no Infinito ♪
            """.trimIndent()

            title.contains("funk") || title.contains("retro") -> """
                [00:00.00] ♪ Groove de Baixo Slap & Bateria 80s ♪
                [00:08.00] Bota o fone e sente esse balanço
                [00:15.50] O groove dos sintetizadores vintage
                [00:23.00] Ritmo contagiante na pista
                [00:31.20] Linha de baixo estalando forte
                [00:39.00] ♪ Solo de Teclado Clavinet ♪
                [00:48.50] Não dá pra ficar parado nessa vibração
                [00:56.80] Brass de sintetizador brilhando alto
                [01:06.00] Swing clássico com energia moderna
                [01:15.00] ♪ Break de Percussão & Funk Drop ♪
                [01:32.00] Solta o som no talo!
                [01:45.00] ♪ Finalização Groovy ♪
            """.trimIndent()

            title.contains("house") || title.contains("groove") || title.contains("deep") -> """
                [00:00.00] ♪ Kick 4x4 & Hi-Hats Aveludados ♪
                [00:15.00] Linha de sub-grave profunda
                [00:23.00] Acordes de órgão house em loop
                [00:31.00] Atmosfera sofisticada e envolvente
                [00:47.00] ♪ Entrada da Melodia Principal ♪
                [01:02.50] O fluxo da música nunca para
                [01:18.00] Batida hipnótica na madrugada
                [01:34.00] ♪ Clímax Deep House ♪
                [01:50.00] O groove que move a pista
                [02:10.00] ♪ Outro Deep & Progressivo ♪
            """.trimIndent()

            else -> null
        }
    }

}
