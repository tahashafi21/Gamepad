package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receiver.CompanionAppScript
import com.example.ui.GamepadViewModel

@Composable
fun ConnectionScreen(
    viewModel: GamepadViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section matching Design HTML
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                        text = "PRO",
                        color = Color(0xFF6366F1),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Top)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF22C55E), shape = RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (settings.connectionType == "BLUETOOTH_HID") "CONNECTED VIA BLUETOOTH" else "CONNECTED VIA WIFI",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Guide Card for Connection Mode
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF27272A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "How to connect to Windows or macOS",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                if (settings.connectionType == "WIFI_UDP") {
                    Text(
                        text = "1. Connect your phone and PC/Mac to the same Local Area Network (WiFi).\n" +
                               "2. Open a terminal on your computer, find your local IP address (e.g. using ipconfig on Windows or ifconfig on Mac).\n" +
                               "3. Enter your computer's IP address in the Settings tab of this app.\n" +
                               "4. Run the Companion Python script on your PC/Mac to handle inputs and display the floating battery widget.",
                        color = Color(0xFFD4D4D8),
                        fontSize = 12.sp,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = "1. Open Bluetooth Settings on your Windows PC or Mac.\n" +
                               "2. Enable Bluetooth on this phone, pair it, and connect to your PC.\n" +
                               "3. Once connected, Windows/Mac will natively detect the phone as a 'Bluetooth Gamepad'.\n" +
                               "4. In Bluetooth HID mode, the phone directly sends native USB HID report packets for ultra-low latency without needing server software!",
                        color = Color(0xFFD4D4D8),
                        fontSize = 12.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Widget & Companion script Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFF27272A))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Desktop Battery Widget & Xbox Emulation Script",
                    color = Color(0xFF818CF8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Below is the complete Companion Python Receiver script. Copy this script, save it as 'companion_receiver.py' on your computer, and run it. It simulates a 100% compatible Xbox 360 Controller and displays a borderless floating widget showing your phone's battery status on your desktop!",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Gamepad Companion Script", CompanionAppScript.PYTHON_SCRIPT)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Script copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("COPY COMPANION PYTHON SCRIPT 📋", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.5.sp)
                }

                // Script preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF09090B), shape = RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF27272A), shape = RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = CompanionAppScript.PYTHON_SCRIPT,
                        color = Color(0xFF818CF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}
