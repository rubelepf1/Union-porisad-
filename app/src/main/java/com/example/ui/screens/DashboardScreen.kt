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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Citizen
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.GreenSecondary
import com.example.ui.viewmodel.CitizenViewModel

@Composable
fun DashboardScreen(
    viewModel: CitizenViewModel,
    onNavigateToNewFile: () -> Unit,
    onNavigateToFiles: (statusFilter: String?) -> Unit,
    onNavigateToDrive: () -> Unit,
    onOpenCitizen: (Long) -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val citizens by viewModel.filteredCitizens.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ইউনিয়ন পরিষদ ফাইল ম্যানেজমেন্ট",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${settings.upName} | ${settings.entrepreneurName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        // Drive Connection Indicator
                        Surface(
                            shape = CircleShape,
                            color = if (settings.isDriveConnected) Color(0xFF2E7D32) else Color(0xFF757575),
                            modifier = Modifier
                                .clickable { onNavigateToDrive() }
                                .padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (settings.isDriveConnected) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                    contentDescription = "Drive Status",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (settings.isDriveConnected) "Drive Ok" else "অফলাইন",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (pendingCount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFECB3))
                                .clickable { onNavigateToDrive() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$pendingCount টি ফাইল ড্রাইভে আপলোডের অপেক্ষায় আছে (Pending Upload)",
                                color = Color(0xFFE65100),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_input"),
                placeholder = {
                    Text(
                        text = "নাম, মোবাইল বা File ID দিয়ে খুঁজুন",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Text("✕", fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        // Prominent 6 Big Cards
        item {
            Text(
                text = "প্রধান সেবাসমূহ (Quick Actions)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: New File & All Files
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardActionCard(
                        title = "নতুন ফাইল",
                        subtitle = "নাগরিক নিবন্ধন শুরু",
                        count = null,
                        icon = Icons.Default.Add,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_new_file"),
                        onClick = onNavigateToNewFile
                    )

                    DashboardActionCard(
                        title = "সকল ফাইল",
                        subtitle = "মোট সংরক্ষিত ফাইল",
                        count = stats.totalFiles.toString(),
                        icon = Icons.Default.Folder,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_all_files"),
                        onClick = { onNavigateToFiles("সকল ফাইল") }
                    )
                }

                // Row 2: Today's Files & Incomplete Files
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardActionCard(
                        title = "আজকের ফাইল",
                        subtitle = "আজ এন্ট্রি করা",
                        count = stats.todayFiles.toString(),
                        icon = Icons.Default.Today,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFF0288D1),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_today_files"),
                        onClick = { onNavigateToFiles("আজকের ফাইল") }
                    )

                    DashboardActionCard(
                        title = "অসম্পূর্ণ ফাইল",
                        subtitle = "কাগজপত্র বাকি রয়েছে",
                        count = stats.incompleteFiles.toString(),
                        icon = Icons.Default.Warning,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFFD32F2F),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_incomplete_files"),
                        onClick = { onNavigateToFiles(Citizen.STATUS_INCOMPLETE) }
                    )
                }

                // Row 3: Ready for Secretary & Google Drive
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardActionCard(
                        title = "জমা দেওয়ার জন্য প্রস্তুত",
                        subtitle = "সচিব মহোদয়ের কাছে",
                        count = stats.readySecretaryFiles.toString(),
                        icon = Icons.Default.HowToReg,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFF3949AB),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_secretary_ready"),
                        onClick = { onNavigateToFiles(Citizen.STATUS_READY_SECRETARY) }
                    )

                    DashboardActionCard(
                        title = "Google Drive",
                        subtitle = if (settings.isDriveConnected) "সংযুক্ত ও সিঙ্ক" else "কানেক্ট করুন (অফলাইন)",
                        count = if (settings.isDriveConnected) (if (pendingCount > 0) "$pendingCount পেন্ডিং" else "সিঙ্কড") else "$pendingCount পেন্ডিং",
                        icon = if (settings.isDriveConnected) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = if (settings.isDriveConnected) GreenSecondary else Color(0xFFE65100),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_google_drive"),
                        onClick = onNavigateToDrive
                    )
                }
            }
        }

        // Dashboard Statistics Strip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "সারসংক্ষেপ পরিসংখ্যান (Statistics)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatCounterItem("মোট", stats.totalFiles, MaterialTheme.colorScheme.primary)
                        StatCounterItem("আজকের", stats.todayFiles, Color(0xFF0288D1))
                        StatCounterItem("অসম্পূর্ণ", stats.incompleteFiles, Color(0xFFD32F2F))
                        StatCounterItem("সম্পূর্ণ", stats.completeFiles, Color(0xFF2E7D32))
                        StatCounterItem("উপজেলায়", stats.sentUpazilaFiles, Color(0xFF00796B))
                    }
                }
            }
        }

        // Recent Files / Search Results
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "অনুসন্ধানের ফলাফল (${citizens.size})" else "সাম্প্রতিক ফাইলসমূহ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "সব দেখুন",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateToFiles("সকল ফাইল") }
                )
            }
        }

        if (citizens.isEmpty()) {
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
                        Text(
                            text = "কোনো ফাইল পাওয়া যায়নি",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToNewFile,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("নতুন ফাইল তৈরি করুন")
                        }
                    }
                }
            }
        } else {
            items(citizens.take(5)) { citizen ->
                CitizenCardItem(
                    citizen = citizen,
                    onOpen = { onOpenCitizen(citizen.id) }
                )
            }
        }
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    subtitle: String,
    count: String?,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = contentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (count != null) {
                    Text(
                        text = count,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = contentColor
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = contentColor,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StatCounterItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun CitizenCardItem(
    citizen: Citizen,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("citizen_card_${citizen.fileId}")
            .clickable { onOpen() },
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = citizen.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ফাইল আইডি: ${citizen.fileId}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                StatusBadge(status = citizen.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "মোবাইল: ${citizen.mobileNumber}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "ওয়ার্ড: ${citizen.wardNo} | গ্রাম: ${citizen.village}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = onOpen,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ফাইল দেখুন",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
