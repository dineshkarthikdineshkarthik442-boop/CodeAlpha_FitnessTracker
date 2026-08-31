package com.lordrimaru.codealphafitnesstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lordrimaru.codealphafitnesstracker.ui.*
import com.lordrimaru.codealphafitnesstracker.ui.theme.CodeAlphaFitnessTrackerTheme
import com.lordrimaru.codealphafitnesstracker.viewmodel.FitnessViewModel
import com.lordrimaru.codealphafitnesstracker.viewmodel.FitnessViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: FitnessViewModel by viewModels {
        FitnessViewModelFactory((application as FitnessApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by viewModel.userPreferences.collectAsState()
            
            CodeAlphaFitnessTrackerTheme(theme = prefs.theme) {
                FitnessTrackerApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessTrackerApp(viewModel: FitnessViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val mainScreens = listOf(
        Screen.Home,
        Screen.Progress,
        Screen.History,
        Screen.Settings
    )

    Scaffold(
        topBar = {
            if (currentDestination?.route != Screen.Achievements.route) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "CodeAlpha Fit",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Achievements") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentDestination?.route in mainScreens.map { it.route }) {
                NavigationBar {
                    mainScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.route == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeView(viewModel)
            }
            composable(Screen.Progress.route) {
                ProgressView(viewModel, navController)
            }
            composable(Screen.History.route) {
                HistoryView(viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsView(viewModel)
            }
            composable(Screen.Achievements.route) {
                AchievementsView(viewModel)
            }
        }
    }
}
