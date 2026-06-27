package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SettingsProfile
import com.example.ui.GamepadViewModel

@Composable
fun SettingsScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    // Temporary variables for text field edits
    var ipText by remember(settings.targetIp) { mutableStateOf(settings.targetIp) }
    var portText by remember(settings.targetPort) { mutableStateOf(settings.targetPort.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title Block matching Design HTML styling
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LinkPad ",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SETTINGS",
                        color = Color(0xFF6366F1),
                        fontSize = 14.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Top)
                    )
                }
                Text(
                    text = "CALIBRATION & CONNECTION CONTROL",
                    color = Color(0xFF71717A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 1. Connection Mode Selector Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF27272A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Connection Interface",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WiFi UDP Selector Button
                    Button(
                        onClick = {
                            viewModel.saveSettingsProfile(settings.copy(connectionType = "WIFI_UDP"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settings.connectionType == "WIFI_UDP") Color(0xFF6366F1) else Color(0x1F6366F1),
                            contentColor = if (settings.connectionType == "WIFI_UDP") Color.White else Color(0xFF818CF8)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(
                            "WiFi UDP (Fastest)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bluetooth HID Selector Button
                    Button(
                        onClick = {
                            viewModel.saveSettingsProfile(settings.copy(connectionType = "BLUETOOTH_HID"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (settings.connectionType == "BLUETOOTH_HID") Color(0xFF6366F1) else Color(0x1F6366F1),
                            contentColor = if (settings.connectionType == "BLUETOOTH_HID") Color.White else Color(0xFF818CF8)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(
                            "Bluetooth HID",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If WIFI UDP, show IP & Port configuration inputs
                if (settings.connectionType == "WIFI_UDP") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = ipText,
                            onValueChange = {
                                ipText = it
                                viewModel.saveSettingsProfile(settings.copy(targetIp = it))
                            },
                            label = { Text("PC IP Address", color = Color(0xFF71717A)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color(0xFF27272A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )

                        OutlinedTextField(
                            value = portText,
                            onValueChange = {
                                portText = it
                                val p = it.toIntOrNull() ?: 5001
                                viewModel.saveSettingsProfile(settings.copy(targetPort = p))
                            },
                            label = { Text("Port", color = Color(0xFF71717A)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = Color(0xFF27272A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                } else {
                    Text(
                        "Bluetooth HID mode lets Windows or macOS see your device as a native gamepad. Bond your computer via Android Bluetooth settings.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Sensitivity & Calibration Sliders Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF27272A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    text = "Calibration Settings",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                // Joystick Sensitivity
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Joystick Sensitivity", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(String.format("%.1fx", settings.joystickSensitivity), color = Color(0xFF818CF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.joystickSensitivity,
                        onValueChange = {
                            viewModel.saveSettingsProfile(settings.copy(joystickSensitivity = it))
                        },
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF818CF8),
                            activeTrackColor = Color(0xFF6366F1),
                            inactiveTrackColor = Color(0xFF27272A)
                        )
                    )
                }

                // Button Touch Sensitivity
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Button Press Sensitivity", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(String.format("%.1fx", settings.buttonSensitivity), color = Color(0xFF818CF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.buttonSensitivity,
                        onValueChange = {
                            viewModel.saveSettingsProfile(settings.copy(buttonSensitivity = it))
                        },
                        valueRange = 0.5f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF818CF8),
                            activeTrackColor = Color(0xFF6366F1),
                            inactiveTrackColor = Color(0xFF27272A)
                        )
                    )
                }

                // Joystick Deadzone
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Joystick Deadzone (drift filter)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(String.format("%.2f", settings.deadzone), color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.deadzone,
                        onValueChange = {
                            viewModel.saveSettingsProfile(settings.copy(deadzone = it))
                        },
                        valueRange = 0.05f..0.35f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFF59E0B),
                            activeTrackColor = Color(0xFFD97706),
                            inactiveTrackColor = Color(0xFF27272A)
                        )
                    )
                }

                // Rumble/Tactile Haptic Intensity
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vibration intensity (tactile feedback)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(String.format("%.0f%%", settings.vibrationIntensity * 100), color = Color(0xFF818CF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.vibrationIntensity,
                        onValueChange = {
                            viewModel.saveSettingsProfile(settings.copy(vibrationIntensity = it))
                        },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF818CF8),
                            activeTrackColor = Color(0xFF6366F1),
                            inactiveTrackColor = Color(0xFF27272A)
                        )
                    )
                }
            }
        }

        // 3. Emulation Profile Selector Card (Xbox vs PS4)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF27272A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Emulation Profile",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf("XBOX_360", "PLAYSTATION_4")
                    modes.forEach { mode ->
                        Button(
                            onClick = {
                                viewModel.saveSettingsProfile(settings.copy(emulationMode = mode))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.emulationMode == mode) Color(0xFF6366F1) else Color(0x1F6366F1),
                                contentColor = if (settings.emulationMode == mode) Color.White else Color(0xFF818CF8)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                text = if (mode == "XBOX_360") "Xbox 360 (Default)" else "PlayStation 4",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = "Emulating Xbox 360 ensures 100% compatibility with all PC games via XInput API natively without extra keymapper mapping configurations.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
