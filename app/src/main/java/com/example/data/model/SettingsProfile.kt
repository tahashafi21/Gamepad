package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings_profile")
data class SettingsProfile(
    @PrimaryKey val id: Int = 1, // Only 1 active profile for simplicity
    val buttonSensitivity: Float = 1.0f,
    val joystickSensitivity: Float = 1.0f,
    val deadzone: Float = 0.15f,
    val vibrationIntensity: Float = 0.5f,
    val connectionType: String = "WIFI_UDP", // "WIFI_UDP" or "BLUETOOTH_HID"
    val targetIp: String = "192.168.1.100",
    val targetPort: Int = 5001,
    val emulationMode: String = "XBOX_360" // "XBOX_360" or "PLAYSTATION_4" or "GENERIC_HID"
)
