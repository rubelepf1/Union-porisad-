package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "submission_batches",
    indices = [
        Index(value = ["batchId"], unique = true)
    ]
)
data class SubmissionBatch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: String, // e.g. SUB-2026-0015
    val createdAt: Long = System.currentTimeMillis(),
    val totalFiles: Int = 0,
    val status: String = STATUS_PREPARED,
    val submissionDate: String,
    val remarks: String = ""
) {
    companion object {
        const val STATUS_PREPARED = "Prepared (প্রস্তুত)"
        const val STATUS_SUBMITTED = "Submitted to Secretary (সচিবের কাছে জমা)"
        const val STATUS_SENT_UPAZILA = "Sent to Upazila (উপজেলায় পাঠানো)"
        const val STATUS_COMPLETED = "Completed (সম্পন্ন)"

        val ALL_BATCH_STATUSES = listOf(
            STATUS_PREPARED,
            STATUS_SUBMITTED,
            STATUS_SENT_UPAZILA,
            STATUS_COMPLETED
        )
    }
}
