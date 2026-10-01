package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentPage
import com.example.data.model.DocumentWithPages
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM document_items WHERE citizenId = :citizenId ORDER BY id ASC")
    fun getDocumentsForCitizen(citizenId: Long): Flow<List<DocumentItem>>

    @Query("SELECT * FROM document_items WHERE citizenId = :citizenId ORDER BY id ASC")
    suspend fun getDocumentsForCitizenList(citizenId: Long): List<DocumentItem>

    @Query("SELECT * FROM document_items WHERE id = :documentId LIMIT 1")
    suspend fun getDocumentById(documentId: Long): DocumentItem?

    @Transaction
    @Query("SELECT * FROM document_items WHERE citizenId = :citizenId ORDER BY id ASC")
    fun getDocumentsWithPagesForCitizen(citizenId: Long): Flow<List<DocumentWithPages>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentItem>): List<Long>

    @Update
    suspend fun updateDocument(document: DocumentItem)

    @Delete
    suspend fun deleteDocument(document: DocumentItem)

    // Document Pages
    @Query("SELECT * FROM document_pages WHERE documentId = :documentId ORDER BY pageNumber ASC")
    fun getPagesForDocument(documentId: Long): Flow<List<DocumentPage>>

    @Query("SELECT * FROM document_pages WHERE citizenId = :citizenId ORDER BY documentId, pageNumber ASC")
    fun getAllPagesForCitizen(citizenId: Long): Flow<List<DocumentPage>>

    @Query("SELECT * FROM document_pages WHERE id = :id LIMIT 1")
    suspend fun getPageById(id: Long): DocumentPage?

    @Query("SELECT * FROM document_pages WHERE uploadStatus != 'uploaded' ORDER BY id ASC")
    fun getPendingUploadPages(): Flow<List<DocumentPage>>

    @Query("SELECT * FROM document_pages WHERE uploadStatus != 'uploaded' ORDER BY id ASC")
    suspend fun getPendingUploadPagesList(): List<DocumentPage>

    @Query("SELECT COUNT(*) FROM document_pages WHERE uploadStatus != 'uploaded'")
    fun countPendingUploads(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: DocumentPage): Long

    @Update
    suspend fun updatePage(page: DocumentPage)

    @Delete
    suspend fun deletePage(page: DocumentPage)

    @Query("SELECT COUNT(*) FROM document_pages WHERE documentId = :documentId")
    suspend fun countPagesForDocument(documentId: Long): Int
}
