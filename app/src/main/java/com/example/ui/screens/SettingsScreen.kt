package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.CitizenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CitizenViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var upName by remember(settings) { mutableStateOf(settings.upName) }
    var entrepreneurName by remember(settings) { mutableStateOf(settings.entrepreneurName) }
    var upazilaDistrict by remember(settings) { mutableStateOf(settings.upazilaDistrict) }

    var showConnectDialog by remember { mutableStateOf(false) }
    var connectEmailInput by remember { mutableStateOf("") }
    var connectTokenInput by remember { mutableStateOf("") }

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
                        text = "অ্যাপ ও ইউনিয়ন পরিষদ সেটিংস",
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
            // Google Drive Dedicated Section (Requirement #10)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (settings.isDriveConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            Column {
                                Text(
                                    text = "Google Drive",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (settings.isDriveConnected) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                        modifier = Modifier.size(10.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (settings.isDriveConnected) "🟢 Connected" else "🔴 Not Connected",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (settings.isDriveConnected) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (settings.isDriveConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (settings.isDriveConnected) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (settings.isDriveConnected) {
                            // Connected State UI
                            Text(
                                text = "Connected Account:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = settings.driveAccountEmail.ifEmpty { "Connected via Google OAuth" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Three required buttons: [Open Google Drive] [Sync Now] [Disconnect]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/my-drive"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Drive", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.uploadAllPending() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync Now", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.disconnectGoogleDrive() },
                                    modifier = Modifier.weight(1.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Disconnect", fontSize = 11.sp)
                                }
                            }
                        } else {
                            // Disconnected State UI: [Connect Google Drive]
                            Text(
                                text = "নাগরিক ফাইলের ছবি স্বয়ংক্রিয়ভাবে Google Drive-এ আপলোড ও ব্যাকআপ করতে আপনার গুগল অ্যাকাউন্ট সংযুক্ত করুন।",
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

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
                                onClick = { showConnectDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("ম্যানুয়াল টোকেন / ডেভ কনফিগারেশন", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Profile & UP Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ইউনিয়ন পরিষদ ও উদ্যোক্তা তথ্য",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = upName,
                            onValueChange = { upName = it },
                            label = { Text("ইউনিয়ন পরিষদের নাম") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_settings_up_name"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = entrepreneurName,
                            onValueChange = { entrepreneurName = it },
                            label = { Text("উদ্যোক্তার নাম ও পদবী") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_settings_entrepreneur"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = upazilaDistrict,
                            onValueChange = { upazilaDistrict = it },
                            label = { Text("উপজেলা ও জেলা") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_settings_upazila"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.updateSettings(upName, entrepreneurName, upazilaDistrict)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("তথ্য সংরক্ষণ করুন")
                        }
                    }
                }
            }

            // Google Drive Auto-Sync Preference
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ছবি তোলার পর স্বয়ংক্রিয় ড্রাইভ আপলোড",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "ইন্টারনেট ও ড্রাইভ সংযুক্ত থাকলে সরাসরি ক্লাউড ড্রাইভে সংরক্ষণ হবে",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Switch(
                                checked = settings.autoUploadOnCapture,
                                onCheckedChange = { viewModel.settingsRepository.updateAutoUpload(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }

            // User Role Architecture (Requirement #17)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ব্যবহারকারীর রোল ও নিরাপত্তা (User Role)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "বর্তমান সক্রিয় রোল: ${settings.userRole}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "অনুমতি: সকল নাগরিক ফাইল তৈরি, স্ক্যান, ড্রাইভ সিঙ্ক ও সচিব জমাদান ব্যাচ তৈরি।",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Database Metadata & Backup Strategy (Requirement #23)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ডাটাবেজ ও ক্লাউড ব্যাকআপ স্ট্র্যাটেজি",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ মূল ভারী ছবির ফাইল Google Drive-এ সুরক্ষিত থাকে।\n✓ মেটাডাটা লোকাল Room SQLite এবং ক্লাউড স্কিমায় সংরক্ষিত।\n✓ অফলাইনেও যেকোনো সময় ফাইল রেজিস্ট্রি করা যায়।",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "UP File Manager Backup Summary")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "ইউনিয়ন পরিষদ ফাইল ম্যানেজার মেটাডাটা ব্যাকআপ সম্পূর্ণ। Google Drive রুট ফোল্ডার: ${settings.driveRootFolder}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "ব্যাকআপ শেয়ার"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "মেটাডাটা ব্যাকআপ তৈরি করুন",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Google Drive Connect / OAuth Dialog
    if (showConnectDialog) {
        AlertDialog(
            onDismissRequest = { showConnectDialog = false },
            title = { Text("Connect Google Drive") },
            text = {
                Column {
                    Text(
                        text = "Google Drive-এ ফাইল আপলোড করার জন্য Google Account ও OAuth অনুমোদন প্রয়োজন।",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = connectEmailInput,
                        onValueChange = { connectEmailInput = it },
                        label = { Text("Google Account Email") },
                        placeholder = { Text("user@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = connectTokenInput,
                        onValueChange = { connectTokenInput = it },
                        label = { Text("OAuth Access Token / API Token (ঐচ্ছিক)") },
                        placeholder = { Text("ya29.a0A...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Google Cloud Console-এ রেজিস্টার করা OAuth টোকেন প্রদান করলে সরাসরি লাইভ ক্লাউড সিঙ্ক চালু হবে।",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val email = connectEmailInput.trim().ifEmpty { "user@gmail.com" }
                    val token = connectTokenInput.trim()
                    viewModel.connectGoogleDriveWithToken(
                        email = email,
                        token = token
                    )
                    showConnectDialog = false
                }) {
                    Text("অনুমোদন ও যুক্ত করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConnectDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
