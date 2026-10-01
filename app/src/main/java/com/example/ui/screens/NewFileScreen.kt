package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Citizen
import com.example.ui.viewmodel.CitizenViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFileScreen(
    viewModel: CitizenViewModel,
    onBack: () -> Unit,
    onCreatedGoToScanner: (citizenId: Long, firstDoc: com.example.data.model.DocumentItem?) -> Unit,
    onCreatedGoToDetails: (citizenId: Long) -> Unit
) {
    val todayFormatted = remember { SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()) }

    var fullName by remember { mutableStateOf("") }
    var fatherMotherName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var wardNo by remember { mutableStateOf("০১") }
    var village by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("জন্ম নিবন্ধন (Birth Registration)") }
    var applicationDate by remember { mutableStateOf(todayFormatted) }
    var remarks by remember { mutableStateOf("") }
    var nextFileIdPreview by remember { mutableStateOf("লোড হচ্ছে...") }
    var serviceDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(serviceType) {
        nextFileIdPreview = viewModel.getNextFileIdPreview(serviceType)
    }

    val wards = listOf("০১", "০২", "০৩", "০৪", "০৫", "০৬", "০৭", "০৮", "০৯")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "নতুন নাগরিক ফাইল তৈরি",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
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
            // Auto File ID Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "স্বয়ংক্রিয় ফাইল আইডি (File ID)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = nextFileIdPreview,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "অটো ইউনিক সিরিয়াল",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Form Title
            item {
                Text(
                    text = "নাগরিকের ব্যক্তিগত তথ্য",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Full Name & Mobile
            item {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("পূর্ণ নাম (নাগরিকের নাম) *") },
                    placeholder = { Text("উদা: মোঃ রহিম উল্লাহ") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_full_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Father/Mother Name
            item {
                OutlinedTextField(
                    value = fatherMotherName,
                    onValueChange = { fatherMotherName = it },
                    label = { Text("পিতা / মাতার নাম *") },
                    placeholder = { Text("পিতা: মোঃ করিম, মাতা: রহিমা বেগম") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_parents_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // DOB and Mobile in Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        label = { Text("জন্মতারিখ") },
                        placeholder = { Text("DD-MM-YYYY") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_dob"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("মোবাইল নম্বর *") },
                        placeholder = { Text("01XXXXXXXXX") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_mobile"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Ward selection chips
            item {
                Column {
                    Text(
                        text = "ওয়ার্ড নম্বর নির্বাচন করুন:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(wards) { ward ->
                            FilterChip(
                                selected = wardNo == ward,
                                onClick = { wardNo = ward },
                                label = { Text("ওয়ার্ড $ward") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Village & Address
            item {
                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text("গ্রামের নাম") },
                    placeholder = { Text("উদা: রামপুর / দক্ষিণ পাড়া") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_village"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("পূর্ণ ঠিকানা / হোল্ডিং নম্বর") },
                    placeholder = { Text("বাড়ি নং, রাস্তা, ডাকঘর") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_address"),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Service Type Selector
            item {
                ExposedDropdownMenuBox(
                    expanded = serviceDropdownExpanded,
                    onExpandedChange = { serviceDropdownExpanded = !serviceDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = serviceType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("কাজের ধরন (Service Type)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = serviceDropdownExpanded,
                        onDismissRequest = { serviceDropdownExpanded = false }
                    ) {
                        Citizen.SERVICE_TYPES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    serviceType = option
                                    serviceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Remarks
            item {
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("মন্তব্য / বিশেষ নোট (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: জরুরি পাসপোর্ট আবেদন, পিতা প্রবাসে ইত্যাদি") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_remarks"),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Primary Action: Save & Open Camera directly for Quick Workflow (Requirement #21)
                    Button(
                        onClick = {
                            viewModel.createNewCitizen(
                                fullName = fullName,
                                fatherMotherName = fatherMotherName,
                                dateOfBirth = dateOfBirth,
                                mobileNumber = mobileNumber,
                                address = address,
                                wardNo = wardNo,
                                village = village,
                                serviceType = serviceType,
                                applicationDate = applicationDate,
                                remarks = remarks,
                                onSuccess = { newCitizenId, firstDoc ->
                                    onCreatedGoToScanner(newCitizenId, firstDoc)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_and_scan"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সংরক্ষণ ও প্রথম কাগজের ছবি তুলুন 📷",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Secondary Action: Save & View Profile
                    Button(
                        onClick = {
                            viewModel.createNewCitizen(
                                fullName = fullName,
                                fatherMotherName = fatherMotherName,
                                dateOfBirth = dateOfBirth,
                                mobileNumber = mobileNumber,
                                address = address,
                                wardNo = wardNo,
                                village = village,
                                serviceType = serviceType,
                                applicationDate = applicationDate,
                                remarks = remarks,
                                onSuccess = { newCitizenId, _ ->
                                    onCreatedGoToDetails(newCitizenId)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_save_and_view"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "শুধু ফাইল সংরক্ষণ করুন (Save)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
