package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "citizens",
    indices = [
        Index(value = ["fileId"], unique = true),
        Index(value = ["mobileNumber"]),
        Index(value = ["fullName"]),
        Index(value = ["status"])
    ]
)
data class Citizen(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileId: String, // e.g. BR-2026-00001
    val fullName: String, // পূর্ণ নাম
    val fatherMotherName: String, // পিতা/মাতার নাম
    val dateOfBirth: String, // জন্মতারিখ
    val mobileNumber: String, // মোবাইল নম্বর
    val address: String, // ঠিকানা
    val wardNo: String, // ওয়ার্ড নম্বর
    val village: String, // গ্রাম
    val serviceType: String = "জন্ম নিবন্ধন", // কাজের ধরন: জন্ম নিবন্ধন, মৃত্যু নিবন্ধন, নাগরিক সনদ, ইত্যাদি
    val applicationDate: String, // আবেদন করার তারিখ
    val remarks: String = "", // মন্তব্য
    val status: String = STATUS_COLLECTING, // স্ট্যাটাস
    val submissionBatchId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_NEW = "নতুন"
        const val STATUS_COLLECTING = "কাগজ সংগ্রহ চলছে"
        const val STATUS_INCOMPLETE = "অসম্পূর্ণ"
        const val STATUS_COMPLETE = "সম্পূর্ণ"
        const val STATUS_READY_SECRETARY = "সচিবের কাছে জমা দেওয়ার জন্য প্রস্তুত"
        const val STATUS_SENT_UPAZILA = "উপজেলা অফিসে পাঠানো হয়েছে"
        const val STATUS_DONE = "সম্পন্ন"
        const val STATUS_CANCELLED = "বাতিল/স্থগিত"

        val ALL_STATUSES = listOf(
            STATUS_NEW,
            STATUS_COLLECTING,
            STATUS_INCOMPLETE,
            STATUS_COMPLETE,
            STATUS_READY_SECRETARY,
            STATUS_SENT_UPAZILA,
            STATUS_DONE,
            STATUS_CANCELLED
        )

        val SERVICE_TYPES = listOf(
            "জন্ম নিবন্ধন (Birth Registration)",
            "মৃত্যু নিবন্ধন (Death Registration)",
            "নাগরিক সনদ (Citizenship Certificate)",
            "ওয়ারিশ সনদ (Inheritance Certificate)",
            "ট্রেড লাইসেন্স (Trade License)",
            "প্রত্যয়নপত্র (Attestation Certificate)",
            "অন্যান্য সেবা (Other Services)"
        )
    }
}
