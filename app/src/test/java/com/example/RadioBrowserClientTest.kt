package com.example

import com.example.data.radio.RadioBrowserClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RadioBrowserClientTest {

    private val client = RadioBrowserClient()

    private val sampleJson = """
        [
          {
            "changeuuid": "59758b6d-0fab-42b3-9047-4e67ad90978c",
            "stationuuid": "963fa65f-0601-11e8-ae97-52543be04c81",
            "name": "Antena 1 FM 94.7",
            "url": "http://antena1.newradio.it/stream",
            "url_resolved": "http://antena1.newradio.it/stream.mp3",
            "homepage": "http://antena1.com.br/",
            "favicon": "https://antena1.com.br/logo.png",
            "tags": "adult contemporary,jazz,pop",
            "country": "Brazil",
            "countrycode": "BR",
            "state": "São Paulo",
            "language": "portuguese",
            "votes": 33268,
            "codec": "AAC+",
            "bitrate": 96,
            "clickcount": 53
          },
          {
            "changeuuid": "ff50ce39-9bc9-4dd9-bdf1-2385ccdcafb7",
            "stationuuid": "962cc6df-0601-11e8-ae97-52543be04c81",
            "name": "Dance Wave!",
            "url": "https://dancewave.online/dance.mp3",
            "url_resolved": "https://dancewave.online/dance.mp3",
            "homepage": "https://dancewave.online/",
            "favicon": "https://dancewave.online/logo.png",
            "tags": "dance,electronic,house",
            "country": "Hungary",
            "countrycode": "HU",
            "state": "",
            "language": "english",
            "votes": 570985,
            "codec": "MP3",
            "bitrate": 128,
            "clickcount": 307
          }
        ]
    """.trimIndent()

    @Test
    fun `parseStationsJson correctly deserializes JSON into list of stations`() {
        val stations = client.parseStationsJson(sampleJson)
        assertEquals(2, stations.size)

        val first = stations[0]
        assertEquals("963fa65f-0601-11e8-ae97-52543be04c81", first.stationuuid)
        assertEquals("Antena 1 FM 94.7", first.name)
        assertEquals("http://antena1.newradio.it/stream.mp3", first.playableUrl)
        assertEquals("São Paulo, Brazil", first.displayLocation)
        assertEquals("Adult contemporary, Jazz, Pop", first.displayGenre)
        assertEquals("96 kbps", first.formattedBitrate)
        assertEquals("AAC+", first.codec)
        assertEquals(33268, first.votes)
        assertEquals("🇧🇷", first.countryFlagEmoji)

        val second = stations[1]
        assertEquals("Dance Wave!", second.name)
        assertEquals("Hungary", second.displayLocation)
        assertEquals("Dance, Electronic, House", second.displayGenre)
        assertEquals("128 kbps", second.formattedBitrate)
        assertEquals(570985, second.votes)
        assertEquals("🇭🇺", second.countryFlagEmoji)
    }

    @Test
    fun `parseStationsJson handles empty or malformed input safely`() {
        assertTrue(client.parseStationsJson("").isEmpty())
        assertTrue(client.parseStationsJson("[]").isEmpty())
        assertTrue(client.parseStationsJson("{ invalid json").isEmpty())
    }

    @Test
    fun `parseStationsJson skips entries missing name or url`() {
        val invalidEntries = """
            [
              { "stationuuid": "1", "name": "", "url": "https://stream.mp3" },
              { "stationuuid": "2", "name": "Valid Radio", "url": "https://stream.mp3" }
            ]
        """.trimIndent()
        val stations = client.parseStationsJson(invalidEntries)
        assertEquals(1, stations.size)
        assertEquals("Valid Radio", stations[0].name)
    }
}
