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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubmissionBatch
import com.example.ui.components.PrintableReportDialog
import com.example.ui.viewmodel.CitizenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionBatchScreen(
    viewModel: CitizenViewModel,
    onBack: () -> Unit
) {
    val batches by viewModel.submissionBatches.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val allCitizens by viewModel.filteredCitizens.collectAsState()

    var selectedBatchForPrint by remember { mutableStateOf<SubmissionBatch?>(null) }
    var selectedBatchForStatusEdit by remember { mutableStateOf<SubmissionBatch?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "সচিব জমাদান ব্যাচ ও রিপোর্ট",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সচিব মহোদয়ের জমাদান তালিকা (Submission Batches)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "সম্পূর্ণ ফাইলসমূহ একত্রিত করে ব্যাচ তৈরি করুন এবং সচিব বা উপজেলা অফিসে জমা দেওয়ার জন্য প্রিন্ট/PDF রিপোর্ট তৈরি করুন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            if (batches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "এখনও কোনো জমাদান ব্যাচ তৈরি করা হয়নি। ফাইল তালিকা থেকে ব্যাচ নির্বাচন করুন।",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(batches) { batch ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                        text = "ব্যাচ আইডি: ${batch.batchId}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "মোট ফাইল: ${batch.totalFiles} টি | তারিখ: ${batch.submissionDate}",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        batch.status.contains("Completed") -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                        batch.status.contains("Upazila") -> Color(0xFF00796B).copy(alpha = 0.15f)
                                        batch.status.contains("Submitted") -> Color(0xFF3949AB).copy(alpha = 0.15f)
                                        else -> Color(0xFFE65100).copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = batch.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            batch.status.contains("Completed") -> Color(0xFF2E7D32)
                                            batch.status.contains("Upazila") -> Color(0xFF00796B)
                                            batch.status.contains("Submitted") -> Color(0xFF3949AB)
                                            else -> Color(0xFFE65100)
                                        }
                                    )
                                }
                            }

                            if (batch.remarks.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "নোট: ${batch.remarks}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { selectedBatchForPrint = batch },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("প্রিন্ট / রিপোর্ট", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { selectedBatchForStatusEdit = batch },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ধাপ পরিবর্তন", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Print Batch Report Dialog
    if (selectedBatchForPrint != null) {
        val b = selectedBatchForPrint!!
        val batchCitizens = allCitizens.filter { it.submissionBatchId == b.batchId }
        val citizensToShow = if (batchCitizens.isNotEmpty()) batchCitizens else allCitizens.take(b.totalFiles.coerceAtLeast(1))

        PrintableReportDialog(
            upName = settings.upName,
            entrepreneurName = settings.entrepreneurName,
            upazilaDistrict = settings.upazilaDistrict,
            title = "সচিব মহোদয়ের কাছে জমাদান ব্যাচ বিবরণী (${b.batchId})",
            citizens = citizensToShow,
            onDismiss = { selectedBatchForPrint = null }
        )
    }

    // Status Edit Dialog
    if (selectedBatchForStatusEdit != null) {
        val b = selectedBatchForStatusEdit!!
        var currentStatus by remember { mutableStateOf(b.status) }

        AlertDialog(
            onDismissRequest = { selectedBatchForStatusEdit = null },
            title = { Text("ব্যাচের স্ট্যাটাস পরিবর্তন") },
            text = {
                Column {
                    SubmissionBatch.ALL_BATCH_STATUSES.forEach { st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentStatus = st }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentStatus == st,
                                onClick = { currentStatus = st }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(st, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Update
                    selectedBatchForStatusEdit = null
                }) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedBatchForStatusEdit = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
