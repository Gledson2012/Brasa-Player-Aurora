package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.datastore.ThemePreferencesDataStore
import com.example.data.model.AlbumArtStyle
import com.example.data.model.AppThemeType
import com.example.data.model.CustomPresetEntity
import com.example.data.model.CustomThemeConfig
import com.example.data.model.LyricsEntity
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettingsEntity
import com.example.data.model.VisualizerStyle
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(
    private val repository: MusicRepository,
    private val themeDataStore: ThemePreferencesDataStore
) {
    private companion object {
        const val MAX_SONGS = 100_000
        const val MAX_PLAYLISTS = 10_000
    }

    suspend fun export(context: Context, uri: Uri, onProgress: ((String) -> Unit)? = null) {
        onProgress?.invoke("Preparando dados...")
        val root = createBackupJson()
        onProgress?.invoke("Compactando backup...")
        val jsonBytes = root.toString(2).toByteArray(Charsets.UTF_8)
        val output = requireNotNull(context.contentResolver.openOutputStream(uri))
        output.use { outputStream ->
            BackupPayloadCodec.writeGzip(outputStream, jsonBytes)
        }
        onProgress?.invoke("Backup concluído.")
    }

    suspend fun restore(context: Context, uri: Uri, onProgress: ((String) -> Unit)? = null): List<Song> {
        onProgress?.invoke("Lendo backup...")
        val input = requireNotNull(context.contentResolver.openInputStream(uri))
        val jsonText = input.use(BackupPayloadCodec::decode)
        val json = JSONObject(jsonText)
        require(json.optInt("version", 0) in 1..2) { "Formato de backup não suportado." }

        val snapshot = MusicRepository.BackupSnapshot(
            songs = json.array("songs", ::jsonToSong),
            playlists = json.array("playlists", ::jsonToPlaylist),
            crossRefs = json.array("crossRefs", ::jsonToCrossRef),
            userSettings = json.optJSONObject("userSettings")?.let(::jsonToSettings),
            customPresets = json.array("customPresets", ::jsonToPreset),
            lyrics = json.array("lyrics", ::jsonToLyrics)
        )
        validateSnapshot(snapshot)

        // Keep a recoverable local copy before replacing the database. This is
        // useful when a user selects a malformed or incomplete backup.
        onProgress?.invoke("Criando backup de segurança...")
        writeEmergencyBackup(context)
        onProgress?.invoke("Restaurando dados...")
        val songs = repository.restoreBackupSnapshot(snapshot)

        json.optJSONObject("theme")?.let { themeDataStore.restore(jsonToTheme(it)) }
        // Last.fm credentials are deliberately excluded from backups and are
        // therefore left untouched during a restore.
        return songs
    }

    private suspend fun createBackupJson(): JSONObject {
        val snapshot = repository.exportBackupSnapshot()
        val themeConfig = themeDataStore.themeConfigFlow.first()
        return JSONObject()
            .put("version", 2)
            .put("createdAt", System.currentTimeMillis())
            .put("songs", JSONArray(snapshot.songs.map(::songToJson)))
            .put("playlists", JSONArray(snapshot.playlists.map(::playlistToJson)))
            .put("crossRefs", JSONArray(snapshot.crossRefs.map(::crossRefToJson)))
            .put("customPresets", JSONArray(snapshot.customPresets.map(::presetToJson)))
            .put("lyrics", JSONArray(snapshot.lyrics.map(::lyricsToJson)))
            .put("userSettings", snapshot.userSettings?.let(::settingsToJson) ?: JSONObject.NULL)
            .put("theme", themeToJson(themeConfig))
            .put("lastFm", lastFmToJson())
    }

    private suspend fun writeEmergencyBackup(context: Context) {
        val backupDir = context.getDir("backup", Context.MODE_PRIVATE)
        val file = backupDir.resolve("pre-restore-${System.currentTimeMillis()}.json.gz")
        val temporary = backupDir.resolve(".${file.name}.tmp")
        val jsonBytes = createBackupJson().toString(2).toByteArray(Charsets.UTF_8)
        temporary.outputStream().use { outputStream ->
            BackupPayloadCodec.writeGzip(outputStream, jsonBytes)
        }
        require(temporary.renameTo(file)) { "Não foi possível criar o backup de segurança." }
        backupDir.listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?.drop(3)
            ?.forEach { it.delete() }
    }

    private fun songToJson(song: Song) = JSONObject()
        .put("id", song.id)
        .put("title", song.title)
        .put("artist", song.artist)
        .put("album", song.album)
        .put("durationMs", song.durationMs)
        .put("mediaUri", song.mediaUri)
        .put("sourceKey", song.sourceKey ?: JSONObject.NULL)
        .put("isAvailable", song.isAvailable)
        .put("isMediaStoreItem", song.isMediaStoreItem)
        .put("coverDrawableName", song.coverDrawableName)
        .put("coverUri", song.coverUri ?: JSONObject.NULL)
        .put("genre", song.genre)
        .put("isFavorite", song.isFavorite)
        .put("playCount", song.playCount)
        .put("lastPlayedTimestamp", song.lastPlayedTimestamp)
        .put("addedTimestamp", song.addedTimestamp)
        .put("synthPreset", song.synthPreset ?: JSONObject.NULL)

    private fun jsonToSong(json: JSONObject) = Song(
        id = json.getLong("id"),
        title = json.getString("title"),
        artist = json.getString("artist"),
        album = json.getString("album"),
        durationMs = json.getLong("durationMs"),
        mediaUri = json.optString("mediaUri"),
        sourceKey = json.optNullableString("sourceKey")
            ?: json.optNullableString("mediaUri"),
        isAvailable = json.optBoolean("isAvailable", true),
        isMediaStoreItem = json.optBoolean("isMediaStoreItem", false),
        coverDrawableName = json.optString("coverDrawableName", "cover_synthwave"),
        coverUri = json.optNullableString("coverUri"),
        genre = json.optString("genre", "Geral"),
        isFavorite = json.optBoolean("isFavorite"),
        playCount = json.optInt("playCount"),
        lastPlayedTimestamp = json.optLong("lastPlayedTimestamp"),
        addedTimestamp = json.optLong("addedTimestamp", System.currentTimeMillis()),
        synthPreset = json.optNullableString("synthPreset")
    )

    private fun playlistToJson(playlist: Playlist) = JSONObject()
        .put("id", playlist.id)
        .put("name", playlist.name)
        .put("description", playlist.description)
        .put("gradientIndex", playlist.gradientIndex)
        .put("iconName", playlist.iconName)
        .put("createdAt", playlist.createdAt)
        .put("isSmart", playlist.isSmart)

    private fun jsonToPlaylist(json: JSONObject) = Playlist(
        id = json.getLong("id"),
        name = json.getString("name"),
        description = json.optString("description"),
        gradientIndex = json.optInt("gradientIndex"),
        iconName = json.optString("iconName", "playlist_play"),
        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
        isSmart = json.optBoolean("isSmart")
    )

    private fun crossRefToJson(ref: PlaylistSongCrossRef) = JSONObject()
        .put("playlistId", ref.playlistId)
        .put("songId", ref.songId)
        .put("orderIndex", ref.orderIndex)
        .put("addedAt", ref.addedAt)

    private fun jsonToCrossRef(json: JSONObject) = PlaylistSongCrossRef(
        playlistId = json.getLong("playlistId"),
        songId = json.getLong("songId"),
        orderIndex = json.optInt("orderIndex"),
        addedAt = json.optLong("addedAt", System.currentTimeMillis())
    )

    private fun settingsToJson(settings: UserSettingsEntity) = JSONObject()
        .put("id", settings.id)
        .put("themeName", settings.themeName)
        .put("visualizerStyle", settings.visualizerStyle)
        .put("albumArtStyle", settings.albumArtStyle)
        .put("dynamicColors", settings.dynamicColors)
        .put("equalizerEnabled", settings.equalizerEnabled)
        .put("currentPresetId", settings.currentPresetId)
        .put("band0", settings.band0).put("band1", settings.band1)
        .put("band2", settings.band2).put("band3", settings.band3).put("band4", settings.band4)
        .put("bassBoost", settings.bassBoost).put("virtualizer", settings.virtualizer)
        .put("balance", settings.balance).put("playbackSpeed", settings.playbackSpeed)
        .put("crossfadeSeconds", settings.crossfadeSeconds)
        .put("repeatMode", settings.repeatMode).put("isShuffle", settings.isShuffle)
        .put("lastPlayedSongId", settings.lastPlayedSongId ?: JSONObject.NULL)
        .put("lastPlaybackPositionMs", settings.lastPlaybackPositionMs)

    private fun jsonToSettings(json: JSONObject) = UserSettingsEntity(
        id = json.optInt("id", 1),
        themeName = json.optString("themeName", "MIDNIGHT_OLED"),
        visualizerStyle = json.optString("visualizerStyle", "BARS"),
        albumArtStyle = json.optString("albumArtStyle", "VINYL_ROTATION"),
        dynamicColors = json.optBoolean("dynamicColors"),
        equalizerEnabled = json.optBoolean("equalizerEnabled", true),
        currentPresetId = json.optString("currentPresetId", "flat"),
        band0 = json.optInt("band0"), band1 = json.optInt("band1"), band2 = json.optInt("band2"),
        band3 = json.optInt("band3"), band4 = json.optInt("band4"),
        bassBoost = json.optInt("bassBoost", 0), virtualizer = json.optInt("virtualizer", 0),
        balance = json.optDouble("balance", 0.0).toFloat(),
        playbackSpeed = json.optDouble("playbackSpeed", 1.0).toFloat(),
        crossfadeSeconds = json.optInt("crossfadeSeconds", 0),
        repeatMode = json.optString("repeatMode", "ALL"), isShuffle = json.optBoolean("isShuffle"),
        lastPlayedSongId = json.optNullableLong("lastPlayedSongId"),
        lastPlaybackPositionMs = json.optLong("lastPlaybackPositionMs")
    )

    private fun presetToJson(preset: CustomPresetEntity) = JSONObject()
        .put("id", preset.id).put("name", preset.name)
        .put("band0", preset.band0).put("band1", preset.band1).put("band2", preset.band2)
        .put("band3", preset.band3).put("band4", preset.band4)
        .put("bassBoost", preset.bassBoost).put("virtualizer", preset.virtualizer)

    private fun jsonToPreset(json: JSONObject) = CustomPresetEntity(
        id = json.optLong("id"), name = json.getString("name"),
        band0 = json.optInt("band0"), band1 = json.optInt("band1"), band2 = json.optInt("band2"),
        band3 = json.optInt("band3"), band4 = json.optInt("band4"),
        bassBoost = json.optInt("bassBoost"), virtualizer = json.optInt("virtualizer")
    )

    private fun lyricsToJson(lyrics: LyricsEntity) = JSONObject()
        .put("songId", lyrics.songId).put("content", lyrics.content)
        .put("isSynced", lyrics.isSynced).put("source", lyrics.source)

    private fun jsonToLyrics(json: JSONObject) = LyricsEntity(
        songId = json.getLong("songId"), content = json.getString("content"),
        isSynced = json.optBoolean("isSynced", true), source = json.optString("source", "Editor")
    )

    private fun themeToJson(theme: ThemeConfig) = JSONObject()
        .put("themeMode", theme.themeMode.name).put("presetTheme", theme.presetTheme.name)
        .put("dynamicColors", theme.dynamicColors).put("visualizerStyle", theme.visualizerStyle.name)
        .put("albumArtStyle", theme.albumArtStyle.name)
        .put("customPrimary", theme.customTheme.primaryColorVal)
        .put("customSecondary", theme.customTheme.secondaryColorVal)
        .put("customTertiary", theme.customTheme.tertiaryColorVal)
        .put("customSurface", theme.customTheme.surfaceColorVal)
        .put("customBackground", theme.customTheme.backgroundColorVal)
        .put("customIsDark", theme.customTheme.isDark)

    private fun jsonToTheme(json: JSONObject): ThemeConfig = ThemeConfig(
        themeMode = enumOrDefault(json.optString("themeMode"), ThemeMode.SYSTEM),
        presetTheme = enumOrDefault(json.optString("presetTheme"), AppThemeType.MIDNIGHT_OLED),
        dynamicColors = json.optBoolean("dynamicColors"),
        visualizerStyle = enumOrDefault(json.optString("visualizerStyle"), VisualizerStyle.BARS),
        albumArtStyle = enumOrDefault(json.optString("albumArtStyle"), AlbumArtStyle.VINYL_ROTATION),
        customTheme = CustomThemeConfig(
            primaryColorVal = json.optLong("customPrimary", 0xFF9D4EDDL),
            secondaryColorVal = json.optLong("customSecondary", 0xFF00F0FFL),
            tertiaryColorVal = json.optLong("customTertiary", 0xFFFF007FL),
            surfaceColorVal = json.optLong("customSurface", 0xFF140F22L),
            backgroundColorVal = json.optLong("customBackground", 0xFF080512L),
            isDark = json.optBoolean("customIsDark", true)
        )
    )

    // Credentials and session tokens must never leave the app in a backup file.
    private fun lastFmToJson() = JSONObject()
        .put("credentialsIncluded", false)
        .put("enabled", false)

    private fun validateSnapshot(snapshot: MusicRepository.BackupSnapshot) {
        require(snapshot.songs.size <= MAX_SONGS) { "Backup excede o limite de músicas permitido." }
        require(snapshot.playlists.size <= MAX_PLAYLISTS) { "Backup excede o limite de playlists permitido." }
        require(snapshot.songs.all { it.title.length <= 1_000 && it.artist.length <= 1_000 }) {
            "Backup contém metadados de música inválidos."
        }
        val songIds = snapshot.songs.map { it.id }
        val playlistIds = snapshot.playlists.map { it.id }
        val sourceKeys = snapshot.songs.mapNotNull { it.sourceKey }
        require(songIds.all { it > 0 } && songIds.toSet().size == songIds.size) {
            "Backup contém IDs de músicas inválidos."
        }
        require(playlistIds.all { it > 0 } && playlistIds.toSet().size == playlistIds.size) {
            "Backup contém IDs de playlists inválidos."
        }
        require(sourceKeys.all { it.isNotBlank() } && sourceKeys.toSet().size == sourceKeys.size) {
            "Backup contém fontes de áudio duplicadas ou inválidas."
        }
        require(snapshot.songs.all { it.durationMs >= 0L && it.playCount >= 0 }) {
            "Backup contém valores de reprodução inválidos."
        }
        require(snapshot.crossRefs.all { it.playlistId in playlistIds && it.songId in songIds }) {
            "Backup contém referências de playlist inválidas."
        }
        require(snapshot.crossRefs.map { it.playlistId to it.songId }.toSet().size == snapshot.crossRefs.size) {
            "Backup contém músicas duplicadas na mesma playlist."
        }
        require(snapshot.lyrics.all { it.songId in songIds }) {
            "Backup contém letras sem música correspondente."
        }
        require(snapshot.lyrics.map { it.songId }.toSet().size == snapshot.lyrics.size) {
            "Backup contém letras duplicadas para a mesma música."
        }
    }

    private inline fun <reified T> enumOrDefault(value: String, default: T): T where T : Enum<T> =
        try { enumValueOf(value) } catch (_: Exception) { default }

    private fun <T> JSONObject.array(key: String, mapper: (JSONObject) -> T): List<T> {
        val array = optJSONArray(key) ?: return emptyList()
        return List(array.length()) { index -> mapper(array.getJSONObject(index)) }
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private fun JSONObject.optNullableLong(key: String): Long? =
        if (isNull(key)) null else optLong(key)
}
