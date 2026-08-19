package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CustomPresetEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    // --- Create / Insert ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettingsOnceForBackup(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPreset(preset: CustomPresetEntity): Long

    @Query("SELECT * FROM custom_equalizer_presets ORDER BY id ASC")
    suspend fun getAllCustomPresetsOnce(): List<CustomPresetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreCustomPresets(presets: List<CustomPresetEntity>)

    // --- Read ---
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettingsOnce(): UserSettingsEntity?

    @Query("SELECT * FROM custom_equalizer_presets ORDER BY id DESC")
    fun getAllCustomPresets(): Flow<List<CustomPresetEntity>>

    @Query("SELECT * FROM custom_equalizer_presets WHERE id = :id")
    fun getCustomPresetById(id: Long): Flow<CustomPresetEntity?>

    // --- Update ---
    @Query("UPDATE user_settings SET themeName = :theme, visualizerStyle = :visualizer, albumArtStyle = :albumArt, dynamicColors = :dynamicColors WHERE id = 1")
    suspend fun updateThemeSettings(theme: String, visualizer: String, albumArt: String, dynamicColors: Boolean)

    @Query("UPDATE user_settings SET equalizerEnabled = :enabled, currentPresetId = :presetId, band0 = :b0, band1 = :b1, band2 = :b2, band3 = :b3, band4 = :b4, bassBoost = :bass, virtualizer = :virt, balance = :bal WHERE id = 1")
    suspend fun updateEqualizerSettings(enabled: Boolean, presetId: String, b0: Int, b1: Int, b2: Int, b3: Int, b4: Int, bass: Int, virt: Int, bal: Float)

    @Query("UPDATE user_settings SET playbackSpeed = :speed, crossfadeSeconds = :crossfade, repeatMode = :repeat, isShuffle = :shuffle WHERE id = 1")
    suspend fun updatePlaybackPreferences(speed: Float, crossfade: Int, repeat: String, shuffle: Boolean)

    @Query("UPDATE user_settings SET lastPlayedSongId = :songId, lastPlaybackPositionMs = :positionMs WHERE id = 1")
    suspend fun updateLastPlayedState(songId: Long?, positionMs: Long)

    // --- Delete ---
    @Delete
    suspend fun deleteCustomPreset(preset: CustomPresetEntity)

    @Query("DELETE FROM custom_equalizer_presets WHERE id = :id")
    suspend fun deleteCustomPresetById(id: Long)

    @Query("DELETE FROM custom_equalizer_presets")
    suspend fun clearAllCustomPresets()

    @Query("DELETE FROM user_settings")
    suspend fun clearUserSettings()
}
