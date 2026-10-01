package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Citizen
import com.example.data.model.DocumentWithPages

@Composable
fun PrintableReportDialog(
    upName: String,
    entrepreneurName: String,
    upazilaDistrict: String,
    title: String,
    citizens: List<Citizen>,
    documentsForSingle: List<DocumentWithPages>? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val reportText = buildString {
        appendLine("=========================================")
        appendLine(upName)
        appendLine(upazilaDistrict)
        appendLine("জন্ম নিবন্ধন ও নাগরিক সেবা ফাইল রিপোর্ট")
        appendLine("=========================================")
        appendLine("বিষয়: $title")
        appendLine("রিপোর্ট প্রস্তুতকারী: $entrepreneurName")
        appendLine("তারিখ: ${java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.US).format(java.util.Date())}")
        appendLine("মোট ফাইলের সংখ্যা: ${citizens.size}")
        appendLine("-----------------------------------------")

        citizens.forEachIndexed { index, c ->
            appendLine("${index + 1}. ফাইল আইডি: ${c.fileId}")
            appendLine("   নাগরিকের নাম: ${c.fullName}")
            appendLine("   পিতা/মাতার নাম: ${c.fatherMotherName}")
            appendLine("   মোবাইল: ${c.mobileNumber}")
            appendLine("   ওয়ার্ড: ${c.wardNo}, গ্রাম: ${c.village}")
            appendLine("   কাজের ধরন: ${c.serviceType}")
            appendLine("   আবেদনের তারিখ: ${c.applicationDate}")
            appendLine("   স্ট্যাটাস: ${c.status}")
            if (c.remarks.isNotBlank()) {
                appendLine("   মন্তব্য: ${c.remarks}")
            }
            appendLine("-----------------------------------------")
        }

        if (documentsForSingle != null && documentsForSingle.isNotEmpty()) {
            appendLine("কাগজপত্রের বিবরণ:")
            documentsForSingle.forEach { doc ->
                val status = if (doc.documentItem.isUploaded) "✓ সংগৃহীত (${doc.pages.size} পৃষ্ঠা)" else "○ বাকি রয়েছে"
                appendLine("- ${doc.documentItem.documentType}: $status")
            }
            appendLine("-----------------------------------------")
        }
        appendLine("সচিবের স্বাক্ষর: _________________")
        appendLine("চেয়ারম্যানের স্বাক্ষর: ______________")
        appendLine("=========================================")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মুদ্রণযোগ্য রিপোর্ট / PDF সামারি",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Report Preview Box
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(380.dp)
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = reportText,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF212121),
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "রিপোর্ট পাঠান বা প্রিন্ট করুন"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("শেয়ার করুন")
                    }

                    Button(
                        onClick = {
                            val html = PrintReportUtil.generateHtmlReport(
                                upName = upName,
                                entrepreneurName = entrepreneurName,
                                upazilaDistrict = upazilaDistrict,
                                title = title,
                                citizens = citizens,
                                documentsForSingle = documentsForSingle
                            )
                            PrintReportUtil.printDocument(context, title, html)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("প্রিন্ট / PDF")
                    }
                }
            }
        }
    }
}
