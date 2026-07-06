package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AnalyticsChartsScreen
import com.example.ui.screens.AppUsageScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.AddEditContactScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ScreenPulseViewModel

enum class ScreenRoute(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("dashboard", "Kişiler", Icons.Filled.Person, Icons.Outlined.Person),
    ARAMALAR("aramalar", "Aramalar", Icons.Filled.Phone, Icons.Outlined.Phone),
    FAVORILER("favoriler", "Favoriler", Icons.Filled.Star, Icons.Outlined.StarBorder),
    AYARLAR("ayarlar", "Ayarlar", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ScreenPulseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ScreenPulseApplication
        val factory = ScreenPulseViewModel.Factory(app.repository, app.settingsManager)
        val vm by viewModels<ScreenPulseViewModel> { factory }
        viewModel = vm

        setContent {
            MyApplicationTheme {
                MainContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContainer(viewModel: ScreenPulseViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainRoutes = listOf(
        ScreenRoute.DASHBOARD.route,
        ScreenRoute.ARAMALAR.route,
        ScreenRoute.FAVORILER.route,
        ScreenRoute.AYARLAR.route
    )

    val showBottomBar = currentRoute in mainRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        val screens = listOf(
                            ScreenRoute.DASHBOARD,
                            ScreenRoute.ARAMALAR,
                            ScreenRoute.FAVORILER,
                            ScreenRoute.AYARLAR
                        )
                        screens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.1.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.DASHBOARD.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            composable(ScreenRoute.DASHBOARD.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onContactClick = { contactId ->
                        navController.navigate("details/$contactId")
                    },
                    onAddContactClick = {
                        navController.navigate("add")
                    }
                )
            }
            composable(ScreenRoute.ARAMALAR.route) {
                AppUsageScreen(
                    viewModel = viewModel,
                    onContactClick = { contactId ->
                        navController.navigate("details/$contactId")
                    }
                )
            }
            composable(ScreenRoute.FAVORILER.route) {
                AnalyticsChartsScreen(
                    viewModel = viewModel,
                    onContactClick = { contactId ->
                        navController.navigate("details/$contactId")
                    }
                )
            }
            composable(ScreenRoute.AYARLAR.route) {
                SettingsScreen()
            }
            composable("details/{contactId}") { backStackEntry ->
                val contactIdStr = backStackEntry.arguments?.getString("contactId")
                val contactId = contactIdStr?.toLongOrNull() ?: 0L
                ContactDetailScreen(
                    contactId = contactId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id ->
                        navController.navigate("edit/$id")
                    }
                )
            }
            composable("add") {
                AddEditContactScreen(
                    contactId = null,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("edit/{contactId}") { backStackEntry ->
                val contactIdStr = backStackEntry.arguments?.getString("contactId")
                val contactId = contactIdStr?.toLongOrNull()
                AddEditContactScreen(
                    contactId = contactId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
