package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SubmissionBatch
import kotlinx.coroutines.flow.Flow

@Dao
interface SubmissionDao {
    @Query("SELECT * FROM submission_batches ORDER BY id DESC")
    fun getAllBatches(): Flow<List<SubmissionBatch>>

    @Query("SELECT * FROM submission_batches WHERE id = :id LIMIT 1")
    fun getBatchById(id: Long): Flow<SubmissionBatch?>

    @Query("SELECT * FROM submission_batches WHERE batchId = :batchId LIMIT 1")
    suspend fun getBatchByBatchId(batchId: String): SubmissionBatch?

    @Query("SELECT batchId FROM submission_batches WHERE batchId LIKE :prefix || '%' ORDER BY batchId DESC LIMIT 1")
    suspend fun getMaxBatchIdForPrefix(prefix: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: SubmissionBatch): Long

    @Update
    suspend fun updateBatch(batch: SubmissionBatch)

    @Delete
    suspend fun deleteBatch(batch: SubmissionBatch)
}
