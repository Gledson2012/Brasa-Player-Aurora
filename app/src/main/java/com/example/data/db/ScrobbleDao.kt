package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PendingScrobbleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScrobbleDao {
    @Query("SELECT * FROM pending_scrobbles ORDER BY createdAt ASC")
    fun getAllPendingScrobbles(): Flow<List<PendingScrobbleEntity>>

    @Query("SELECT * FROM pending_scrobbles ORDER BY createdAt ASC")
    suspend fun getAllPendingScrobblesOnce(): List<PendingScrobbleEntity>

    @Query("SELECT * FROM pending_scrobbles WHERE retryCount < :maxRetries ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getScrobblesReadyForRetry(maxRetries: Int = 3, limit: Int = 10): List<PendingScrobbleEntity>

    @Query("SELECT COUNT(*) FROM pending_scrobbles")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingScrobble(scrobble: PendingScrobbleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingScrobbles(scrobbles: List<PendingScrobbleEntity>): List<Long>

    @Update
    suspend fun updatePendingScrobble(scrobble: PendingScrobbleEntity)

    @Query("DELETE FROM pending_scrobbles WHERE id = :id")
    suspend fun deletePendingScrobble(id: Long)

    @Query("DELETE FROM pending_scrobbles WHERE id IN (:ids)")
    suspend fun deletePendingScrobbles(ids: List<Long>)

    @Query("DELETE FROM pending_scrobbles")
    suspend fun clearAllPendingScrobbles()

    @Query("SELECT * FROM pending_scrobbles WHERE songId = :songId AND timestampSeconds = :timestampSeconds LIMIT 1")
    suspend fun findDuplicate(songId: Long, timestampSeconds: Long): PendingScrobbleEntity?

    @Query("SELECT COUNT(*) FROM pending_scrobbles WHERE retryCount < :maxRetries")
    suspend fun getScrobblesReadyForRetryOnce(maxRetries: Int = 3): Int
}
