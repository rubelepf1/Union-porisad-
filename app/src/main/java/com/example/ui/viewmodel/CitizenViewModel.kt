package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Intent
import com.example.data.drive.DriveManager
import com.example.data.drive.GoogleAuthManager
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseSeeder
import com.example.data.model.ActivityLog
import com.example.data.model.Citizen
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentPage
import com.example.data.model.DocumentWithPages
import com.example.data.model.SubmissionBatch
import com.example.data.repository.AppSettingsData
import com.example.data.repository.CitizenRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.components.BitmapProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardStats(
    val totalFiles: Int = 0,
    val todayFiles: Int = 0,
    val incompleteFiles: Int = 0,
    val completeFiles: Int = 0,
    val readySecretaryFiles: Int = 0,
    val sentUpazilaFiles: Int = 0,
    val pendingUploadsCount: Int = 0
)

class CitizenViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val driveManager = DriveManager(application)
    val googleAuthManager = GoogleAuthManager(application)
    val settingsRepository = SettingsRepository(application)
    val repository = CitizenRepository(
        citizenDao = database.citizenDao(),
        documentDao = database.documentDao(),
        submissionDao = database.submissionDao(),
        activityLogDao = database.activityLogDao(),
        driveManager = driveManager,
        settingsRepository = settingsRepository
    )

    // User settings
    val settings: StateFlow<AppSettingsData> = settingsRepository.settingsFlow

    // Search and filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("সকল ফাইল")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _selectedWardFilter = MutableStateFlow("সকল ওয়ার্ড")
    val selectedWardFilter: StateFlow<String> = _selectedWardFilter.asStateFlow()

    // Dashboard statistics
    val dashboardStats: StateFlow<DashboardStats> = combine(
        repository.allCitizens,
        repository.countToday()
    ) { all, today ->
        DashboardStats(
            totalFiles = all.size,
            todayFiles = today,
            incompleteFiles = all.count { it.status == Citizen.STATUS_INCOMPLETE },
            completeFiles = all.count { it.status == Citizen.STATUS_COMPLETE },
            readySecretaryFiles = all.count { it.status == Citizen.STATUS_READY_SECRETARY },
            sentUpazilaFiles = all.count { it.status == Citizen.STATUS_SENT_UPAZILA }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Filtered citizens flow
    val filteredCitizens: StateFlow<List<Citizen>> = combine(
        repository.allCitizens,
        _searchQuery,
        _selectedStatusFilter,
        _selectedWardFilter
    ) { list, query, status, ward ->
        list.filter { citizen ->
            val matchesQuery = query.isBlank() ||
                citizen.fullName.contains(query, ignoreCase = true) ||
                citizen.mobileNumber.contains(query, ignoreCase = true) ||
                citizen.fileId.contains(query, ignoreCase = true) ||
                citizen.fatherMotherName.contains(query, ignoreCase = true) ||
                citizen.village.contains(query, ignoreCase = true)

            val matchesStatus = when (status) {
                "সকল ফাইল" -> true
                "আজকের ফাইল" -> {
                    val today = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
                    citizen.applicationDate == today
                }
                else -> citizen.status == status
            }

            val matchesWard = when (ward) {
                "সকল ওয়ার্ড" -> true
                else -> citizen.wardNo == ward
            }

            matchesQuery && matchesStatus && matchesWard
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending upload pages
    val pendingPages: StateFlow<List<DocumentPage>> = repository.pendingPagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = repository.pendingUploadsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Submissions
    val submissionBatches: StateFlow<List<SubmissionBatch>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently viewed Citizen details
    private val _currentCitizenId = MutableStateFlow<Long?>(null)
    val currentCitizenId: StateFlow<Long?> = _currentCitizenId.asStateFlow()

    private val _selectedCitizen = MutableStateFlow<Citizen?>(null)
    val selectedCitizen: StateFlow<Citizen?> = _selectedCitizen.asStateFlow()

    private val _citizenDocuments = MutableStateFlow<List<DocumentWithPages>>(emptyList())
    val citizenDocuments: StateFlow<List<DocumentWithPages>> = _citizenDocuments.asStateFlow()

    private val _citizenLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val citizenLogs: StateFlow<List<ActivityLog>> = _citizenLogs.asStateFlow()

    // Message/Notification
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Scanner / Camera active state
    val activeScanningDoc = MutableStateFlow<DocumentItem?>(null)
    val capturedImageUri = MutableStateFlow<Uri?>(null)
    val scannedBitmap = MutableStateFlow<Bitmap?>(null)
    val scanRotation = MutableStateFlow(0f)
    val scanBrightness = MutableStateFlow(0f)
    val scanContrast = MutableStateFlow(1f)
    val scanFilterMode = MutableStateFlow("স্বাভাবিক") // স্বাভাবিক, স্ক্যান/ডকুমেন্ট, সাদাকালো, উজ্জ্বল
    val scanBlurWarning = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            DatabaseSeeder.seedSampleDataIfEmpty(database, getApplication())
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterSelected(status: String) {
        _selectedStatusFilter.value = status
    }

    fun onWardFilterSelected(ward: String) {
        _selectedWardFilter.value = ward
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun selectCitizen(citizenId: Long) {
        _currentCitizenId.value = citizenId
        viewModelScope.launch {
            repository.getCitizenById(citizenId).collect { citizen ->
                _selectedCitizen.value = citizen
            }
        }
        viewModelScope.launch {
            repository.getDocumentsWithPages(citizenId).collect { docs ->
                _citizenDocuments.value = docs
            }
        }
        viewModelScope.launch {
            repository.getActivityLogs(citizenId).collect { logs ->
                _citizenLogs.value = logs
            }
        }
    }

    suspend fun getNextFileIdPreview(serviceType: String): String {
        return repository.generateNextFileId(serviceType)
    }

    fun createNewCitizen(
        fullName: String,
        fatherMotherName: String,
        dateOfBirth: String,
        mobileNumber: String,
        address: String,
        wardNo: String,
        village: String,
        serviceType: String,
        applicationDate: String,
        remarks: String,
        onSuccess: (citizenId: Long, firstDoc: DocumentItem?) -> Unit
    ) {
        if (fullName.isBlank() || mobileNumber.isBlank()) {
            _userMessage.value = "অনুগ্রহ করে পূর্ণ নাম এবং মোবাইল নম্বর লিখুন।"
            return
        }

        viewModelScope.launch {
            try {
                val newId = repository.createCitizen(
                    fullName = fullName,
                    fatherMotherName = fatherMotherName,
                    dateOfBirth = dateOfBirth,
                    mobileNumber = mobileNumber,
                    address = address,
                    wardNo = wardNo,
                    village = village,
                    serviceType = serviceType,
                    applicationDate = applicationDate,
                    remarks = remarks
                )
                _userMessage.value = "নতুন ফাইল সফলভাবে তৈরি হয়েছে!"
                selectCitizen(newId)
                val docs = repository.getDocumentsForCitizen(newId)
                onSuccess(newId, docs.firstOrNull())
            } catch (e: Exception) {
                _userMessage.value = "ফাইল তৈরিতে সমস্যা হয়েছে: ${e.localizedMessage}"
            }
        }
    }

    fun updateStatus(citizenId: Long, newStatus: String, notes: String = "") {
        viewModelScope.launch {
            repository.updateCitizenStatus(citizenId, newStatus, notes)
            _userMessage.value = "স্ট্যাটাস পরিবর্তন করে '$newStatus' করা হয়েছে।"
        }
    }

    fun startScanning(document: DocumentItem) {
        activeScanningDoc.value = document
        scannedBitmap.value = null
        scanRotation.value = 0f
        scanBrightness.value = 0f
        scanContrast.value = 1f
        scanFilterMode.value = "স্বাভাবিক"
        scanBlurWarning.value = false
    }

    fun processCapturedImage(uri: Uri) {
        capturedImageUri.value = uri
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    // Check blur / low resolution heuristic
                    val isLowRes = originalBitmap.width < 600 || originalBitmap.height < 600
                    scanBlurWarning.value = isLowRes
                    scannedBitmap.value = originalBitmap
                }
            } catch (e: Exception) {
                _userMessage.value = "ছবি প্রসেস করতে ব্যর্থ হয়েছে: ${e.localizedMessage}"
            }
        }
    }

    fun rotateScan() {
        val current = scanRotation.value
        scanRotation.value = (current + 90f) % 360f
    }

    fun setFilterMode(mode: String) {
        scanFilterMode.value = mode
    }

    fun saveScannedPage(onSaved: () -> Unit) {
        val doc = activeScanningDoc.value ?: return
        val bitmap = scannedBitmap.value ?: return
        val citizen = _selectedCitizen.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Apply rotation and filters accurately using BitmapProcessor
                val processedBitmap = BitmapProcessor.processBitmap(
                    source = bitmap,
                    rotationDegrees = scanRotation.value,
                    filterMode = scanFilterMode.value,
                    contrastMultiplier = scanContrast.value,
                    brightnessOffset = scanBrightness.value
                )

                // Save to local app storage
                val documentsDir = File(getApplication<Application>().filesDir, "citizen_documents")
                if (!documentsDir.exists()) documentsDir.mkdirs()

                val timeStamp = System.currentTimeMillis()
                val targetFile = File(documentsDir, "${citizen.fileId}_doc_${doc.id}_$timeStamp.jpg")
                FileOutputStream(targetFile).use { out ->
                    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }

                repository.addDocumentPage(
                    citizenId = citizen.id,
                    documentId = doc.id,
                    localFilePath = targetFile.absolutePath,
                    fileSize = targetFile.length()
                )

                withContext(Dispatchers.Main) {
                    _userMessage.value = "কাগজের ছবি সফলভাবে প্রসেস ও লোকাল স্টোরেজে সংরক্ষিত হয়েছে!"
                    activeScanningDoc.value = null
                    scannedBitmap.value = null
                    onSaved()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "সংরক্ষণ ব্যর্থ হয়েছে: ${e.localizedMessage}"
                }
            }
        }
    }

    fun retryUploadPage(pageId: Long) {
        viewModelScope.launch {
            val page = database.documentDao().getPageById(pageId)
            if (page != null && page.uploadStatus == DocumentPage.STATUS_UPLOADED && !page.driveFileId.isNullOrBlank()) {
                _userMessage.value = "Already uploaded: এই ডকুমেন্টটি ইতিমধ্যে Google Drive-এ আপলোড সম্পন্ন হয়েছে।"
                return@launch
            }

            val currentSettings = settings.value
            if (!currentSettings.isDriveConnected || currentSettings.googleAccessToken.isNullOrBlank()) {
                _userMessage.value = "Google Drive সংযোগ বিচ্ছিন্ন রয়েছে। Settings থেকে Google Account সংযুক্ত করুন।"
                return@launch
            }

            _userMessage.value = "Google Drive-এ আপলোড করা হচ্ছে..."
            val ok = repository.uploadSinglePage(pageId)
            if (ok) {
                _userMessage.value = "Google Drive-এ সফলভাবে আপলোড সম্পন্ন হয়েছে!"
            } else {
                val updatedPage = database.documentDao().getPageById(pageId)
                val err = updatedPage?.uploadError ?: "ইন্টারনেট সংযোগ বা টোকেন চেক করুন।"
                _userMessage.value = "আপলোড ব্যর্থ: $err"
            }
        }
    }

    fun uploadAllPending() {
        viewModelScope.launch {
            val currentSettings = settings.value
            if (!currentSettings.isDriveConnected || currentSettings.googleAccessToken.isNullOrBlank()) {
                _userMessage.value = "Google Drive সংযোগ বিচ্ছিন্ন রয়েছে। প্রথমে Google Drive কানেক্ট করুন।"
                return@launch
            }

            _userMessage.value = "পেন্ডিং ডকুমেন্ট Google Drive-এ আপলোড শুরু হচ্ছে..."
            val count = repository.uploadAllPendingPages()
            if (count > 0) {
                _userMessage.value = "$count টি ফাইল সফলভাবে Google Drive-এ আপলোড হয়েছে!"
            } else {
                _userMessage.value = "কোনো ফাইল আপলোড হয়নি। নেটওয়ার্ক ও টোকেন স্ট্যাটাস চেক করুন।"
            }
        }
    }

    fun deletePage(page: DocumentPage) {
        viewModelScope.launch {
            repository.deleteDocumentPage(page)
            _userMessage.value = "ডকুমেন্ট পৃষ্ঠা ডিলিট করা হয়েছে।"
        }
    }

    fun createSubmissionBatch(citizenIds: List<Long>, remarks: String, onComplete: (String) -> Unit) {
        if (citizenIds.isEmpty()) {
            _userMessage.value = "কমপক্ষে একটি ফাইল নির্বাচন করুন।"
            return
        }
        viewModelScope.launch {
            val batchId = repository.createSubmissionBatch(citizenIds, remarks)
            _userMessage.value = "সাবমিশন ব্যাচ $batchId সফলভাবে তৈরি হয়েছে!"
            onComplete(batchId)
        }
    }

    fun getGoogleSignInIntent(): Intent {
        return googleAuthManager.getSignInIntent()
    }

    fun handleGoogleSignInResult(
        intent: Intent?,
        onConsentRequired: ((Intent) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _userMessage.value = "Google Account অনুমোদন যাচাই করা হচ্ছে..."
            when (val result = googleAuthManager.handleSignInResult(intent)) {
                is GoogleAuthManager.AuthResult.Success -> {
                    settingsRepository.updateDriveConnection(
                        connected = true,
                        email = result.email,
                        accessToken = result.accessToken
                    )
                    _userMessage.value = "Google Account (${result.email}) সফলভাবে সংযুক্ত হয়েছে! Drive API সক্রিয়।"
                }
                is GoogleAuthManager.AuthResult.NeedsUserConsent -> {
                    if (onConsentRequired != null) {
                        onConsentRequired(result.recoveryIntent)
                    } else {
                        _userMessage.value = "Google Drive অ্যাক্সেসের জন্য অতিরিক্ত অনুমতি অনুমোদন প্রয়োজন।"
                    }
                }
                is GoogleAuthManager.AuthResult.Failure -> {
                    settingsRepository.updateDriveConnection(connected = false, email = "")
                    _userMessage.value = "Google Drive সংযোগ করা যায়নি: ${result.errorMessage}"
                }
            }
        }
    }

    fun connectGoogleDriveWithToken(email: String, token: String, refreshToken: String? = null) {
        settingsRepository.updateDriveConnection(
            connected = true,
            email = email,
            accessToken = token,
            refreshToken = refreshToken
        )
        _userMessage.value = "Google Account ($email) সফলভাবে সংযুক্ত হয়েছে।"
    }

    fun disconnectGoogleDrive() {
        viewModelScope.launch {
            googleAuthManager.signOut()
            settingsRepository.disconnectDrive()
            _userMessage.value = "Google Drive সংযোগ বিচ্ছিন্ন করা হয়েছে।"
        }
    }

    fun toggleDriveConnection(connected: Boolean) {
        if (connected) {
            _userMessage.value = "Google Drive-এ যুক্ত করতে Connect বোতাম ব্যবহার করুন।"
        } else {
            disconnectGoogleDrive()
        }
    }

    fun updateSettings(upName: String, entrepreneurName: String, upazilaDistrict: String) {
        settingsRepository.updateProfile(upName, entrepreneurName, upazilaDistrict)
        _userMessage.value = "প্রোফাইল তথ্য আপডেট করা হয়েছে।"
    }

    fun addCustomDocumentCategory(citizenId: Long, categoryName: String) {
        if (categoryName.isBlank()) return
        viewModelScope.launch {
            repository.addNewDocumentCategory(citizenId, categoryName)
            _userMessage.value = "'$categoryName' তালিকাভুক্ত করা হয়েছে।"
        }
    }
}
