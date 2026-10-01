package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Citizen
import kotlinx.coroutines.flow.Flow

@Dao
interface CitizenDao {
    @Query("SELECT * FROM citizens ORDER BY id DESC")
    fun getAllCitizens(): Flow<List<Citizen>>

    @Query("SELECT * FROM citizens WHERE id = :id LIMIT 1")
    fun getCitizenById(id: Long): Flow<Citizen?>

    @Query("SELECT * FROM citizens WHERE id = :id LIMIT 1")
    suspend fun getCitizenByIdOnce(id: Long): Citizen?

    @Query("SELECT * FROM citizens WHERE fileId = :fileId LIMIT 1")
    suspend fun getCitizenByFileId(fileId: String): Citizen?

    @Query(
        """
        SELECT * FROM citizens 
        WHERE (fullName LIKE '%' || :query || '%' 
           OR mobileNumber LIKE '%' || :query || '%' 
           OR fileId LIKE '%' || :query || '%' 
           OR fatherMotherName LIKE '%' || :query || '%'
           OR village LIKE '%' || :query || '%')
        ORDER BY id DESC
        """
    )
    fun searchCitizens(query: String): Flow<List<Citizen>>

    @Query("SELECT * FROM citizens WHERE status = :status ORDER BY id DESC")
    fun getCitizensByStatus(status: String): Flow<List<Citizen>>

    @Query("SELECT * FROM citizens WHERE wardNo = :wardNo ORDER BY id DESC")
    fun getCitizensByWard(wardNo: String): Flow<List<Citizen>>

    @Query("SELECT * FROM citizens WHERE submissionBatchId = :batchId ORDER BY id ASC")
    fun getCitizensForBatch(batchId: String): Flow<List<Citizen>>

    @Query("SELECT fileId FROM citizens WHERE fileId LIKE :prefix || '%' ORDER BY fileId DESC LIMIT 1")
    suspend fun getMaxFileIdForPrefix(prefix: String): String?

    @Query("SELECT COUNT(*) FROM citizens")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM citizens WHERE createdAt >= :startOfDayMillis")
    fun countToday(startOfDayMillis: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM citizens WHERE status = :status")
    fun countByStatus(status: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCitizen(citizen: Citizen): Long

    @Update
    suspend fun updateCitizen(citizen: Citizen)

    @Delete
    suspend fun deleteCitizen(citizen: Citizen)

    @Query("UPDATE citizens SET submissionBatchId = :batchId, status = :newStatus WHERE id IN (:citizenIds)")
    suspend fun assignBatchToCitizens(citizenIds: List<Long>, batchId: String, newStatus: String)
}
