package com.example.data.repository

import com.example.data.db.GamepadDao
import com.example.data.model.ControllerButton
import com.example.data.model.SettingsProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GamepadRepository(private val gamepadDao: GamepadDao) {

    val allButtons: Flow<List<ControllerButton>> = gamepadDao.getAllButtons()
    val settings: Flow<SettingsProfile?> = gamepadDao.getSettings()

    suspend fun insertButton(button: ControllerButton) = gamepadDao.insertButton(button)
    suspend fun updateButton(button: ControllerButton) = gamepadDao.updateButton(button)
    suspend fun deleteButton(button: ControllerButton) = gamepadDao.deleteButton(button)

    suspend fun saveSettings(profile: SettingsProfile) = gamepadDao.insertSettings(profile)

    suspend fun initializeDefaultsIfEmpty() {
        val currentButtons = allButtons.firstOrNull() ?: emptyList()
        if (currentButtons.isEmpty()) {
            resetToDefaultLayout()
        }
        val currentSettings = settings.firstOrNull()
        if (currentSettings == null) {
            gamepadDao.insertSettings(SettingsProfile())
        }
    }

    suspend fun resetToDefaultLayout() {
        gamepadDao.clearAllButtons()
        
        val defaultButtons = listOf(
            // Joysticks
            ControllerButton(name = "LSTICK", label = "L", xPercent = 18f, yPercent = 60f, sizeDp = 110, type = "JOYSTICK"),
            ControllerButton(name = "RSTICK", label = "R", xPercent = 68f, yPercent = 60f, sizeDp = 110, type = "JOYSTICK"),
            
            // D-Pad
            ControllerButton(name = "DPAD", label = "D-Pad", xPercent = 18f, yPercent = 22f, sizeDp = 100, type = "DPAD"),
            
            // Action Buttons
            ControllerButton(name = "A", label = "A", xPercent = 82f, yPercent = 60f, sizeDp = 64, type = "BUTTON"),
            ControllerButton(name = "B", label = "B", xPercent = 90f, yPercent = 45f, sizeDp = 64, type = "BUTTON"),
            ControllerButton(name = "X", label = "X", xPercent = 74f, yPercent = 45f, sizeDp = 64, type = "BUTTON"),
            ControllerButton(name = "Y", label = "Y", xPercent = 82f, yPercent = 30f, sizeDp = 64, type = "BUTTON"),
            
            // Bumpers & Triggers
            ControllerButton(name = "LB", label = "LB", xPercent = 10f, yPercent = 5f, sizeDp = 64, type = "BUTTON"),
            ControllerButton(name = "RB", label = "RB", xPercent = 90f, yPercent = 5f, sizeDp = 64, type = "BUTTON"),
            ControllerButton(name = "LT", label = "LT", xPercent = 26f, yPercent = 5f, sizeDp = 64, type = "BUTTON"),
            ControllerButton(name = "RT", label = "RT", xPercent = 74f, yPercent = 5f, sizeDp = 64, type = "BUTTON"),
            
            // Middle Menu Buttons
            ControllerButton(name = "START", label = "START", xPercent = 54f, yPercent = 10f, sizeDp = 50, type = "BUTTON"),
            ControllerButton(name = "SELECT", label = "BACK", xPercent = 46f, yPercent = 10f, sizeDp = 50, type = "BUTTON")
        )
        gamepadDao.insertButtons(defaultButtons)
    }
}
