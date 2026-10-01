package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.components.PrintableReportDialog
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.CitizenViewModel

@Composable
fun FileListScreen(
    viewModel: CitizenViewModel,
    onNavigateToNewFile: () -> Unit,
    onOpenCitizen: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val selectedWard by viewModel.selectedWardFilter.collectAsState()
    val citizens by viewModel.filteredCitizens.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var isBatchSelectionMode by remember { mutableStateOf(false) }
    val selectedCitizenIds = remember { mutableStateListOf<Long>() }
    var showBatchDialog by remember { mutableStateOf(false) }
    var showPrintReportDialog by remember { mutableStateOf(false) }
    var batchRemarks by remember { mutableStateOf("") }

    val statusFilters = listOf(
        "সকল ফাইল",
        "আজকের ফাইল",
        Citizen.STATUS_INCOMPLETE,
        Citizen.STATUS_COLLECTING,
        Citizen.STATUS_COMPLETE,
        Citizen.STATUS_READY_SECRETARY,
        Citizen.STATUS_SENT_UPAZILA,
        Citizen.STATUS_DONE
    )

    val wardFilters = listOf("সকল ওয়ার্ড", "০১", "০২", "০৩", "০৪", "০৫", "০৬", "০৭", "০৮", "০৯")

    Scaffold(
        floatingActionButton = {
            if (!isBatchSelectionMode) {
                FloatingActionButton(
                    onClick = onNavigateToNewFile,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_new_file")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New File")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Header & Batch Action Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isBatchSelectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                isBatchSelectionMode = false
                                selectedCitizenIds.clear()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel")
                            }
                            Text(
                                text = "${selectedCitizenIds.size} টি ফাইল নির্বাচিত",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Button(
                            onClick = {
                                if (selectedCitizenIds.isNotEmpty()) {
                                    showBatchDialog = true
                                }
                            },
                            enabled = selectedCitizenIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("সাবমিশন তৈরি")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "নাগরিক ফাইল তালিকা",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "মোট প্রাপ্ত ফলাফল: ${citizens.size} টি",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Row {
                            IconButton(onClick = { showPrintReportDialog = true }) {
                                Icon(
                                    Icons.Default.Print,
                                    contentDescription = "Print/Report",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = { isBatchSelectionMode = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ব্যাচ নির্বাচন",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("file_list_search_input"),
                    placeholder = {
                        Text(
                            text = "নাম, মোবাইল বা File ID দিয়ে খুঁজুন...",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
            }

            // Filter Chips Horizontal Row: Status
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusFilters) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { viewModel.onStatusFilterSelected(status) },
                        label = { Text(status, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Ward Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(wardFilters) { ward ->
                    FilterChip(
                        selected = selectedWard == ward,
                        onClick = { viewModel.onWardFilterSelected(ward) },
                        label = { Text(if (ward == "সকল ওয়ার্ড") "সকল ওয়ার্ড" else "ওয়ার্ড $ward", fontSize = 11.sp) }
                    )
                }
            }

            // Citizen Cards List
            if (citizens.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "কোনো ফাইল পাওয়া যায়নি",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "সার্চ ফিল্টার পরিবর্তন করুন অথবা নতুন ফাইল এন্ট্রি করুন।",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(citizens, key = { it.id }) { citizen ->
                        val isSelected = selectedCitizenIds.contains(citizen.id)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isBatchSelectionMode) {
                                        if (isSelected) {
                                            selectedCitizenIds.remove(citizen.id)
                                        } else {
                                            selectedCitizenIds.add(citizen.id)
                                        }
                                    } else {
                                        onOpenCitizen(citizen.id)
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isBatchSelectionMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked == true) {
                                                selectedCitizenIds.add(citizen.id)
                                            } else {
                                                selectedCitizenIds.remove(citizen.id)
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = citizen.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        StatusBadge(status = citizen.status)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "ফাইল আইডি: ${citizen.fileId}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "ওয়ার্ড: ${citizen.wardNo}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "মোবাইল: ${citizen.mobileNumber}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )

                                        if (!isBatchSelectionMode) {
                                            Button(
                                                onClick = { onOpenCitizen(citizen.id) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Open File",
                                                    fontSize = 12.sp,
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
                }
            }
        }
    }

    // Batch Creation Dialog
    if (showBatchDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            title = { Text("সচিবের জমাদান ব্যাচ তৈরি") },
            text = {
                Column {
                    Text("নির্বাচিত ফাইলের সংখ্যা: ${selectedCitizenIds.size} টি")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = batchRemarks,
                        onValueChange = { batchRemarks = it },
                        label = { Text("ব্যাচের বিবরণ / নোট") },
                        placeholder = { Text("উদা: অক্টোবর ১ম সপ্তাহের জমা") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createSubmissionBatch(
                            citizenIds = selectedCitizenIds.toList(),
                            remarks = batchRemarks,
                            onComplete = {
                                showBatchDialog = false
                                isBatchSelectionMode = false
                                selectedCitizenIds.clear()
                            }
                        )
                    }
                ) {
                    Text("তৈরি করুন")
                }
            },
            dismissButton = {
                Button(onClick = { showBatchDialog = false }, colors = ButtonDefaults.outlinedButtonColors()) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Printable Report Dialog
    if (showPrintReportDialog) {
        PrintableReportDialog(
            upName = settings.upName,
            entrepreneurName = settings.entrepreneurName,
            upazilaDistrict = settings.upazilaDistrict,
            title = "নাগরিক জন্ম নিবন্ধন ফাইল সারাংশ ($selectedStatus)",
            citizens = citizens,
            onDismiss = { showPrintReportDialog = false }
        )
    }
}
