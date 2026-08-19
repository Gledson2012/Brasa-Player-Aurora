package com.example.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class Migration7to8Test {

    private lateinit var db: SupportSQLiteDatabase
    private val migration: Migration = AppDatabase.MIGRATION_7_8

    @Before
    fun setup() {
        db = mockk(relaxed = true)
    }

    @Test
    fun `migration creates FTS4 virtual table`() {
        migration.migrate(db)

        verify {
            db.execSQL(
                "CREATE VIRTUAL TABLE IF NOT EXISTS `songs_fts` USING fts4(`title`, `artist`, `album`, `genre`, content=`songs`)"
            )
        }
    }

    @Test
    fun `migration populates FTS with existing songs`() {
        migration.migrate(db)

        verify {
            db.execSQL(
                "INSERT INTO `songs_fts`(`rowid`, `title`, `artist`, `album`, `genre`) SELECT `rowid`, `title`, `artist`, `album`, `genre` FROM `songs`"
            )
        }
    }

    @Test
    fun `migration creates BEFORE_UPDATE trigger`() {
        migration.migrate(db)

        verify {
            db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_songs_fts_BEFORE_UPDATE BEFORE UPDATE ON `songs` BEGIN DELETE FROM `songs_fts` WHERE `docid`=OLD.`rowid`; END"
            )
        }
    }

    @Test
    fun `migration creates BEFORE_DELETE trigger`() {
        migration.migrate(db)

        verify {
            db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_songs_fts_BEFORE_DELETE BEFORE DELETE ON `songs` BEGIN DELETE FROM `songs_fts` WHERE `docid`=OLD.`rowid`; END"
            )
        }
    }

    @Test
    fun `migration creates AFTER_UPDATE trigger`() {
        migration.migrate(db)

        verify {
            db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_songs_fts_AFTER_UPDATE AFTER UPDATE ON `songs` BEGIN INSERT INTO `songs_fts`(`docid`, `title`, `artist`, `album`, `genre`) VALUES (NEW.`rowid`, NEW.`title`, NEW.`artist`, NEW.`album`, NEW.`genre`); END"
            )
        }
    }

    @Test
    fun `migration creates AFTER_INSERT trigger`() {
        migration.migrate(db)

        verify {
            db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_songs_fts_AFTER_INSERT AFTER INSERT ON `songs` BEGIN INSERT INTO `songs_fts`(`docid`, `title`, `artist`, `album`, `genre`) VALUES (NEW.`rowid`, NEW.`title`, NEW.`artist`, NEW.`album`, NEW.`genre`); END"
            )
        }
    }

    @Test
    fun `migration creates pending_scrobbles table`() {
        migration.migrate(db)

        verify {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `pending_scrobbles` (\n" +
                "    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "    `songId` INTEGER NOT NULL,\n" +
                "    `title` TEXT NOT NULL,\n" +
                "    `artist` TEXT NOT NULL,\n" +
                "    `album` TEXT NOT NULL,\n" +
                "    `durationSeconds` INTEGER NOT NULL,\n" +
                "    `timestampSeconds` INTEGER NOT NULL,\n" +
                "    `createdAt` INTEGER NOT NULL,\n" +
                "    `retryCount` INTEGER NOT NULL,\n" +
                "    `lastError` TEXT\n" +
                ")"
            )
        }
    }

    @Test
    fun `migration creates unique index on pending_scrobbles`() {
        migration.migrate(db)

        verify {
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_pending_scrobbles_songId_timestampSeconds` ON `pending_scrobbles` (`songId`, `timestampSeconds`)"
            )
        }
    }
}
