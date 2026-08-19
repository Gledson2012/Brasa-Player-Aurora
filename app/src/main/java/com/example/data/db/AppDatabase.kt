package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CustomPresetEntity
import com.example.data.model.LyricsEntity
import com.example.data.model.PendingScrobbleEntity
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import com.example.data.model.SongFtsEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Song::class,
        Playlist::class,
        PlaylistSongCrossRef::class,
        UserSettingsEntity::class,
        CustomPresetEntity::class,
        LyricsEntity::class,
        SongFtsEntity::class,
        PendingScrobbleEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun scrobbleDao(): ScrobbleDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS lyrics (songId INTEGER NOT NULL, content TEXT NOT NULL, isSynced INTEGER NOT NULL, source TEXT NOT NULL, PRIMARY KEY(songId))"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE playlist_songs_new (
                        playlistId INTEGER NOT NULL,
                        songId INTEGER NOT NULL,
                        orderIndex INTEGER NOT NULL,
                        addedAt INTEGER NOT NULL,
                        PRIMARY KEY(playlistId, songId),
                        FOREIGN KEY(playlistId) REFERENCES playlists(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(songId) REFERENCES songs(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO playlist_songs_new (playlistId, songId, orderIndex, addedAt)
                    SELECT playlistId, songId, orderIndex, addedAt
                    FROM playlist_songs
                    WHERE playlistId IN (SELECT id FROM playlists)
                      AND songId IN (SELECT id FROM songs)
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE playlist_songs")
                db.execSQL("ALTER TABLE playlist_songs_new RENAME TO playlist_songs")
                db.execSQL("CREATE INDEX index_playlist_songs_playlistId ON playlist_songs(playlistId)")
                db.execSQL("CREATE INDEX index_playlist_songs_songId ON playlist_songs(songId)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_settings ADD COLUMN crossfadeSeconds INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Synthetic demo tracks share an empty mediaUri, so the unique
                // identity is kept in a nullable column. Existing imported rows
                // are backfilled from their MediaStore/file URI. If an older
                // version somehow created duplicates, keep the oldest row's key
                // and let the others be reconciled by the next scan.
                db.execSQL("ALTER TABLE songs ADD COLUMN sourceKey TEXT")
                db.execSQL(
                    """
                    UPDATE songs
                    SET sourceKey = CASE
                        WHEN mediaUri IS NOT NULL AND mediaUri <> ''
                             AND id = (
                                 SELECT MIN(existing.id)
                                 FROM songs AS existing
                                 WHERE existing.mediaUri = songs.mediaUri
                             )
                        THEN mediaUri
                        ELSE NULL
                    END
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_songs_sourceKey ON songs(sourceKey)"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE songs ADD COLUMN isAvailable INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE songs ADD COLUMN isMediaStoreItem INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    UPDATE songs
                    SET isMediaStoreItem = CASE
                        WHEN sourceKey LIKE 'content://media/external/audio/media/%' THEN 1
                        ELSE 0
                    END
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create FTS4 virtual table for full-text search.
                db.execSQL(
                    "CREATE VIRTUAL TABLE IF NOT EXISTS `songs_fts` USING fts4(`title`, `artist`, `album`, `genre`, content=`songs`)"
                )

                // Populate FTS with existing songs
                db.execSQL(
                    "INSERT INTO `songs_fts`(`rowid`, `title`, `artist`, `album`, `genre`) SELECT `rowid`, `title`, `artist`, `album`, `genre` FROM `songs`"
                )

                // Room requires these content sync triggers for FTS4 content tables.
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_songs_fts_BEFORE_UPDATE BEFORE UPDATE ON `songs` BEGIN DELETE FROM `songs_fts` WHERE `docid`=OLD.`rowid`; END"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_songs_fts_BEFORE_DELETE BEFORE DELETE ON `songs` BEGIN DELETE FROM `songs_fts` WHERE `docid`=OLD.`rowid`; END"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_songs_fts_AFTER_UPDATE AFTER UPDATE ON `songs` BEGIN INSERT INTO `songs_fts`(`docid`, `title`, `artist`, `album`, `genre`) VALUES (NEW.`rowid`, NEW.`title`, NEW.`artist`, NEW.`album`, NEW.`genre`); END"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_songs_fts_AFTER_INSERT AFTER INSERT ON `songs` BEGIN INSERT INTO `songs_fts`(`docid`, `title`, `artist`, `album`, `genre`) VALUES (NEW.`rowid`, NEW.`title`, NEW.`artist`, NEW.`album`, NEW.`genre`); END"
                )

                // Create pending scrobbles table for offline queue
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pending_scrobbles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `songId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `artist` TEXT NOT NULL,
                        `album` TEXT NOT NULL,
                        `durationSeconds` INTEGER NOT NULL,
                        `timestampSeconds` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `retryCount` INTEGER NOT NULL,
                        `lastError` TEXT
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_pending_scrobbles_songId_timestampSeconds` ON `pending_scrobbles` (`songId`, `timestampSeconds`)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return getDatabase(context, CoroutineScope(Dispatchers.IO))
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_player_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                    .addMigrations(MIGRATION_6_7)
                    .addMigrations(MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.songDao(), database.playlistDao(), database.userSettingsDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(songDao: SongDao, playlistDao: PlaylistDao, userSettingsDao: UserSettingsDao) {
            userSettingsDao.saveUserSettings(UserSettingsEntity())
            val initialSongs = listOf(
                Song(
                    title = "Neon Horizon",
                    artist = "Cyber Wave",
                    album = "Outrun 1984",
                    durationMs = 204000L, // 3:24
                    coverDrawableName = "cover_synthwave",
                    genre = "Synthwave",
                    isFavorite = true,
                    synthPreset = "synthwave"
                ),
                Song(
                    title = "Midnight Study Rain",
                    artist = "LoFi Lounge",
                    album = "Chillhop Dreams",
                    durationMs = 168000L, // 2:48
                    coverDrawableName = "cover_lofi",
                    genre = "Lo-Fi Chill",
                    isFavorite = true,
                    synthPreset = "lofi"
                ),
                Song(
                    title = "Acoustic Sunsets",
                    artist = "Forest Horizon",
                    album = "Golden Hour Sessions",
                    durationMs = 195000L, // 3:15
                    coverDrawableName = "cover_acoustic",
                    genre = "Acoustic Folk",
                    isFavorite = false,
                    synthPreset = "acoustic"
                ),
                Song(
                    title = "Cyber City Matrix",
                    artist = "Pulse Project",
                    album = "Holographic Beats",
                    durationMs = 232000L, // 3:52
                    coverDrawableName = "cover_electronic",
                    genre = "EDM & Electro",
                    isFavorite = false,
                    synthPreset = "electronic"
                ),
                Song(
                    title = "Velvet Saxophone",
                    artist = "Nova Quartet",
                    album = "Midnight Blue",
                    durationMs = 185000L, // 3:05
                    coverDrawableName = "cover_lofi",
                    genre = "Smooth Jazz",
                    isFavorite = false,
                    synthPreset = "jazz"
                ),
                Song(
                    title = "Deep Space Echoes",
                    artist = "Astral Voyager",
                    album = "Interstellar Drift",
                    durationMs = 250000L, // 4:10
                    coverDrawableName = "cover_synthwave",
                    genre = "Ambient",
                    isFavorite = false,
                    synthPreset = "ambient"
                ),
                Song(
                    title = "Sunset Highway",
                    artist = "Retro Riders",
                    album = "Night Cruise",
                    durationMs = 210000L, // 3:30
                    coverDrawableName = "cover_synthwave",
                    genre = "Synthwave",
                    isFavorite = true,
                    synthPreset = "synthwave"
                ),
                Song(
                    title = "Morning Coffee & Raindrops",
                    artist = "Chillhop Cafe",
                    album = "Warm Mornings",
                    durationMs = 160000L, // 2:40
                    coverDrawableName = "cover_lofi",
                    genre = "Lo-Fi Beats",
                    isFavorite = false,
                    synthPreset = "lofi"
                )
            )

            val insertedIds = songDao.insertSongs(initialSongs)

            val playlists = listOf(
                Playlist(
                    name = "Favoritas do Momento",
                    description = "Músicas relaxantes e synthwave marcadas como favoritas",
                    gradientIndex = 0,
                    iconName = "favorite"
                ),
                Playlist(
                    name = "Cyberpunk & Synthwave",
                    description = "Batidas energéticas e sintéticas para focar",
                    gradientIndex = 1,
                    iconName = "electric_bolt"
                ),
                Playlist(
                    name = "Foco & Relaxar",
                    description = "Sons calmos de piano, chuva e acordes acústicos",
                    gradientIndex = 2,
                    iconName = "spa"
                )
            )

            val pIds = playlistDao.insertPlaylists(playlists)

            if (pIds.isNotEmpty() && insertedIds.isNotEmpty()) {
                // Add initial songs to playlists
                val p1 = pIds[0]
                val p2 = if (pIds.size > 1) pIds[1] else p1
                val p3 = if (pIds.size > 2) pIds[2] else p1

                playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p1, insertedIds[0], 0))
                playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p1, insertedIds[1], 1))
                if (insertedIds.size > 6) {
                    playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p1, insertedIds[6], 2))
                }

                playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p2, insertedIds[0], 0))
                if (insertedIds.size > 3) {
                    playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p2, insertedIds[3], 1))
                }
                if (insertedIds.size > 6) {
                    playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p2, insertedIds[6], 2))
                }

                if (insertedIds.size > 1) {
                    playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p3, insertedIds[1], 0))
                }
                if (insertedIds.size > 2) {
                    playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p3, insertedIds[2], 1))
                }
                if (insertedIds.size > 4) {
                    playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p3, insertedIds[4], 2))
                }
                if (insertedIds.size > 7) {
                    playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(p3, insertedIds[7], 3))
                }
            }
        }
    }
}
