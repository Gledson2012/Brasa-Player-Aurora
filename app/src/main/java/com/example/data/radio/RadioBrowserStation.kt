package com.example.data.radio

/**
 * Model representing a radio station returned by the Radio Browser API
 * (https://github.com/ivandotv/radio-browser-api / https://api.radio-browser.info).
 */
data class RadioBrowserStation(
    val stationuuid: String,
    val name: String,
    val url: String,
    val urlResolved: String,
    val homepage: String = "",
    val favicon: String = "",
    val tags: String = "",
    val country: String = "",
    val countryCode: String = "",
    val state: String = "",
    val language: String = "",
    val votes: Int = 0,
    val codec: String = "",
    val bitrate: Int = 0,
    val clickCount: Int = 0
) {
    val displayGenre: String
        get() {
            if (tags.isBlank()) return "Geral"
            val list = tags.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
            return if (list.isEmpty()) "Geral"
            else list.take(3).joinToString(", ") { tag ->
                tag.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }

    val displayLocation: String
        get() {
            return when {
                state.isNotBlank() && country.isNotBlank() -> "$state, $country"
                country.isNotBlank() -> country
                state.isNotBlank() -> state
                else -> "Mundial"
            }
        }

    val playableUrl: String
        get() = urlResolved.ifBlank { url }

    val formattedBitrate: String
        get() = if (bitrate > 0) "${bitrate} kbps" else ""

    val countryFlagEmoji: String
        get() {
            val code = countryCode.trim().uppercase()
            if (code.length != 2) return "📻"
            val first = code[0].code - 'A'.code + 0x1F1E6
            val second = code[1].code - 'A'.code + 0x1F1E6
            return String(Character.toChars(first)) + String(Character.toChars(second))
        }
}
