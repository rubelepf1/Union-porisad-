package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DocumentItem
import com.example.ui.screens.CitizenDetailsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentScannerScreen
import com.example.ui.screens.DriveSyncScreen
import com.example.ui.screens.FileListScreen
import com.example.ui.screens.NewFileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubmissionBatchScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CitizenViewModel

sealed class AppScreen {
    object Dashboard : AppScreen()
    object FileList : AppScreen()
    object NewFile : AppScreen()
    data class CitizenDetails(val citizenId: Long) : AppScreen()
    data class Scanner(val documentItem: DocumentItem) : AppScreen()
    object SubmissionBatch : AppScreen()
    object DriveSync : AppScreen()
    object Settings : AppScreen()
}

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: AppScreen,
    val tag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: CitizenViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }
    var screenStack by remember { mutableStateOf(listOf<AppScreen>(AppScreen.Dashboard)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
        screenStack = screenStack + screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            val newStack = screenStack.dropLast(1)
            screenStack = newStack
            currentScreen = newStack.last()
        } else {
            currentScreen = AppScreen.Dashboard
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem("হোম", Icons.Filled.Home, Icons.Outlined.Home, AppScreen.Dashboard, "nav_home"),
        BottomNavItem("ফাইলসমূহ", Icons.Filled.Folder, Icons.Outlined.Folder, AppScreen.FileList, "nav_files"),
        BottomNavItem("নতুন", Icons.Filled.AddCircle, Icons.Filled.AddCircle, AppScreen.NewFile, "nav_new"),
        BottomNavItem("সাবমিশন", Icons.Filled.Assignment, Icons.Outlined.Assignment, AppScreen.SubmissionBatch, "nav_batch"),
        BottomNavItem("সেটিংস", Icons.Filled.Settings, Icons.Outlined.Settings, AppScreen.Settings, "nav_settings")
    )

    val showBottomBar = currentScreen is AppScreen.Dashboard ||
        currentScreen is AppScreen.FileList ||
        currentScreen is AppScreen.SubmissionBatch ||
        currentScreen is AppScreen.Settings

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = when (item.screen) {
                            AppScreen.Dashboard -> currentScreen is AppScreen.Dashboard
                            AppScreen.FileList -> currentScreen is AppScreen.FileList
                            AppScreen.NewFile -> currentScreen is AppScreen.NewFile
                            AppScreen.SubmissionBatch -> currentScreen is AppScreen.SubmissionBatch
                            AppScreen.Settings -> currentScreen is AppScreen.Settings
                            else -> false
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navigateTo(item.screen)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(item.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        BackHandler(enabled = screenStack.size > 1) {
            navigateBack()
        }

        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            when (val screen = currentScreen) {
                is AppScreen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToNewFile = { navigateTo(AppScreen.NewFile) },
                        onNavigateToFiles = { filter ->
                            filter?.let { viewModel.onStatusFilterSelected(it) }
                            navigateTo(AppScreen.FileList)
                        },
                        onNavigateToDrive = { navigateTo(AppScreen.DriveSync) },
                        onOpenCitizen = { citizenId ->
                            viewModel.selectCitizen(citizenId)
                            navigateTo(AppScreen.CitizenDetails(citizenId))
                        }
                    )
                }

                is AppScreen.FileList -> {
                    FileListScreen(
                        viewModel = viewModel,
                        onNavigateToNewFile = { navigateTo(AppScreen.NewFile) },
                        onOpenCitizen = { citizenId ->
                            viewModel.selectCitizen(citizenId)
                            navigateTo(AppScreen.CitizenDetails(citizenId))
                        }
                    )
                }

                is AppScreen.NewFile -> {
                    NewFileScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() },
                        onCreatedGoToScanner = { citizenId, firstDoc ->
                            // Directly start scanning first document without race condition
                            if (firstDoc != null) {
                                viewModel.startScanning(firstDoc)
                                navigateTo(AppScreen.Scanner(firstDoc))
                            } else {
                                navigateTo(AppScreen.CitizenDetails(citizenId))
                            }
                        },
                        onCreatedGoToDetails = { citizenId ->
                            navigateTo(AppScreen.CitizenDetails(citizenId))
                        }
                    )
                }

                is AppScreen.CitizenDetails -> {
                    CitizenDetailsScreen(
                        viewModel = viewModel,
                        citizenId = screen.citizenId,
                        onBack = { navigateBack() },
                        onNavigateToScan = { docItem ->
                            navigateTo(AppScreen.Scanner(docItem))
                        }
                    )
                }

                is AppScreen.Scanner -> {
                    DocumentScannerScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() },
                        onSavedSuccessfully = {
                            navigateBack()
                        }
                    )
                }

                is AppScreen.SubmissionBatch -> {
                    SubmissionBatchScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() }
                    )
                }

                is AppScreen.DriveSync -> {
                    DriveSyncScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() }
                    )
                }

                is AppScreen.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { navigateBack() }
                    )
                }
            }
        }
    }
}
