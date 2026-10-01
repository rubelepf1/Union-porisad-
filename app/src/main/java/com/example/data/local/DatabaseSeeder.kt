package com.example.data.local

import android.content.Context
import com.example.data.model.ActivityLog
import com.example.data.model.Citizen
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentPage
import com.example.data.model.SubmissionBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DatabaseSeeder {

    suspend fun seedSampleDataIfEmpty(database: AppDatabase, context: Context) = withContext(Dispatchers.IO) {
        val count = database.citizenDao().getMaxFileIdForPrefix("BR-2026-")
        if (count != null) {
            // Already has data
            return@withContext
        }

        val todayDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())

        // Citizen 1: Completed & Ready for Secretary
        val c1 = Citizen(
            fileId = "BR-2026-00001",
            fullName = "মোঃ রহিম উল্লাহ",
            fatherMotherName = "পিতা: মোঃ করিম উল্লাহ, মাতা: রহিমা বেগম",
            dateOfBirth = "১২-০৪-২০২৪",
            mobileNumber = "01819234567",
            address = "হোল্ডিং নং: ১২৪, দক্ষিণ পাড়া",
            wardNo = "০৫",
            village = "রামপুর",
            serviceType = "জন্ম নিবন্ধন (Birth Registration)",
            applicationDate = todayDate,
            remarks = "জরুরি ভিত্তিতে পাসপোর্টের জন্য প্রয়োজন",
            status = Citizen.STATUS_READY_SECRETARY
        )
        val c1Id = database.citizenDao().insertCitizen(c1)

        // Citizen 2: Incomplete
        val c2 = Citizen(
            fileId = "BR-2026-00002",
            fullName = "আয়েশা সিদ্দিকা",
            fatherMotherName = "পিতা: সামসুল হক, মাতা: খাদিজা আক্তার",
            dateOfBirth = "০৫-০৮-২০২৫",
            mobileNumber = "01711987654",
            address = "মোল্লা বাড়ি",
            wardNo = "০৩",
            village = "চর রামপুর",
            serviceType = "জন্ম নিবন্ধন (Birth Registration)",
            applicationDate = todayDate,
            remarks = "টিকা কার্ড ও পিতার NID জমা হয়েছে, মাতার NID বাকি",
            status = Citizen.STATUS_INCOMPLETE
        )
        val c2Id = database.citizenDao().insertCitizen(c2)

        // Citizen 3: Paper Collecting
        val c3 = Citizen(
            fileId = "BR-2026-00003",
            fullName = "তানভীর আহমেদ তানিম",
            fatherMotherName = "পিতা: দেলোয়ার হোসেন, মাতা: নারগিস বেগম",
            dateOfBirth = "০১-০১-২০২৬",
            mobileNumber = "01912334455",
            address = "পূর্ব বাজার সংলগ্ন",
            wardNo = "০২",
            village = "শান্তিপুর",
            serviceType = "জন্ম নিবন্ধন (Birth Registration)",
            applicationDate = todayDate,
            remarks = "হাসপাতালের ডিসচার্জ সার্টিফিকেট সংগ্রহাধীন",
            status = Citizen.STATUS_COLLECTING
        )
        val c3Id = database.citizenDao().insertCitizen(c3)

        // Citizen 4: Sent to Upazila
        val c4 = Citizen(
            fileId = "BR-2026-00004",
            fullName = "ফাতিমা আক্তার",
            fatherMotherName = "পিতা: জয়নাল আবেদীন, মাতা: সাহিদা বেগম",
            dateOfBirth = "২০-০৬-২০২৩",
            mobileNumber = "01622334455",
            address = "মাস্টার পাড়া",
            wardNo = "০৭",
            village = "উত্তর রামপুর",
            serviceType = "জন্ম নিবন্ধন (Birth Registration)",
            applicationDate = "28-09-2026",
            remarks = "উপজেলা নির্বাহী অফিসার কার্যালয়ে অনুমোদনের জন্য পাঠানো হয়েছে",
            status = Citizen.STATUS_SENT_UPAZILA,
            submissionBatchId = "SUB-2026-0001"
        )
        val c4Id = database.citizenDao().insertCitizen(c4)

        // Insert documents for c1
        insertDefaultDocsForCitizen(database, c1Id, allUploaded = true, context = context, fileId = "BR-2026-00001", citizenName = "Md-Rahim")
        insertDefaultDocsForCitizen(database, c2Id, allUploaded = false, context = context, fileId = "BR-2026-00002", citizenName = "Ayesha-Siddika")
        insertDefaultDocsForCitizen(database, c3Id, allUploaded = false, context = context, fileId = "BR-2026-00003", citizenName = "Tanvir-Ahmed")
        insertDefaultDocsForCitizen(database, c4Id, allUploaded = true, context = context, fileId = "BR-2026-00004", citizenName = "Fatima-Akter")

        // Seed Submission Batch
        val batch = SubmissionBatch(
            batchId = "SUB-2026-0001",
            totalFiles = 1,
            status = SubmissionBatch.STATUS_SENT_UPAZILA,
            submissionDate = todayDate,
            remarks = "সেপ্টেম্বর মাসের ৪র্থ সপ্তাহের জন্ম নিবন্ধন আবেদনসমূহ"
        )
        database.submissionDao().insertBatch(batch)
    }

    private suspend fun insertDefaultDocsForCitizen(
        database: AppDatabase,
        citizenId: Long,
        allUploaded: Boolean,
        context: Context,
        fileId: String,
        citizenName: String
    ) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val defaultDocs = DocumentItem.DEFAULT_REQUIRED_DOCUMENTS
        for ((index, docType) in defaultDocs.withIndex()) {
            val isUploaded = when {
                allUploaded -> true
                index < 2 -> true
                else -> false
            }
            val docItem = DocumentItem(
                citizenId = citizenId,
                documentType = docType,
                isRequired = docType != DocumentItem.DOC_OTHER,
                isUploaded = isUploaded
            )
            val docId = database.documentDao().insertDocument(docItem)

            if (isUploaded) {
                // Create a simulated local file in app cache
                val sampleFile = File(context.cacheDir, "${fileId}_doc_${docId}.txt")
                if (!sampleFile.exists()) {
                    FileOutputStream(sampleFile).use { it.write("UP Citizen Document Proof - $docType".toByteArray()) }
                }

                val standardName = "${fileId}_${docType.replace(" ", "_")}_${citizenName}_${todayDate}.jpg"
                val page = DocumentPage(
                    documentId = docId,
                    citizenId = citizenId,
                    pageNumber = 1,
                    localFilePath = sampleFile.absolutePath,
                    fileName = standardName,
                    fileSize = 420_000L + (index * 85_000L),
                    driveFileId = null,
                    driveFolderId = null,
                    driveWebViewLink = null,
                    uploadStatus = DocumentPage.STATUS_PENDING,
                    uploadedAt = null
                )
                database.documentDao().insertPage(page)
            }
        }

        database.activityLogDao().insertLog(
            ActivityLog(
                citizenId = citizenId,
                action = "$todayDate — ফাইল রেজিস্ট্রি করা হয়েছে",
                details = "ইউপি ফাইল আইডি: $fileId"
            )
        )
    }
}
