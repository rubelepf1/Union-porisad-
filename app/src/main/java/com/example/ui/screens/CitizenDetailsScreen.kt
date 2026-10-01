package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Citizen
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentPage
import com.example.data.model.DocumentWithPages
import com.example.ui.components.PrintableReportDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.CitizenViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDetailsScreen(
    viewModel: CitizenViewModel,
    citizenId: Long,
    onBack: () -> Unit,
    onNavigateToScan: (DocumentItem) -> Unit
) {
    val citizen by viewModel.selectedCitizen.collectAsState()
    val documentsWithPages by viewModel.citizenDocuments.collectAsState()
    val activityLogs by viewModel.citizenLogs.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showAddCustomDocDialog by remember { mutableStateOf(false) }
    var newDocCategoryName by remember { mutableStateOf("") }
    var showPrintDialog by remember { mutableStateOf(false) }
    var selectedPageForPreview by remember { mutableStateOf<DocumentPage?>(null) }
    var pageToDelete by remember { mutableStateOf<DocumentPage?>(null) }

    // Android Photo Picker for picking from gallery
    var pickingForDocItem by remember { mutableStateOf<DocumentItem?>(null) }
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && pickingForDocItem != null) {
            val doc = pickingForDocItem!!
            viewModel.startScanning(doc)
            viewModel.processCapturedImage(uri)
            onNavigateToScan(doc)
        }
    }

    if (citizen == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ফাইল তথ্য লোড হচ্ছে...")
        }
        return
    }

    val cit = citizen!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = cit.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "ফাইল আইডি: ${cit.fileId}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPrintDialog = true }) {
                        Icon(Icons.Default.Print, contentDescription = "Print Summary")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Citizen Header Overview Card (Requirement #22 UX Spec)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = cit.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = cit.fileId,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        StatusBadge(status = cit.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cit.mobileNumber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "ওয়ার্ড: ${cit.wardNo} | গ্রাম: ${cit.village}",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "স্ট্যাটাস পরিবর্তন",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (cit.status != Citizen.STATUS_READY_SECRETARY) {
                            Button(
                                onClick = {
                                    viewModel.updateStatus(
                                        cit.id,
                                        Citizen.STATUS_READY_SECRETARY,
                                        "সচিবের কাছে জমা দেওয়ার জন্য প্রস্তুত চিহ্নিত করা হয়েছে"
                                    )
                                },
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "সচিবে জমা প্রস্তুত 📤",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("কাগজপত্র (${documentsWithPages.count { it.documentItem.isUploaded }}/${documentsWithPages.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("ব্যক্তিগত তথ্য") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("অ্যাক্টিভিটি হিস্ট্রি") }
                )
            }

            // Tab Contents
            when (selectedTabIndex) {
                0 -> {
                    // Documents Section (Requirement #4 & #5)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "প্রয়োজনীয় কাগজপত্র তালিকা",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedButton(
                                    onClick = { showAddCustomDocDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("অন্যান্য কাগজ যোগ", fontSize = 11.sp)
                                }
                            }
                        }

                        items(documentsWithPages) { docWithPages ->
                            val doc = docWithPages.documentItem
                            val pages = docWithPages.pages

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (doc.isUploaded) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (doc.isUploaded) Color(0xFF2E7D32) else Color(0xFFE65100),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = doc.documentType,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (doc.isUploaded) "✓ সংগৃহীত (${pages.size} পৃষ্ঠা)" else "○ সংগ্রহ বাকি",
                                                    fontSize = 12.sp,
                                                    color = if (doc.isUploaded) Color(0xFF2E7D32) else Color(0xFFE65100)
                                                )
                                            }
                                        }

                                        // Capture & Pick Actions
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            // Camera Capture button
                                            Button(
                                                onClick = {
                                                    viewModel.startScanning(doc)
                                                    onNavigateToScan(doc)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.CameraAlt,
                                                    contentDescription = "Camera",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (pages.isEmpty()) "ছবি তুলুন" else "Add Page",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Gallery Pick button
                                            OutlinedButton(
                                                onClick = {
                                                    pickingForDocItem = doc
                                                    galleryPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Image,
                                                    contentDescription = "Gallery",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Gallery", fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    // Display Pages Thumbnails & Drive Status
                                    if (pages.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = Color(0xFFEEEEEE))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(pages) { page ->
                                                DocumentPageThumbnailItem(
                                                    page = page,
                                                    onView = { selectedPageForPreview = page },
                                                    onRetry = { viewModel.retryUploadPage(page.id) },
                                                    onDelete = { pageToDelete = page }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Personal Information Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    InfoField("পূর্ণ নাম", cit.fullName)
                                    InfoField("পিতা / মাতার নাম", cit.fatherMotherName)
                                    InfoField("জন্মতারিখ", cit.dateOfBirth.ifEmpty { "উল্লেখ নেই" })
                                    InfoField("মোবাইল নম্বর", cit.mobileNumber)
                                    InfoField("গ্রাম", cit.village.ifEmpty { "উল্লেখ নেই" })
                                    InfoField("ওয়ার্ড নম্বর", cit.wardNo)
                                    InfoField("ঠিকানা", cit.address.ifEmpty { "উল্লেখ নেই" })
                                    InfoField("কাজের ধরন", cit.serviceType)
                                    InfoField("আবেদনের তারিখ", cit.applicationDate)
                                    InfoField("ফাইল আইডি", cit.fileId)
                                    if (cit.submissionBatchId != null) {
                                        InfoField("জমাদান ব্যাচ নং", cit.submissionBatchId!!)
                                    }
                                    if (cit.remarks.isNotBlank()) {
                                        InfoField("মন্তব্য / বিশেষ নোট", cit.remarks)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Activity Log Tab (Requirement #10)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "ফাইল কার্যবিবরণী ও স্ট্যাটাস হিস্ট্রি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (activityLogs.isEmpty()) {
                            item {
                                Text("কোনো হিস্ট্রি লগ পাওয়া যায়নি।", color = Color.Gray, fontSize = 13.sp)
                            }
                        } else {
                            items(activityLogs) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = log.action,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            if (log.details.isNotBlank()) {
                                                Text(
                                                    text = log.details,
                                                    fontSize = 12.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                            Text(
                                                text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(log.timestamp)),
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Status Changer Dialog (Requirement #10)
    if (showStatusDialog) {
        var selectedStatus by remember { mutableStateOf(cit.status) }
        var statusNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("ফাইলের স্ট্যাটাস পরিবর্তন") },
            text = {
                Column {
                    Citizen.ALL_STATUSES.forEach { statusOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = statusOption }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStatus == statusOption,
                                onClick = { selectedStatus = statusOption }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(statusOption, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = statusNote,
                        onValueChange = { statusNote = it },
                        label = { Text("পরিবর্তনের কারণ / নোট (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStatus(cit.id, selectedStatus, statusNote)
                        showStatusDialog = false
                    }
                ) {
                    Text("আপডেট করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStatusDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Add Custom Document Category Dialog
    if (showAddCustomDocDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomDocDialog = false },
            title = { Text("নতুন কাগজের নাম যুক্ত করুন") },
            text = {
                OutlinedTextField(
                    value = newDocCategoryName,
                    onValueChange = { newDocCategoryName = it },
                    label = { Text("ডকুমেন্টের নাম") },
                    placeholder = { Text("উদা: বিদ্যুৎ বিল / প্রত্যয়ন পত্র") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDocCategoryName.isNotBlank()) {
                            viewModel.addCustomDocumentCategory(cit.id, newDocCategoryName)
                            newDocCategoryName = ""
                            showAddCustomDocDialog = false
                        }
                    }
                ) {
                    Text("যুক্ত করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddCustomDocDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Printable Single Citizen Report Dialog
    if (showPrintDialog) {
        PrintableReportDialog(
            upName = settings.upName,
            entrepreneurName = settings.entrepreneurName,
            upazilaDistrict = settings.upazilaDistrict,
            title = "নাগরিক জন্ম নিবন্ধন ফাইল রেকর্ড (${cit.fileId})",
            citizens = listOf(cit),
            documentsForSingle = documentsWithPages,
            onDismiss = { showPrintDialog = false }
        )
    }

    // Page Full Preview & Details Dialog
    if (selectedPageForPreview != null) {
        val p = selectedPageForPreview!!
        AlertDialog(
            onDismissRequest = { selectedPageForPreview = null },
            title = { Text("পৃষ্ঠা ${p.pageNumber}: ${p.fileName}", fontSize = 14.sp) },
            text = {
                Column {
                    val file = File(p.localFilePath)
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Page Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color(0xFFEEEEEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("কাগজের ছবি প্রিভিউ", color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Google Drive স্ট্যাটাস: ${p.uploadStatus.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (p.driveFileId != null) {
                        Text("Drive File ID: ${p.driveFileId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    if (p.driveWebViewLink != null) {
                        Text("Web Link: ${p.driveWebViewLink}", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                    }
                    if (p.uploadError != null) {
                        Text("ত্রুটি: ${p.uploadError}", fontSize = 11.sp, color = Color.Red)
                    }
                }
            },
            confirmButton = {
                if (p.uploadStatus != DocumentPage.STATUS_UPLOADED) {
                    Button(onClick = {
                        viewModel.retryUploadPage(p.id)
                        selectedPageForPreview = null
                    }) {
                        Text("Drive-এ আপলোড করুন")
                    }
                } else {
                    Button(onClick = { selectedPageForPreview = null }) {
                        Text("ঠিক আছে")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedPageForPreview = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Delete Page Confirmation Dialog
    if (pageToDelete != null) {
        val page = pageToDelete!!
        AlertDialog(
            onDismissRequest = { pageToDelete = null },
            title = { Text("পৃষ্ঠা মুছে ফেলার নিশ্চয়তা") },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিতভাবে পৃষ্ঠা ${page.pageNumber} (${page.fileName}) মুছে ফেলতে চান? এটি লোকাল মেমোরি থেকে সম্পূর্ণ ডিলিট হয়ে যাবে।",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePage(page)
                        pageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pageToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun InfoField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
    }
}

@Composable
fun DocumentPageThumbnailItem(
    page: DocumentPage,
    onView: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(115.dp)
            .clickable { onView() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val file = File(page.localFilePath)
            if (file.exists()) {
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.LightGray, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "পৃষ্ঠা ${page.pageNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold)

            // Truthful Drive Status Pill
            when (page.uploadStatus) {
                DocumentPage.STATUS_UPLOADED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("ড্রাইভে সিঙ্ক", fontSize = 9.sp, color = Color(0xFF2E7D32))
                    }
                }
                DocumentPage.STATUS_UPLOADING -> {
                    Text("আপলোড হচ্ছে...", fontSize = 9.sp, color = Color(0xFF0288D1), fontWeight = FontWeight.SemiBold)
                }
                DocumentPage.STATUS_FAILED -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onRetry() }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color(0xFFD32F2F), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("ব্যর্থ (Retry)", fontSize = 9.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    // STATUS_PENDING - Offline saved
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onRetry() }
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Pending Upload", tint = Color(0xFFE65100), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("পেন্ডিং (লোকাল)", fontSize = 9.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Medium)
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Page", tint = Color.Gray, modifier = Modifier.size(14.dp))
            }
        }
    }
}
