package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentPage
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.CitizenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveSyncScreen(
    viewModel: CitizenViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val pendingPages by viewModel.pendingPages.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()

    var showManualTokenDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var manualEmailInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var manualTokenInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(
            intent = result.data,
            onConsentRequired = { consentIntent ->
                consentLauncher.launch(consentIntent)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Google Drive ও ক্লাউড স্টোরেজ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Drive Connection Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (settings.isDriveConnected) MaterialTheme.colorScheme.primaryContainer else Color(0xFFFFEBEE)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (settings.isDriveConnected) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (settings.isDriveConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (settings.isDriveConnected) "Google Drive সংযুক্ত রয়েছে" else "Google Drive অনুমোদন প্রয়োজন",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (settings.isDriveConnected) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFC62828)
                                    )
                                    Text(
                                        text = if (settings.isDriveConnected) settings.driveAccountEmail else "ক্লাউড সিঙ্কের জন্য Google OAuth কনফিগারেশন প্রয়োজন",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Switch(
                                checked = settings.isDriveConnected,
                                onCheckedChange = { viewModel.toggleDriveConnection(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!settings.isDriveConnected) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ℹ️ অফলাইন নিরাপত্তা: Google Drive সংযোগ না থাকলেও নাগরিকের যাবতীয় তথ্য ও স্ক্যান করা ছবি লোকাল ডাটাবেজে সম্পূর্ণ সুরক্ষিত ও অক্ষত থাকবে। ইন্টারনেট ও OAuth অ্যাক্সেস টোকেন পেলে স্বয়ংক্রিয়ভাবে আপলোড কিউ প্রসেস করা যাবে।",
                                    fontSize = 11.sp,
                                    color = Color(0xFF424242),
                                    modifier = Modifier.padding(10.dp),
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    try {
                                        googleSignInLauncher.launch(viewModel.getGoogleSignInIntent())
                                    } catch (e: Exception) {
                                        viewModel.showMessage("Google Drive সংযোগ করা যায়নি: ${e.localizedMessage}")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_connect_google_drive"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connect Google Drive", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = { showManualTokenDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("ম্যানুয়াল টোকেন / ডেভ কনফিগারেশন", fontSize = 12.sp)
                            }
                        } else {
                            // Connected controls
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.uploadAllPending() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync Now", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.disconnectGoogleDrive() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                                ) {
                                    Text("Disconnect", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Folder Architecture Preview (Requirement #6)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Drive নির্ধারিত ফোল্ডার স্ট্রাকচার",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = """
📁 Union Parishad File Management (Root)
   └── 📁 2026
        └── 📁 BR-2026-00125 - Md Rahim
             ├── 📄 BR-2026-00125_Parent-NID_Md-Rahim_2026-10-01.jpg
             ├── 📄 BR-2026-00125_Applicant-NID_Md-Rahim_2026-10-01.jpg
             └── 📄 BR-2026-00125_Hospital-Proof_Md-Rahim_2026-10-01.jpg
                                """.trimIndent(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF333333),
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ ফাইলের নাম এবং ফোল্ডার স্বয়ংক্রিয়ভাবে প্রমিত কাঠামো অনুযায়ী সংরক্ষিত হয়।",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Offline Upload Queue Header & Bulk Sync Action (Requirement #13 & #14)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "পেন্ডিং আপলোড কিউ (Offline Queue)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "অপেক্ষমান ডকুমেন্ট: $pendingCount টি",
                            fontSize = 12.sp,
                            color = if (pendingCount > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                    }

                    if (pendingCount > 0) {
                        Button(
                            onClick = { viewModel.uploadAllPending() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সব আপলোড করুন (Upload All)", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Pending Queue Items
            if (pendingPages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোনো পেন্ডিং আপলোড নেই!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "সব ডকুমেন্ট সফলভাবে Google Drive-এ সিঙ্ক হয়েছে।",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(pendingPages) { page ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = page.fileName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (badgeText, badgeColor) = when (page.uploadStatus) {
                                        DocumentPage.STATUS_UPLOADING -> Pair("আপলোড হচ্ছে...", Color(0xFF0288D1))
                                        DocumentPage.STATUS_FAILED -> Pair("আপলোড ব্যর্থ (Failed)", Color(0xFFD32F2F))
                                        else -> Pair("Pending Upload", Color(0xFFE65100))
                                    }
                                    Text(
                                        text = badgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor
                                    )
                                    Text(
                                        text = " • ${(page.fileSize / 1024)} KB",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                if (page.uploadError != null) {
                                    Text(
                                        text = page.uploadError,
                                        fontSize = 10.sp,
                                        color = Color.Red,
                                        maxLines = 1
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.retryUploadPage(page.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Retry",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showManualTokenDialog) {
        AlertDialog(
            onDismissRequest = { showManualTokenDialog = false },
            title = { Text("ম্যানুয়াল টোকেন কনফিগারেশন") },
            text = {
                Column {
                    Text(
                        text = "গুগল ক্লাউড কনসোলের মাধ্যমে প্রাপ্ত Bearer Token বা কাস্টম অ্যাক্সেস টোকেন দিয়ে সরাসরি টেস্ট করার জন্য:",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualEmailInput,
                        onValueChange = { manualEmailInput = it },
                        label = { Text("Google Account Email") },
                        placeholder = { Text("rubelbskf@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = manualTokenInput,
                        onValueChange = { manualTokenInput = it },
                        label = { Text("OAuth 2.0 Access Token") },
                        placeholder = { Text("ya29.a0A...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = manualEmailInput.trim().ifEmpty { "rubelbskf@gmail.com" }
                        val token = manualTokenInput.trim()
                        if (token.isNotEmpty()) {
                            viewModel.connectGoogleDriveWithToken(email, token)
                        } else {
                            viewModel.showMessage("অনুগ্রহ করে অ্যাক্সেস টোকেন লিখুন।")
                        }
                        showManualTokenDialog = false
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showManualTokenDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
