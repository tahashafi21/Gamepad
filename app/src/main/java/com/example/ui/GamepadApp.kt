package com.example.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ConnectionScreen
import com.example.ui.screens.ControllerScreen
import com.example.ui.screens.SettingsScreen

@Composable
fun GamepadApp(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Custom premium dark background for modern console dashboard look
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0A0A0A)
    ) {
        if (isLandscape) {
            // Landscape Layout: Navigation Rail on Left, content on Right
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = Color(0xFF121214),
                    header = {
                        Text(
                            text = "🕹️",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    },
                    modifier = Modifier.width(72.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Controller Tab
                    NavigationRailItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.Gamepad else Icons.Outlined.Gamepad,
                                contentDescription = "Controller"
                            )
                        },
                        label = { Text("Gamepad", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF818CF8),
                            unselectedIconColor = Color(0xFF71717A),
                            selectedTextColor = Color(0xFF818CF8),
                            unselectedTextColor = Color(0xFF71717A),
                            indicatorColor = Color(0x226366F1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Settings Tab
                    NavigationRailItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF818CF8),
                            unselectedIconColor = Color(0xFF71717A),
                            selectedTextColor = Color(0xFF818CF8),
                            unselectedTextColor = Color(0xFF71717A),
                            indicatorColor = Color(0x226366F1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Telemetry / Setup Tab
                    NavigationRailItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Filled.Info else Icons.Outlined.Info,
                                contentDescription = "Telemetry"
                            )
                        },
                        label = { Text("Setup", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF818CF8),
                            unselectedIconColor = Color(0xFF71717A),
                            selectedTextColor = Color(0xFF818CF8),
                            unselectedTextColor = Color(0xFF71717A),
                            indicatorColor = Color(0x226366F1)
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }

                // Active Tab Content Pane
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (selectedTab) {
                        0 -> ControllerScreen(viewModel = viewModel)
                        1 -> SettingsScreen(viewModel = viewModel)
                        2 -> ConnectionScreen(viewModel = viewModel)
                    }
                }
            }
        } else {
            // Portrait Layout: Standard Bottom Navigation Bar
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = Color(0xFF121214),
                        tonalElevation = 8.dp
                    ) {
                        // Controller Tab
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Gamepad else Icons.Outlined.Gamepad,
                                    contentDescription = "Controller"
                                )
                            },
                            label = { Text("Gamepad", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF818CF8),
                                unselectedIconColor = Color(0xFF71717A),
                                selectedTextColor = Color(0xFF818CF8),
                                unselectedTextColor = Color(0xFF71717A),
                                indicatorColor = Color(0x226366F1)
                            )
                        )

                        // Settings Tab
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF818CF8),
                                unselectedIconColor = Color(0xFF71717A),
                                selectedTextColor = Color(0xFF818CF8),
                                unselectedTextColor = Color(0xFF71717A),
                                indicatorColor = Color(0x226366F1)
                            )
                        )

                        // Companion Setup Tab
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Filled.Info else Icons.Outlined.Info,
                                    contentDescription = "Telemetry"
                                )
                            },
                            label = { Text("Setup", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF818CF8),
                                unselectedIconColor = Color(0xFF71717A),
                                selectedTextColor = Color(0xFF818CF8),
                                unselectedTextColor = Color(0xFF71717A),
                                indicatorColor = Color(0x226366F1)
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        0 -> ControllerScreen(viewModel = viewModel)
                        1 -> SettingsScreen(viewModel = viewModel)
                        2 -> ConnectionScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
