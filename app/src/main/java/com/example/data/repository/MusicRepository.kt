package com.example.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.room.withTransaction
import com.example.data.db.AppDatabase
import com.example.data.db.LyricsDao
import com.example.data.db.PlaylistDao
import com.example.data.db.SongDao
import com.example.data.db.UserSettingsDao
import com.example.data.model.CustomPresetEntity
import com.example.data.model.LyricsEntity
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.PlaylistWithSongs
import com.example.data.model.Song
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class MusicRepository(
    private val database: AppDatabase,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val userSettingsDao: UserSettingsDao,
    private val lyricsDao: LyricsDao
) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()
    val recentlyPlayed: Flow<List<Song>> = songDao.getRecentlyPlayedSongs()
    val mostPlayed: Flow<List<Song>> = songDao.getMostPlayedSongs()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val allPlaylistsWithSongs: Flow<List<PlaylistWithSongs>> = combine(
        allPlaylists,
        allSongs,
        playlistDao.observeAllCrossRefs()
    ) { playlists, songs, crossRefs ->
        val songsById = songs.associateBy { it.id }
        val refsByPlaylist = crossRefs.groupBy { it.playlistId }
        playlists.map { playlist ->
            val orderedSongs = refsByPlaylist[playlist.id]
                .orEmpty()
                .sortedWith(compareBy<PlaylistSongCrossRef> { it.orderIndex }.thenBy { it.addedAt })
                .mapNotNull { songsById[it.songId] }
            PlaylistWithSongs(playlist, orderedSongs)
        }
    }
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getUserSettings()
    val customPresets: Flow<List<CustomPresetEntity>> = userSettingsDao.getAllCustomPresets()

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        userSettingsDao.saveUserSettings(settings)
    }

    suspend fun getUserSettingsOnce(): UserSettingsEntity {
        return userSettingsDao.getUserSettingsOnce() ?: UserSettingsEntity()
    }

    suspend fun updateLastPlayedState(songId: Long?, positionMs: Long) {
        userSettingsDao.updateLastPlayedState(songId, positionMs.coerceAtLeast(0L))
    }

    suspend fun saveCustomPreset(preset: CustomPresetEntity): Long {
        return userSettingsDao.insertCustomPreset(preset)
    }

    suspend fun deleteCustomPreset(id: Long) {
        userSettingsDao.deleteCustomPresetById(id)
    }

    suspend fun getLyricsOnce(songId: Long): LyricsEntity? = lyricsDao.getLyricsOnce(songId)

    suspend fun saveLyrics(lyrics: LyricsEntity) = lyricsDao.saveLyrics(lyrics)

    suspend fun deleteLyrics(songId: Long) = lyricsDao.deleteLyrics(songId)

    data class BackupSnapshot(
        val songs: List<Song>,
        val playlists: List<Playlist>,
        val crossRefs: List<PlaylistSongCrossRef>,
        val userSettings: UserSettingsEntity?,
        val customPresets: List<CustomPresetEntity>,
        val lyrics: List<LyricsEntity>
    )

    suspend fun exportBackupSnapshot(): BackupSnapshot = withContext(Dispatchers.IO) {
        database.withTransaction {
            BackupSnapshot(
                songs = songDao.getAllSongsOnce(),
                playlists = playlistDao.getAllPlaylistsOnce(),
                crossRefs = playlistDao.getAllCrossRefsOnce(),
                userSettings = userSettingsDao.getUserSettingsOnceForBackup(),
                customPresets = userSettingsDao.getAllCustomPresetsOnce(),
                lyrics = lyricsDao.getAllLyrics()
            )
        }
    }

    suspend fun restoreBackupSnapshot(snapshot: BackupSnapshot): List<Song> = withContext(Dispatchers.IO) {
        database.withTransaction {
            playlistDao.clearAllCrossRefs()
            lyricsDao.clearAllLyrics()
            playlistDao.clearAllPlaylists()
            songDao.clearAllSongs()
            userSettingsDao.clearAllCustomPresets()
            userSettingsDao.clearUserSettings()

            songDao.restoreSongs(snapshot.songs)
            if (snapshot.playlists.isNotEmpty()) {
                playlistDao.insertPlaylists(snapshot.playlists)
            }
            if (snapshot.crossRefs.isNotEmpty()) {
                playlistDao.restoreCrossRefs(snapshot.crossRefs)
            }
            // Older backups may not contain settings. Keep the singleton row
            // present so later UPDATE queries (playback position/equalizer)
            // are not silently applied to zero rows.
            userSettingsDao.saveUserSettings(snapshot.userSettings ?: UserSettingsEntity())
            if (snapshot.customPresets.isNotEmpty()) {
                userSettingsDao.restoreCustomPresets(snapshot.customPresets)
            }
            if (snapshot.lyrics.isNotEmpty()) {
                lyricsDao.restoreLyrics(snapshot.lyrics)
            }
        }
        snapshot.songs
    }

    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?> = combine(
        playlistDao.getPlaylistWithSongs(playlistId),
        playlistDao.observeCrossRefsForPlaylist(playlistId)
    ) { playlistWithSongs, crossRefs ->
        playlistWithSongs?.let { relation ->
            val songsById = relation.songs.associateBy { it.id }
            relation.copy(
                songs = crossRefs
                    .sortedWith(compareBy<PlaylistSongCrossRef> { it.orderIndex }.thenBy { it.addedAt })
                    .mapNotNull { songsById[it.songId] }
            )
        }
    }

    fun searchSongs(query: String): Flow<List<Song>> =
        songDao.searchSongs(query)

    fun searchSongsByTitleOrArtist(query: String): Flow<List<Song>> =
        songDao.searchSongsByTitleOrArtist(query)

    fun searchAndSortSongs(query: String, sort: String): Flow<List<Song>> =
        songDao.searchAndSortSongs(query, sort)

    suspend fun insertSong(song: Song): Long =
        songDao.insertSong(song)

    suspend fun toggleFavorite(song: Song) {
        songDao.updateFavorite(song.id, !song.isFavorite)
    }

    suspend fun recordSongPlayed(songId: Long) {
        songDao.recordSongPlayed(songId)
    }

    suspend fun deleteSong(song: Song) {
        database.withTransaction {
            // Lyrics do not have a foreign key to songs because older schemas
            // allowed them to exist independently. Remove them explicitly so
            // deleting a track cannot leave orphaned lyrics behind.
            lyricsDao.deleteLyrics(song.id)
            songDao.deleteSongById(song.id)
        }
    }

    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

    suspend fun relinkSong(context: Context, songId: Long, uri: Uri): Song {
        val current = requireNotNull(songDao.getSongByIdOnce(songId)) {
            "A música selecionada não está mais na biblioteca."
        }
        val sourceKey = uri.toString()
        songDao.getSongBySourceKey(sourceKey)
            ?.takeIf { it.id != songId }
            ?.let { throw IllegalArgumentException("Esse arquivo já está na biblioteca.") }

        ensureReadable(context, uri)
        val metadata = readAudioMetadata(context, uri)
        val updated = current.copy(
            mediaUri = sourceKey,
            sourceKey = sourceKey,
            durationMs = metadata.durationMs.takeIf { it > 0L } ?: current.durationMs,
            isAvailable = true,
            isMediaStoreItem = false
        )
        songDao.updateSong(updated)
        return updated
    }

    suspend fun createPlaylist(name: String, description: String = "", iconName: String = "queue_music", gradientIndex: Int = 0): Long {
        val cleanName = name.trim().take(1_000)
        if (cleanName.isBlank()) return -1L
        val playlist = Playlist(
            name = cleanName,
            description = description.trim().take(5_000),
            gradientIndex = gradientIndex.coerceIn(0, 5),
            iconName = iconName.trim().take(100).ifBlank { "queue_music" }
        )
        return playlistDao.insertPlaylist(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun deletePlaylistById(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(playlistId, songId)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    /**
     * Scans device storage for audio tracks via MediaStore
     */
    suspend fun scanDeviceAudio(context: Context): Int = withContext(Dispatchers.IO) {
        var importedCount = 0
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 15000"

            contentResolver.query(uri, projection, selection, null, "${MediaStore.Audio.Media.TITLE} ASC")?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                val scannedSongs = mutableListOf<Song>()
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Faixa Desconhecida"
                    val artist = cursor.getString(artistCol) ?: "Artista Desconhecido"
                    val album = cursor.getString(albumCol) ?: "Álbum Desconhecido"
                    val duration = cursor.getLong(durationCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()

                    var albumArtUri: String? = null
                    if (albumIdCol != -1) {
                        val albumId = cursor.getLong(albumIdCol)
                        val artworkUri = "content://media/external/audio/albumart".toUri()
                        albumArtUri = ContentUris.withAppendedId(artworkUri, albumId).toString()
                    }

                    val covers = listOf("cover_synthwave", "cover_lofi", "cover_acoustic", "cover_electronic")
                    val cover = covers[(id % covers.size).toInt().coerceIn(0, covers.size - 1)]

                    scannedSongs.add(
                        Song(
                            title = title,
                            artist = if (artist == "<unknown>") "Artista Desconhecido" else artist,
                            album = if (album == "<unknown>") "Álbum Desconhecido" else album,
                            durationMs = duration,
                            mediaUri = contentUri,
                            sourceKey = contentUri,
                            isAvailable = true,
                            isMediaStoreItem = true,
                            coverDrawableName = cover,
                            coverUri = albumArtUri,
                            genre = "Áudio Local"
                        )
                    )
                }

                if (scannedSongs.isNotEmpty()) {
                    // MediaStore providers should not duplicate rows, but some OEMs do.
                    // De-duplicate before querying/inserting to keep rescans idempotent.
                    val uniqueSongs = scannedSongs.distinctBy { it.mediaUri }
                    val sourceKeys = uniqueSongs.mapNotNull { it.sourceKey }
                    val existingSongs = songDao.getSongsBySourceKeys(sourceKeys).associateBy { it.sourceKey }
                    val newSongs = uniqueSongs.filter { it.sourceKey !in existingSongs }
                    val ids = songDao.insertSongs(newSongs)
                    importedCount = ids.count { it != -1L }
                }

                // Reconcile only MediaStore-managed rows. Files imported with
                // the document picker are intentionally not touched here.
                songDao.markMediaStoreSongsUnavailable()
                val scannedSourceKeys = scannedSongs.mapNotNull { it.sourceKey }.distinct()
                if (scannedSourceKeys.isNotEmpty()) {
                    songDao.markSourcesAvailable(scannedSourceKeys)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error scanning device audio", e)
            throw IllegalStateException("Não foi possível escanear as músicas do dispositivo.", e)
        }
        importedCount
    }

    suspend fun importAudioUri(context: Context, uri: Uri, title: String, artist: String = "Importado"): Long {
        val sourceKey = uri.toString()
        songDao.getSongBySourceKey(sourceKey)?.let { existing ->
            // Re-importing the same document is also how a user repairs a
            // previously revoked URI permission. Verify access and mark the
            // row available again without discarding edited metadata.
            ensureReadable(context, uri)
            if (!existing.isAvailable) {
                songDao.updateSong(existing.copy(isAvailable = true))
            }
            return existing.id
        }

        ensureReadable(context, uri)
        val metadata = readAudioMetadata(context, uri)
        val song = Song(
            title = title.ifBlank { metadata.title ?: "Faixa Desconhecida" },
            artist = metadata.artist?.takeIf { it.isNotBlank() && it != "<unknown>" } ?: artist,
            album = metadata.album?.takeIf { it.isNotBlank() && it != "<unknown>" } ?: "Músicas Locais",
            durationMs = metadata.durationMs.coerceAtLeast(0L),
            mediaUri = uri.toString(),
            sourceKey = sourceKey,
            isAvailable = true,
            isMediaStoreItem = false,
            coverDrawableName = "cover_synthwave",
            genre = "Arquivo de Áudio"
        )
        return try {
            songDao.insertSong(song)
        } catch (e: Exception) {
            // Two picker callbacks can arrive close together. The unique
            // sourceKey index is the source of truth; return the row created
            // by the competing insert instead of reporting a false failure.
            songDao.getSongBySourceKey(sourceKey)?.id ?: throw e
        }
    }

    private data class AudioMetadata(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val durationMs: Long = 0L
    )

    private fun readAudioMetadata(context: Context, uri: Uri): AudioMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
                retriever.setDataSource(context, uri)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                AudioMetadata(
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    durationMs = durationStr?.toLongOrNull() ?: 0L
                )
        } catch (e: Exception) {
            Log.w("MusicRepository", "Could not read audio metadata for $uri", e)
            AudioMetadata()
        } finally {
            retriever.release()
        }
    }

    private fun ensureReadable(context: Context, uri: Uri) {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Não foi possível acessar o arquivo selecionado.")
        require(input.use { it.read() >= 0 }) {
            "O arquivo selecionado está vazio ou não pode ser lido."
        }
    }
}
