package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "document_items",
    foreignKeys = [
        ForeignKey(
            entity = Citizen::class,
            parentColumns = ["id"],
            childColumns = ["citizenId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["citizenId"]),
        Index(value = ["documentType"])
    ]
)
data class DocumentItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val citizenId: Long,
    val documentType: String,
    val isRequired: Boolean = true,
    val isUploaded: Boolean = false,
    val notes: String = ""
) {
    companion object {
        const val DOC_PARENT_NID = "পিতা/মাতার NID"
        const val DOC_APPLICANT_NID = "আবেদনকারীর NID"
        const val DOC_PARENT_BIRTH_CERT = "পিতা/মাতার জন্ম নিবন্ধন"
        const val DOC_HOSPITAL_CERT = "হাসপাতালের ছাড়পত্র/জন্ম সংক্রান্ত কাগজ"
        const val DOC_EDUCATION_CERT = "শিক্ষাগত সনদ"
        const val DOC_ADDRESS_PROOF = "ঠিকানার প্রমাণ / হোল্ডিং ট্যাক্স"
        const val DOC_OTHER = "অন্যান্য কাগজ"

        val DEFAULT_REQUIRED_DOCUMENTS = listOf(
            DOC_PARENT_NID,
            DOC_APPLICANT_NID,
            DOC_PARENT_BIRTH_CERT,
            DOC_HOSPITAL_CERT,
            DOC_EDUCATION_CERT,
            DOC_ADDRESS_PROOF,
            DOC_OTHER
        )
    }
}
