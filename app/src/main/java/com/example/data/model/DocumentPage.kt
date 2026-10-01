package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "document_pages",
    foreignKeys = [
        ForeignKey(
            entity = DocumentItem::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documentId"]),
        Index(value = ["citizenId"]),
        Index(value = ["uploadStatus"])
    ]
)
data class DocumentPage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentId: Long,
    val citizenId: Long,
    val pageNumber: Int = 1,
    val localFilePath: String,
    val fileName: String, // e.g. BR-2026-00125_NID_Md-Rahim_2026-10-01.jpg
    val fileType: String = "image/jpeg",
    val fileSize: Long = 0L,
    val driveFileId: String? = null,
    val driveFolderId: String? = null,
    val driveWebViewLink: String? = null,
    val uploadStatus: String = STATUS_PENDING, // pending, uploading, uploaded, failed
    val uploadError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val uploadedAt: Long? = null
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_UPLOADING = "uploading"
        const val STATUS_UPLOADED = "uploaded"
        const val STATUS_FAILED = "failed"
    }
}
