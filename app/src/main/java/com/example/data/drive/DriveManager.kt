package com.example.data.drive

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.model.DocumentPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DriveManager(private val context: Context) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    data class DriveUploadResult(
        val isSuccess: Boolean,
        val driveFileId: String? = null,
        val driveFolderId: String? = null,
        val driveWebViewLink: String? = null,
        val errorMessage: String? = null,
        val isDuplicate: Boolean = false
    )

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Generates standard file name:
     * Format: FileID_DocumentType_CitizenName_Date.jpg
     * Example: BR-2026-00125_Parent-NID_Md-Rahim_2026-10-01.jpg
     */
    fun generateStandardFileName(
        fileId: String,
        documentType: String,
        citizenName: String,
        pageNumber: Int = 1
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val cleanName = citizenName
            .replace("\\s+".toRegex(), "-")
            .replace("[^a-zA-Z0-9\\u0980-\\u09FF\\-]".toRegex(), "")
            .take(20)
            .ifEmpty { "Citizen" }

        val cleanDocType = when {
            documentType.contains("NID") && documentType.contains("পিতা") -> "Parent-NID"
            documentType.contains("NID") -> "Applicant-NID"
            documentType.contains("জন্ম নিবন্ধন") -> "Parent-Birth-Cert"
            documentType.contains("হাসপাতাল") -> "Hospital-Proof"
            documentType.contains("শিক্ষা") -> "Education-Cert"
            documentType.contains("ঠিকানা") -> "Address-Proof"
            else -> "Other-Doc"
        }

        val pageSuffix = if (pageNumber > 1) "_p$pageNumber" else ""
        return "${fileId}_${cleanDocType}_${cleanName}${pageSuffix}_$dateStr.jpg"
    }

    /**
     * Finds an existing folder or file by name in Google Drive
     */
    suspend fun findDriveItem(
        name: String,
        parentFolderId: String?,
        isFolder: Boolean,
        accessToken: String
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val mimeFilter = if (isFolder) {
                "mimeType = 'application/vnd.google-apps.folder'"
            } else {
                "mimeType != 'application/vnd.google-apps.folder'"
            }

            var query = "name = '$name' and $mimeFilter and trashed = false"
            if (parentFolderId != null) {
                query += " and '$parentFolderId' in parents"
            }

            val url = "https://www.googleapis.com/drive/v3/files" +
                "?q=" + java.net.URLEncoder.encode(query, "UTF-8") +
                "&fields=files(id,name,webViewLink,parents)"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            if (!response.isSuccessful) return@withContext null

            val json = JSONObject(body)
            val files = json.optJSONArray("files") ?: return@withContext null
            if (files.length() > 0) {
                return@withContext files.getJSONObject(0)
            }
        } catch (_: Exception) { }
        return@withContext null
    }

    /**
     * Creates a folder in Google Drive
     */
    suspend fun createDriveFolder(
        folderName: String,
        parentFolderId: String?,
        accessToken: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = JSONObject().apply {
                put("name", folderName)
                put("mimeType", "application/vnd.google-apps.folder")
                if (parentFolderId != null) {
                    put("parents", JSONArray().put(parentFolderId))
                }
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files?fields=id,name")
                .addHeader("Authorization", "Bearer $accessToken")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            if (response.isSuccessful) {
                val json = JSONObject(body)
                return@withContext json.optString("id")
            }
        } catch (_: Exception) { }
        return@withContext null
    }

    /**
     * Gets or creates a folder in Google Drive
     */
    suspend fun getOrCreateFolder(
        folderName: String,
        parentFolderId: String?,
        accessToken: String
    ): String? {
        val existing = findDriveItem(folderName, parentFolderId, isFolder = true, accessToken = accessToken)
        if (existing != null) {
            val id = existing.optString("id")
            if (id.isNotBlank()) return id
        }
        return createDriveFolder(folderName, parentFolderId, accessToken)
    }

    /**
     * Ensures the complete 4-level folder structure exists:
     * Union Parishad File Management
     *    ↓
     * 2026
     *    ↓
     * BR-2026-00001
     *    ↓
     * Citizen Name
     */
    suspend fun ensureFolderHierarchy(
        fileId: String,
        citizenName: String,
        accessToken: String
    ): String? = withContext(Dispatchers.IO) {
        val rootFolderName = "Union Parishad File Management"
        val rootId = getOrCreateFolder(rootFolderName, null, accessToken) ?: return@withContext null

        val currentYear = SimpleDateFormat("yyyy", Locale.US).format(Date())
        val yearId = getOrCreateFolder(currentYear, rootId, accessToken) ?: return@withContext null

        val fileIdFolder = getOrCreateFolder(fileId, yearId, accessToken) ?: return@withContext null

        val cleanName = citizenName.trim().ifEmpty { "Citizen" }
        val citizenFolderId = getOrCreateFolder(cleanName, fileIdFolder, accessToken) ?: return@withContext null

        return@withContext citizenFolderId
    }

    /**
     * Real Google Drive REST API Multipart Upload
     * Follows official Google Drive API v3:
     * https://developers.google.com/drive/api/v3/manage-uploads#multipart
     */
    suspend fun uploadDocumentPage(
        page: DocumentPage,
        fileId: String,
        citizenName: String,
        isConnectedToDrive: Boolean,
        accessToken: String?
    ): DriveUploadResult = withContext(Dispatchers.IO) {
        if (!isConnectedToDrive || accessToken.isNullOrBlank()) {
            return@withContext DriveUploadResult(
                isSuccess = false,
                errorMessage = "Google Drive সংযোগ বিচ্ছিন্ন রয়েছে বা এক্সেস টোকেন নেই। Settings থেকে Connect Google Drive করুন।"
            )
        }

        if (!isNetworkAvailable()) {
            return@withContext DriveUploadResult(
                isSuccess = false,
                errorMessage = "ইন্টারনেট নেই। ফাইলটি Pending Upload হিসেবে রাখা হয়েছে।"
            )
        }

        val localFile = File(page.localFilePath)
        if (!localFile.exists() || localFile.length() == 0L) {
            return@withContext DriveUploadResult(
                isSuccess = false,
                errorMessage = "লোকাল ফাইল পাওয়া যায়নি বা ফাইলের সাইজ ০ বাইট।"
            )
        }

        // 1. Ensure folder structure
        val targetFolderId = ensureFolderHierarchy(fileId, citizenName, accessToken)
            ?: return@withContext DriveUploadResult(
                isSuccess = false,
                errorMessage = "Google Drive-এ ফোল্ডার তৈরি বা অ্যাক্সেস করতে ব্যর্থ হয়েছে। টোকেন মেয়াদোত্তীর্ণ হতে পারে।"
            )

        // 2. Duplicate Protection (Requirement #8)
        val existingFile = findDriveItem(page.fileName, targetFolderId, isFolder = false, accessToken = accessToken)
        if (existingFile != null) {
            val existingId = existingFile.optString("id")
            val webViewLink = existingFile.optString("webViewLink", "https://drive.google.com/file/d/$existingId/view")
            return@withContext DriveUploadResult(
                isSuccess = true,
                driveFileId = existingId,
                driveFolderId = targetFolderId,
                driveWebViewLink = webViewLink,
                isDuplicate = true
            )
        }

        // 3. Perform real multipart upload
        try {
            val metadataJson = JSONObject().apply {
                put("name", page.fileName)
                put("parents", JSONArray().put(targetFolderId))
                put("description", "Union Parishad Birth Registration Document: $fileId - $citizenName")
            }

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "metadata",
                    null,
                    metadataJson.toString().toRequestBody("application/json; charset=UTF-8".toMediaType())
                )
                .addFormDataPart(
                    "file",
                    page.fileName,
                    localFile.asRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val uploadUrl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,webViewLink"
            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(multipartBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val json = JSONObject(responseBody)
                val uploadedFileId = json.optString("id")
                val webViewLink = json.optString("webViewLink", "https://drive.google.com/file/d/$uploadedFileId/view")

                return@withContext DriveUploadResult(
                    isSuccess = true,
                    driveFileId = uploadedFileId,
                    driveFolderId = targetFolderId,
                    driveWebViewLink = webViewLink
                )
            } else {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }

                return@withContext DriveUploadResult(
                    isSuccess = false,
                    errorMessage = "ড্রাইভ আপলোড ব্যর্থ: $errorMsg"
                )
            }
        } catch (e: Exception) {
            return@withContext DriveUploadResult(
                isSuccess = false,
                errorMessage = "নেটওয়ার্ক ত্রুটি: ${e.localizedMessage ?: "অজ্ঞাত সমস্যা"}"
            )
        }
    }
}
