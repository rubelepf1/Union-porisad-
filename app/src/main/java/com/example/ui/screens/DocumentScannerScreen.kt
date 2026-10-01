package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.viewmodel.CitizenViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    viewModel: CitizenViewModel,
    onBack: () -> Unit,
    onSavedSuccessfully: () -> Unit
) {
    val context = LocalContext.current
    val activeDoc by viewModel.activeScanningDoc.collectAsState()
    val scannedBitmap by viewModel.scannedBitmap.collectAsState()
    val rotation by viewModel.scanRotation.collectAsState()
    val filterMode by viewModel.scanFilterMode.collectAsState()
    val blurWarning by viewModel.scanBlurWarning.collectAsState()
    val citizen by viewModel.selectedCitizen.collectAsState()

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showAdjustSliders by remember { mutableStateOf(false) }
    var contrastSlider by remember { mutableFloatStateOf(1f) }
    var brightnessSlider by remember { mutableFloatStateOf(0f) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    // Camera Launcher with FileProvider
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.processCapturedImage(tempCameraUri!!)
        }
    }

    // Camera Permission Launcher
    fun launchCameraInternal() {
        val cacheFile = File(context.cacheDir, "scan_capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )
        tempCameraUri = uri
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraInternal()
        } else {
            showCameraPermissionDialog = true
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCameraInternal()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery Picker
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.processCapturedImage(uri)
        }
    }

    // Auto-launch camera if no image loaded yet
    LaunchedEffect(scannedBitmap) {
        if (scannedBitmap == null) {
            // Keep ready for user action
        }
    }

    val filterPresets = listOf("স্বাভাবিক", "ডকুমেন্ট/স্ক্যান", "সাদাকালো", "উজ্জ্বল")

    // Compose ColorMatrix for real-time document filter preview
    val colorMatrix = remember(filterMode, contrastSlider, brightnessSlider) {
        when (filterMode) {
            "সাদাকালো" -> ColorMatrix().apply { setToSaturation(0f) }
            "ডকুমেন্ট/স্ক্যান" -> {
                // High contrast document mode
                val c = 1.3f * contrastSlider
                val b = 20f + brightnessSlider
                ColorMatrix(
                    floatArrayOf(
                        c, 0f, 0f, 0f, b,
                        0f, c, 0f, 0f, b,
                        0f, 0f, c, 0f, b,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            "উজ্জ্বল" -> {
                val b = 30f + brightnessSlider
                ColorMatrix(
                    floatArrayOf(
                        1.1f, 0f, 0f, 0f, b,
                        0f, 1.1f, 0f, 0f, b,
                        0f, 0f, 1.1f, 0f, b,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            else -> {
                // Normal with slider adjustments
                if (contrastSlider != 1f || brightnessSlider != 0f) {
                    ColorMatrix(
                        floatArrayOf(
                            contrastSlider, 0f, 0f, 0f, brightnessSlider,
                            0f, contrastSlider, 0f, 0f, brightnessSlider,
                            0f, 0f, contrastSlider, 0f, brightnessSlider,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                } else {
                    ColorMatrix()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ডকুমেন্ট ক্যামেরা স্ক্যানার",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${activeDoc?.documentType ?: "কাগজপত্র"} — ${citizen?.fileId ?: ""}",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E211E))
                .padding(innerPadding)
        ) {
            // Blur or Low Quality Warning Banner (Requirement #5)
            AnimatedVisibility(visible = blurWarning) {
                Surface(
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সতর্কতা: ছবি ঝাপসা বা আলো কম হতে পারে। প্রয়োজনে রিটেক করুন।",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Main Scanner Viewport with Edge Detection Overlay Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (scannedBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = scannedBitmap!!.asImageBitmap(),
                        contentDescription = "Document Capture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .rotate(rotation),
                        colorFilter = ColorFilter.colorMatrix(colorMatrix)
                    )
                } else {
                    // Empty Scanner Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2B302B), RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFF4CAF50).copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "কাগজের ছবি তোলার জন্য ক্যামেরা চালু করুন",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "অথবা গ্যালারি থেকে পূর্বে তোলা ছবি বাছাই করুন",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { launchCamera() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ক্যামেরা খুলুন")
                                }

                                OutlinedButton(
                                    onClick = {
                                        galleryPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("গ্যালারি")
                                }
                            }
                        }
                    }
                }

                // Document Edge Detection Overlay Frame Guide
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val frameWidth = size.width * 0.88f
                    val frameHeight = size.height * 0.88f
                    val left = (size.width - frameWidth) / 2
                    val top = (size.height - frameHeight) / 2

                    // Corner brackets
                    val cornerLen = 30.dp.toPx()
                    val strokeW = 3.dp.toPx()
                    val cornerColor = androidx.compose.ui.graphics.Color(0xFF00E676)

                    // Top-Left
                    drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
                    drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)

                    // Top-Right
                    drawLine(cornerColor, Offset(left + frameWidth, top), Offset(left + frameWidth - cornerLen, top), strokeW)
                    drawLine(cornerColor, Offset(left + frameWidth, top), Offset(left + frameWidth, top + cornerLen), strokeW)

                    // Bottom-Left
                    drawLine(cornerColor, Offset(left, top + frameHeight), Offset(left + cornerLen, top + frameHeight), strokeW)
                    drawLine(cornerColor, Offset(left, top + frameHeight), Offset(left, top + frameHeight - cornerLen), strokeW)

                    // Bottom-Right
                    drawLine(cornerColor, Offset(left + frameWidth, top + frameHeight), Offset(left + frameWidth - cornerLen, top + frameHeight), strokeW)
                    drawLine(cornerColor, Offset(left + frameWidth, top + frameHeight), Offset(left + frameWidth, top + frameHeight - cornerLen), strokeW)
                }
            }

            // Scanner Toolbar Controls: Filter Presets, Rotate, Brightness/Contrast
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Filter Mode Selection Chips
                    Text(
                        text = "স্ক্যান ফিল্টার (Presets):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterPresets) { mode ->
                            FilterChip(
                                selected = filterMode == mode,
                                onClick = { viewModel.setFilterMode(mode) },
                                label = { Text(mode, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Adjustment Sliders (Toggleable)
                    AnimatedVisibility(visible = showAdjustSliders) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("উজ্জ্বলতা (Brightness)", fontSize = 11.sp, color = Color.Gray)
                                Text("${brightnessSlider.toInt()}", fontSize = 11.sp)
                            }
                            Slider(
                                value = brightnessSlider,
                                onValueChange = { brightnessSlider = it },
                                valueRange = -50f..50f,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("কনট্রাস্ট (Contrast)", fontSize = 11.sp, color = Color.Gray)
                                Text(String.format(java.util.Locale.US, "%.1fx", contrastSlider), fontSize = 11.sp)
                            }
                            Slider(
                                value = contrastSlider,
                                onValueChange = { contrastSlider = it },
                                valueRange = 0.5f..2.5f,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tool Icons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Retake
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { launchCamera() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text("রিটেক", fontSize = 11.sp, color = Color.DarkGray)
                        }

                        // Rotate 90 deg
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { viewModel.rotateScan() }) {
                                Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text("ঘোরান", fontSize = 11.sp, color = Color.DarkGray)
                        }

                        // Adjust toggle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { showAdjustSliders = !showAdjustSliders }) {
                                Icon(Icons.Default.Brightness6, contentDescription = "Adjust", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text("এডজাস্ট", fontSize = 11.sp, color = Color.DarkGray)
                        }

                        // Gallery pick alternate
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                Icon(Icons.Default.Image, contentDescription = "Pick Image", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text("গ্যালারি", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Final Save Button
                    Button(
                        onClick = {
                            viewModel.saveScannedPage(onSaved = onSavedSuccessfully)
                        },
                        enabled = scannedBitmap != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_scanned_page"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সংরক্ষণ করুন ও পরবর্তী পৃষ্ঠা/সম্পন্ন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    if (showCameraPermissionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("ক্যামেরা ব্যবহারের অনুমতি প্রয়োজন") },
            text = {
                Text(
                    text = "নাগরিকের জন্ম নিবন্ধন সংক্রান্ত কাগজপত্র ও সনদের ছবি স্ক্যান করার জন্য ক্যামেরা ব্যবহারের অনুমতি প্রয়োজন। অনুগ্রহ করে পারমিশন দিন অথবা গ্যালারি থেকে ছবি নির্বাচন করুন।",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(onClick = {
                    showCameraPermissionDialog = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("অনুমতি দিন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
