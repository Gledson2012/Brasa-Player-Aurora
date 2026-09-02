package com.example.data.model

/**
 * Aggregated listening statistics for the Statistics screen.
 */
data class ArtistStat(val artist: String, val count: Int)
data class GenreStat(val genre: String, val count: Int)
data class AlbumStat(val album: String, val artist: String, val count: Int)

data class ListeningStatistics(
    val totalSongs: Int = 0,
    val favoriteSongs: Int = 0,
    val uniqueArtists: Int = 0,
    val uniqueAlbums: Int = 0,
    val uniqueGenres: Int = 0,
    val totalPlayCount: Int = 0,
    val totalLibraryDurationMs: Long = 0L,
    val totalListenedDurationMs: Long = 0L,
    val topArtists: List<ArtistStat> = emptyList(),
    val topGenres: List<GenreStat> = emptyList(),
    val topAlbums: List<AlbumStat> = emptyList(),
    val mostPlayedSongs: List<Song> = emptyList()
) {
    val totalLibraryDurationFormatted: String get() = formatDuration(totalLibraryDurationMs)
    val totalListenedDurationFormatted: String get() = formatDuration(totalListenedDurationMs)

    private fun formatDuration(ms: Long): String {
        val totalMinutes = ms / 1000 / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes} min"
    }
}
