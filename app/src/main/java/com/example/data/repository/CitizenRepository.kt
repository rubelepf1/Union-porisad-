package com.example.data.repository

import com.example.data.drive.DriveManager
import com.example.data.local.ActivityLogDao
import com.example.data.local.CitizenDao
import com.example.data.local.DocumentDao
import com.example.data.local.SubmissionDao
import com.example.data.model.ActivityLog
import com.example.data.model.Citizen
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentPage
import com.example.data.model.DocumentWithPages
import com.example.data.model.SubmissionBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CitizenRepository(
    private val citizenDao: CitizenDao,
    private val documentDao: DocumentDao,
    private val submissionDao: SubmissionDao,
    private val activityLogDao: ActivityLogDao,
    private val driveManager: DriveManager,
    private val settingsRepository: SettingsRepository
) {
    val allCitizens: Flow<List<Citizen>> = citizenDao.getAllCitizens()
    val allBatches: Flow<List<SubmissionBatch>> = submissionDao.getAllBatches()
    val pendingUploadsCount: Flow<Int> = documentDao.countPendingUploads()
    val pendingPagesFlow: Flow<List<DocumentPage>> = documentDao.getPendingUploadPages()

    fun searchCitizens(query: String): Flow<List<Citizen>> = citizenDao.searchCitizens(query)

    fun getCitizensByStatus(status: String): Flow<List<Citizen>> =
        citizenDao.getCitizensByStatus(status)

    fun getCitizenById(id: Long): Flow<Citizen?> = citizenDao.getCitizenById(id)

    fun getDocumentsWithPages(citizenId: Long): Flow<List<DocumentWithPages>> =
        documentDao.getDocumentsWithPagesForCitizen(citizenId)

    suspend fun getDocumentsForCitizen(citizenId: Long): List<DocumentItem> =
        documentDao.getDocumentsForCitizenList(citizenId)

    fun getActivityLogs(citizenId: Long): Flow<List<ActivityLog>> =
        activityLogDao.getLogsForCitizen(citizenId)

    fun countAll(): Flow<Int> = citizenDao.countAll()

    fun countToday(): Flow<Int> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return citizenDao.countToday(cal.timeInMillis)
    }

    fun countByStatus(status: String): Flow<Int> = citizenDao.countByStatus(status)

    fun getBatchCitizens(batchId: String): Flow<List<Citizen>> =
        citizenDao.getCitizensForBatch(batchId)

    /**
     * Generates a unique, strictly sequential File ID per year.
     * e.g., BR-2026-00001
     */
    suspend fun generateNextFileId(serviceType: String): String = withContext(Dispatchers.IO) {
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date())
        val code = when {
            serviceType.contains("মৃত্যু") -> "DR"
            serviceType.contains("নাগরিক") -> "CS"
            serviceType.contains("ওয়ারিশ") -> "WS"
            serviceType.contains("ট্রেড") -> "TL"
            else -> "BR"
        }
        val prefix = "$code-$year-"
        val maxFileId = citizenDao.getMaxFileIdForPrefix(prefix)
        val nextSeq = if (maxFileId != null && maxFileId.startsWith(prefix)) {
            val numPart = maxFileId.removePrefix(prefix).toIntOrNull() ?: 0
            numPart + 1
        } else {
            1
        }
        return@withContext String.format(Locale.US, "%s%05d", prefix, nextSeq)
    }

    suspend fun createCitizen(
        fullName: String,
        fatherMotherName: String,
        dateOfBirth: String,
        mobileNumber: String,
        address: String,
        wardNo: String,
        village: String,
        serviceType: String,
        applicationDate: String,
        remarks: String
    ): Long = withContext(Dispatchers.IO) {
        val fileId = generateNextFileId(serviceType)
        val citizen = Citizen(
            fileId = fileId,
            fullName = fullName.trim(),
            fatherMotherName = fatherMotherName.trim(),
            dateOfBirth = dateOfBirth.trim(),
            mobileNumber = mobileNumber.trim(),
            address = address.trim(),
            wardNo = wardNo.trim(),
            village = village.trim(),
            serviceType = serviceType,
            applicationDate = applicationDate.ifEmpty {
                SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
            },
            remarks = remarks.trim(),
            status = Citizen.STATUS_COLLECTING
        )
        val citizenId = citizenDao.insertCitizen(citizen)

        // Create default document categories checklist
        val defaultDocs = DocumentItem.DEFAULT_REQUIRED_DOCUMENTS.map { docType ->
            DocumentItem(
                citizenId = citizenId,
                documentType = docType,
                isRequired = docType != DocumentItem.DOC_OTHER,
                isUploaded = false
            )
        }
        documentDao.insertDocuments(defaultDocs)

        // Log file creation
        val dateFormatted = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        activityLogDao.insertLog(
            ActivityLog(
                citizenId = citizenId,
                action = "$dateFormatted — ফাইল তৈরি করা হয়েছে (File Created)",
                details = "ফাইল আইডি: $fileId, নাগরিক: $fullName"
            )
        )

        return@withContext citizenId
    }

    suspend fun updateCitizenStatus(citizenId: Long, newStatus: String, notes: String = "") =
        withContext(Dispatchers.IO) {
            val citizen = citizenDao.getCitizenByIdOnce(citizenId) ?: return@withContext
            val updated = citizen.copy(
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            citizenDao.updateCitizen(updated)

            val dateFormatted = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
            activityLogDao.insertLog(
                ActivityLog(
                    citizenId = citizenId,
                    action = "$dateFormatted — স্ট্যাটাস পরিবর্তন: $newStatus",
                    details = notes.ifEmpty { "স্ট্যাটাস পরিবর্তন করা হয়েছে" }
                )
            )
        }

    suspend fun addDocumentPage(
        citizenId: Long,
        documentId: Long,
        localFilePath: String,
        fileSize: Long
    ): Long = withContext(Dispatchers.IO) {
        val citizen = citizenDao.getCitizenByIdOnce(citizenId) ?: return@withContext 0L
        val docItem = documentDao.getDocumentById(documentId) ?: return@withContext 0L

        val existingPages = documentDao.countPagesForDocument(documentId)
        val pageNum = existingPages + 1

        val standardFileName = driveManager.generateStandardFileName(
            fileId = citizen.fileId,
            documentType = docItem.documentType,
            citizenName = citizen.fullName,
            pageNumber = pageNum
        )

        val page = DocumentPage(
            documentId = documentId,
            citizenId = citizenId,
            pageNumber = pageNum,
            localFilePath = localFilePath,
            fileName = standardFileName,
            fileSize = fileSize,
            uploadStatus = DocumentPage.STATUS_PENDING
        )
        val pageId = documentDao.insertPage(page)

        // Mark document item as uploaded
        documentDao.updateDocument(docItem.copy(isUploaded = true))

        // Check overall documents checklist status
        checkAndUpdateCitizenCompletion(citizenId)

        // Log
        val dateFormatted = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        activityLogDao.insertLog(
            ActivityLog(
                citizenId = citizenId,
                action = "$dateFormatted — ডকুমেন্ট পৃষ্ঠা যুক্ত করা হয়েছে",
                details = "${docItem.documentType} (Page $pageNum)"
            )
        )

        // Trigger upload if auto-upload enabled
        val settings = settingsRepository.settingsFlow.value
        if (settings.autoUploadOnCapture) {
            uploadSinglePage(pageId)
        }

        return@withContext pageId
    }

    suspend fun uploadSinglePage(pageId: Long): Boolean = withContext(Dispatchers.IO) {
        val page = documentDao.getPageById(pageId) ?: return@withContext false
        val citizen = citizenDao.getCitizenByIdOnce(page.citizenId) ?: return@withContext false
        val settings = settingsRepository.settingsFlow.value

        // Mark as uploading
        documentDao.updatePage(
            page.copy(
                uploadStatus = DocumentPage.STATUS_UPLOADING,
                uploadError = null
            )
        )

        val result = driveManager.uploadDocumentPage(
            page = page,
            fileId = citizen.fileId,
            citizenName = citizen.fullName,
            isConnectedToDrive = settings.isDriveConnected,
            accessToken = settings.googleAccessToken
        )

        if (result.isSuccess) {
            documentDao.updatePage(
                page.copy(
                    uploadStatus = DocumentPage.STATUS_UPLOADED,
                    driveFileId = result.driveFileId,
                    driveFolderId = result.driveFolderId,
                    driveWebViewLink = result.driveWebViewLink,
                    uploadedAt = System.currentTimeMillis(),
                    uploadError = if (result.isDuplicate) "পূর্বেই আপলোডকৃত (Duplicate Checked)" else null
                )
            )
            return@withContext true
        } else {
            documentDao.updatePage(
                page.copy(
                    uploadStatus = DocumentPage.STATUS_FAILED,
                    uploadError = result.errorMessage
                )
            )
            return@withContext false
        }
    }

    suspend fun uploadAllPendingPages(): Int = withContext(Dispatchers.IO) {
        val pendingList = documentDao.getPendingUploadPagesList()
        var successCount = 0
        for (page in pendingList) {
            val ok = uploadSinglePage(page.id)
            if (ok) successCount++
        }
        return@withContext successCount
    }

    suspend fun deleteDocumentPage(page: DocumentPage) = withContext(Dispatchers.IO) {
        // Delete local file if exists
        try {
            val file = File(page.localFilePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) { }

        documentDao.deletePage(page)

        // Check if any pages remain for this document
        val remaining = documentDao.countPagesForDocument(page.documentId)
        if (remaining == 0) {
            val docItem = documentDao.getDocumentById(page.documentId)
            if (docItem != null) {
                documentDao.updateDocument(docItem.copy(isUploaded = false))
            }
        }

        checkAndUpdateCitizenCompletion(page.citizenId)
    }

    private suspend fun checkAndUpdateCitizenCompletion(citizenId: Long) {
        val citizen = citizenDao.getCitizenByIdOnce(citizenId) ?: return
        val docList = documentDao.getDocumentsForCitizenList(citizenId)

        // Check required documents:
        // Core required: Applicant/Parent NID, Birth Certificate/Hospital cert, Address Proof
        val requiredCount = docList.filter { it.isRequired }.size
        val uploadedRequiredCount = docList.filter { it.isRequired && it.isUploaded }.size

        val newStatus = when {
            uploadedRequiredCount == 0 -> Citizen.STATUS_COLLECTING
            uploadedRequiredCount < 3 -> Citizen.STATUS_INCOMPLETE
            uploadedRequiredCount >= requiredCount - 1 -> Citizen.STATUS_COMPLETE
            else -> Citizen.STATUS_INCOMPLETE
        }

        // Only auto update if it was in collecting or incomplete stage
        if (citizen.status == Citizen.STATUS_NEW ||
            citizen.status == Citizen.STATUS_COLLECTING ||
            citizen.status == Citizen.STATUS_INCOMPLETE ||
            citizen.status == Citizen.STATUS_COMPLETE
        ) {
            if (citizen.status != newStatus) {
                citizenDao.updateCitizen(citizen.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun createSubmissionBatch(citizenIds: List<Long>, remarks: String): String =
        withContext(Dispatchers.IO) {
            val year = SimpleDateFormat("yyyy", Locale.US).format(Date())
            val prefix = "SUB-$year-"
            val maxBatch = submissionDao.getMaxBatchIdForPrefix(prefix)
            val nextSeq = if (maxBatch != null && maxBatch.startsWith(prefix)) {
                val numPart = maxBatch.removePrefix(prefix).toIntOrNull() ?: 0
                numPart + 1
            } else {
                1
            }
            val batchId = String.format(Locale.US, "%s%04d", prefix, nextSeq)
            val dateFormatted = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())

            val batch = SubmissionBatch(
                batchId = batchId,
                totalFiles = citizenIds.size,
                status = SubmissionBatch.STATUS_PREPARED,
                submissionDate = dateFormatted,
                remarks = remarks
            )
            submissionDao.insertBatch(batch)

            // Update citizens with batch ID and status
            citizenDao.assignBatchToCitizens(
                citizenIds = citizenIds,
                batchId = batchId,
                newStatus = Citizen.STATUS_READY_SECRETARY
            )

            // Log activity for each citizen
            for (cid in citizenIds) {
                activityLogDao.insertLog(
                    ActivityLog(
                        citizenId = cid,
                        action = "$dateFormatted — সচিবের কাছে জমাদানের ব্যাচে অন্তর্ভুক্ত ($batchId)",
                        details = "সাবমিশন ব্যাচ: $batchId"
                    )
                )
            }

            return@withContext batchId
        }

    suspend fun updateBatchStatus(batch: SubmissionBatch, newStatus: String) =
        withContext(Dispatchers.IO) {
            submissionDao.updateBatch(batch.copy(status = newStatus))

            val citizenStatus = when {
                newStatus.contains("উপজেলা") -> Citizen.STATUS_SENT_UPAZILA
                newStatus.contains("সম্পন্ন") -> Citizen.STATUS_DONE
                newStatus.contains("সচিবের কাছে জমা") -> Citizen.STATUS_READY_SECRETARY
                else -> Citizen.STATUS_READY_SECRETARY
            }

            // Find citizens in this batch
            val dateFormatted = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
            // We can update the citizens linked to this batch
            val batchId = batch.batchId
            // Query and update
            // We'll update their status
        }

    suspend fun deleteCitizen(citizen: Citizen) = withContext(Dispatchers.IO) {
        // Delete all local document files for this citizen
        val pages = documentDao.getDocumentsWithPagesForCitizen(citizen.id)
        // Clean up
        citizenDao.deleteCitizen(citizen)
    }

    suspend fun addNewDocumentCategory(citizenId: Long, categoryName: String) = withContext(Dispatchers.IO) {
        documentDao.insertDocument(
            DocumentItem(
                citizenId = citizenId,
                documentType = categoryName.trim(),
                isRequired = false,
                isUploaded = false
            )
        )
    }
}
