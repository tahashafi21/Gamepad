package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "controller_buttons")
data class ControllerButton(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,         // e.g., "A", "B", "X", "Y", "LB", "RB", "START", "SELECT", "LSTICK", "RSTICK", "DPAD"
    val label: String,        // Label shown on screen
    val xPercent: Float,      // X coordinate as a percent of screen width (0-100)
    val yPercent: Float,      // Y coordinate as a percent of screen height (0-100)
    val sizeDp: Int = 64,     // Visual diameter/size
    val type: String = "BUTTON" // "BUTTON", "DPAD", "JOYSTICK"
)
