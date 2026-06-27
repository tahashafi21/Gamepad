package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.ControllerButton
import com.example.ui.GamepadViewModel
import com.example.ui.components.DpadView
import com.example.ui.components.JoystickView

@Composable
fun ControllerScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val buttons by viewModel.buttons.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val selectedButton by viewModel.selectedButtonForEdit.collectAsState()
    val transmissionActive by viewModel.transmissionActive.collectAsState()
    val bluetoothConnected by viewModel.bluetoothConnected.collectAsState()
    val btDeviceName by viewModel.bluetoothDeviceName.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Elegant Dark Deep Black
    ) {
        // Alignment grid overlay in edit mode
        if (isEditMode) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSpacing = 40.dp.toPx()
                val paintColor = Color(0x156366F1) // Indigo Grid lines
                
                // Vertical lines
                var x = 0f
                while (x < size.width) {
                    drawLine(paintColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += gridSpacing
                }
                
                // Horizontal lines
                var y = 0f
                while (y < size.height) {
                    drawLine(paintColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += gridSpacing
                }
            }
        }

        // Main Controller Canvas area where buttons are laid out dynamically
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val constraintsWidth = maxWidth
            val constraintsHeight = maxHeight

            // Render each configured controller widget dynamically
            buttons.forEach { button ->
                // Map percentages to current screen dp offsets
                val xOffset = (button.xPercent / 100f) * constraintsWidth.value
                val yOffset = (button.yPercent / 100f) * constraintsHeight.value

                Box(
                    modifier = Modifier
                        .offset(x = xOffset.dp, y = yOffset.dp)
                        .size(button.sizeDp.dp)
                        .pointerInput(button.id, isEditMode) {
                            if (isEditMode) {
                                detectDragGestures(
                                    onDragStart = { viewModel.selectButtonForEdit(button) },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        // Calculate delta in percentages
                                        val dxPercent = (dragAmount.x / size.width) * 100f
                                        val dyPercent = (dragAmount.y / size.height) * 100f
                                        
                                        viewModel.updateButtonPosition(
                                            buttonId = button.id,
                                            xPercent = button.xPercent + dxPercent,
                                            yPercent = button.yPercent + dyPercent
                                        )
                                    }
                                )
                            }
                        }
                ) {
                    // Render appropriate widget according to its type
                    when (button.type) {
                        "JOYSTICK" -> {
                            JoystickView(
                                size = button.sizeDp.dp,
                                sensitivity = settings.joystickSensitivity,
                                deadzone = settings.deadzone,
                                modifier = Modifier.border(
                                    width = if (isEditMode && selectedButton?.id == button.id) 2.dp else 0.dp,
                                    color = Color(0xFF818CF8),
                                    shape = CircleShape
                                ),
                                onMove = { lx, ly ->
                                    val isLeft = button.name == "LSTICK"
                                    viewModel.onJoystickMoved(isLeft, lx, ly)
                                }
                            )
                        }
                        "DPAD" -> {
                            DpadView(
                                size = button.sizeDp.dp,
                                modifier = Modifier.border(
                                    width = if (isEditMode && selectedButton?.id == button.id) 2.dp else 0.dp,
                                    color = Color(0xFF818CF8),
                                    shape = CircleShape
                                ),
                                onDirectionChange = { dir ->
                                    viewModel.onDpadChanged(dir)
                                }
                            )
                        }
                        else -> {
                            // Standard Button
                            GamepadPressButton(
                                button = button,
                                isEditMode = isEditMode,
                                isSelectedInEdit = selectedButton?.id == button.id,
                                onSelect = { viewModel.selectButtonForEdit(button) },
                                onPressStateChanged = { pressed ->
                                    viewModel.onButtonPressStateChanged(button.name, pressed)
                                }
                            )
                        }
                    }
                }
            }
        }

        // HUD Dashboard Overlay (Top status & controls)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Connection Status Indicator
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xEE121214)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF27272A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (transmissionActive) Color(0xFF22C55E) else Color(0xFFEF4444),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (settings.connectionType == "BLUETOOTH_HID") {
                            if (bluetoothConnected) "BT: $btDeviceName" else "BT: WAITING..."
                        } else {
                            "WIFI: ${settings.targetIp}:${settings.targetPort}"
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }

            // Right: Design and Layout mode toggle
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode) {
                    Button(
                        onClick = { viewModel.resetLayoutToDefault() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset Layout", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { viewModel.toggleEditMode() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditMode) Color(0xFF6366F1) else Color(0xFF18181B)
                    ),
                    border = if (!isEditMode) BorderStroke(1.dp, Color(0xFF27272A)) else null,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isEditMode) "Save Layout" else "Edit Layout 📐",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom editing context panel (appears when selecting a button in Edit Mode)
        if (isEditMode && selectedButton != null) {
            selectedButton?.let { btn ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF27272A))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Customize Widget: ${btn.name}",
                                color = Color(0xFF818CF8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { viewModel.selectButtonForEdit(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Size modifier slider
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Size: ${btn.sizeDp}dp", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Slider(
                                    value = btn.sizeDp.toFloat(),
                                    onValueChange = { viewModel.updateButtonSize(btn.id, it.toInt()) },
                                    valueRange = 30f..200f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF818CF8),
                                        activeTrackColor = Color(0xFF6366F1),
                                        inactiveTrackColor = Color(0xFF27272A)
                                    )
                                )
                            }

                            // Custom Label text input (only for standard buttons)
                            if (btn.type == "BUTTON") {
                                OutlinedTextField(
                                    value = btn.label,
                                    onValueChange = { viewModel.updateButtonLabel(btn.id, it) },
                                    label = { Text("Label", color = Color.Gray, fontSize = 10.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF818CF8),
                                        unfocusedBorderColor = Color(0xFF27272A)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamepadPressButton(
    button: ControllerButton,
    isEditMode: Boolean,
    isSelectedInEdit: Boolean,
    onSelect: () -> Unit,
    onPressStateChanged: (pressed: Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Interactive button colors
    val defaultBg = when (button.name) {
        "A" -> Color(0xAA10B981) // Green
        "B" -> Color(0xAAEF4444) // Red
        "X" -> Color(0xAA3B82F6) // Blue
        "Y" -> Color(0xAAFBBF24) // Yellow
        "START", "SELECT", "BACK" -> Color(0xAA18181B)
        else -> Color(0xAA18181B) // Bumpers & Triggers
    }

    val activeBg = if (isPressed) {
        when (button.name) {
            "A" -> Color(0xFF10B981)
            "B" -> Color(0xFFEF4444)
            "X" -> Color(0xFF3B82F6)
            "Y" -> Color(0xFFFBBF24)
            else -> Color(0xFF6366F1) // Indigo Active Glow
        }
    } else defaultBg

    val borderStroke = if (isEditMode) {
        BorderStroke(2.dp, if (isSelectedInEdit) Color(0xFF818CF8) else Color.DarkGray)
    } else {
        BorderStroke(1.5.dp, if (isPressed) Color(0xFF818CF8) else Color(0xFF27272A))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isEditMode) {
                if (isEditMode) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                onSelect()
                            }
                        }
                    }
                } else {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                change.consume()
                                val pressed = change.pressed
                                if (isPressed != pressed) {
                                    isPressed = pressed
                                    onPressStateChanged(pressed)
                                }
                            }
                        }
                    }
                }
            }
            .background(brush = Brush.verticalGradient(listOf(activeBg, activeBg.copy(alpha = 0.5f))), shape = CircleShape)
            .border(borderStroke, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = button.label,
            color = Color.White,
            fontSize = if (button.sizeDp < 55) 10.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
