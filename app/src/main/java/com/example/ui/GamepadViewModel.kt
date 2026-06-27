package com.example.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.BluetoothHidManager
import com.example.data.db.GamepadDatabase
import com.example.data.model.ControllerButton
import com.example.data.model.SettingsProfile
import com.example.data.repository.GamepadRepository
import com.example.network.UdpGamepadTransmitter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.experimental.and
import kotlin.experimental.or

class GamepadViewModel(
    private val context: Context,
    private val repository: GamepadRepository
) : ViewModel() {

    // UI States
    val buttons: StateFlow<List<ControllerButton>> = repository.allButtons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<SettingsProfile> = repository.settings
        .map { it ?: SettingsProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsProfile())

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _selectedButtonForEdit = MutableStateFlow<ControllerButton?>(null)
    val selectedButtonForEdit: StateFlow<ControllerButton?> = _selectedButtonForEdit.asStateFlow()

    private val _bluetoothConnected = MutableStateFlow(false)
    val bluetoothConnected: StateFlow<Boolean> = _bluetoothConnected.asStateFlow()

    private val _bluetoothDeviceName = MutableStateFlow<String?>(null)
    val bluetoothDeviceName: StateFlow<String?> = _bluetoothDeviceName.asStateFlow()

    private val _transmissionActive = MutableStateFlow(false)
    val transmissionActive: StateFlow<Boolean> = _transmissionActive.asStateFlow()

    // Active gamepad state
    private var lx = 0f
    private var ly = 0f
    private var rx = 0f
    private var ry = 0f
    private var activeButtonsMask: Short = 0
    private var dpadMask: Byte = 8 // neutral

    // Low latency transmitters
    private val udpTransmitter = UdpGamepadTransmitter()
    private val bluetoothHidManager = BluetoothHidManager(context)

    // Vibrator for tactile feedback
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfEmpty()
        }

        // Setup Bluetooth connection state listener
        bluetoothHidManager.onConnectionStateChanged = { connected, deviceName ->
            _bluetoothConnected.value = connected
            _bluetoothDeviceName.value = deviceName
            if (settings.value.connectionType == "BLUETOOTH_HID") {
                _transmissionActive.value = connected
            }
        }

        // Start transmitters based on settings
        viewModelScope.launch {
            settings.collect { currentSettings ->
                // Stop previous
                udpTransmitter.stop()
                bluetoothHidManager.stop()

                if (currentSettings.connectionType == "WIFI_UDP") {
                    udpTransmitter.start(currentSettings.targetIp, currentSettings.targetPort)
                    _transmissionActive.value = true
                } else if (currentSettings.connectionType == "BLUETOOTH_HID") {
                    bluetoothHidManager.start()
                    _transmissionActive.value = _bluetoothConnected.value
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        udpTransmitter.stop()
        bluetoothHidManager.stop()
    }

    // --- Customization Mode ---
    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
        if (!_isEditMode.value) {
            _selectedButtonForEdit.value = null
        }
    }

    fun selectButtonForEdit(button: ControllerButton?) {
        _selectedButtonForEdit.value = button
    }

    fun updateButtonPosition(buttonId: Int, xPercent: Float, yPercent: Float) {
        viewModelScope.launch {
            val buttonList = buttons.value
            val button = buttonList.find { it.id == buttonId }
            if (button != null) {
                val updated = button.copy(
                    xPercent = xPercent.coerceIn(0f, 100f),
                    yPercent = yPercent.coerceIn(0f, 100f)
                )
                repository.updateButton(updated)
                if (_selectedButtonForEdit.value?.id == buttonId) {
                    _selectedButtonForEdit.value = updated
                }
            }
        }
    }

    fun updateButtonSize(buttonId: Int, sizeDp: Int) {
        viewModelScope.launch {
            val buttonList = buttons.value
            val button = buttonList.find { it.id == buttonId }
            if (button != null) {
                val updated = button.copy(sizeDp = sizeDp.coerceIn(30, 200))
                repository.updateButton(updated)
                if (_selectedButtonForEdit.value?.id == buttonId) {
                    _selectedButtonForEdit.value = updated
                }
            }
        }
    }

    fun updateButtonLabel(buttonId: Int, label: String) {
        viewModelScope.launch {
            val buttonList = buttons.value
            val button = buttonList.find { it.id == buttonId }
            if (button != null) {
                val updated = button.copy(label = label)
                repository.updateButton(updated)
                if (_selectedButtonForEdit.value?.id == buttonId) {
                    _selectedButtonForEdit.value = updated
                }
            }
        }
    }

    fun resetLayoutToDefault() {
        viewModelScope.launch {
            repository.resetToDefaultLayout()
            _selectedButtonForEdit.value = null
        }
    }

    fun saveSettingsProfile(profile: SettingsProfile) {
        viewModelScope.launch {
            repository.saveSettings(profile)
        }
    }

    // --- Controller Inputs & Transmission ---
    fun onButtonPressStateChanged(buttonName: String, pressed: Boolean) {
        if (_isEditMode.value) return

        val mask = getButtonMask(buttonName)
        if (mask != 0.toShort()) {
            val maskInt = mask.toInt()
            val currentMaskInt = activeButtonsMask.toInt()
            val newMaskInt = if (pressed) {
                currentMaskInt or maskInt
            } else {
                currentMaskInt and maskInt.inv()
            }
            activeButtonsMask = newMaskInt.toShort()
            
            // Haptic/tactile feedback on press
            if (pressed) {
                triggerHapticFeedback()
            }

            transmitCurrentState()
        }
    }

    fun onJoystickMoved(isLeft: Boolean, x: Float, y: Float) {
        if (_isEditMode.value) return

        if (isLeft) {
            lx = x
            ly = y
        } else {
            rx = x
            ry = y
        }
        transmitCurrentState()
    }

    fun onDpadChanged(direction: Byte) {
        if (_isEditMode.value) return

        dpadMask = direction
        if (direction != 8.toByte()) {
            triggerHapticFeedback(20L)
        }
        transmitCurrentState()
    }

    private fun triggerHapticFeedback(durationMs: Long = 30L) {
        try {
            val intensity = settings.value.vibrationIntensity
            if (intensity > 0f) {
                vibrator?.vibrate((durationMs * intensity).toLong().coerceAtLeast(1L))
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun transmitCurrentState() {
        val currentSettings = settings.value
        val battery = getBatteryPercent()

        if (currentSettings.connectionType == "WIFI_UDP") {
            udpTransmitter.sendState(
                lx = lx,
                ly = ly,
                rx = rx,
                ry = ry,
                buttonsMask = activeButtonsMask,
                dpadMask = dpadMask,
                batteryPercent = battery
            )
        } else if (currentSettings.connectionType == "BLUETOOTH_HID") {
            bluetoothHidManager.sendReport(
                lx = lx,
                ly = ly,
                rx = rx,
                ry = ry,
                buttonsMask = activeButtonsMask,
                dpadMask = dpadMask
            )
        }
    }

    private fun getBatteryPercent(): Int {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt()
        } else {
            100
        }
    }

    private fun getButtonMask(name: String): Short {
        return when (name) {
            "A" -> 0x0001
            "B" -> 0x0002
            "X" -> 0x0004
            "Y" -> 0x0008
            "LB" -> 0x0010
            "RB" -> 0x0020
            "LT" -> 0x0040
            "RT" -> 0x0080
            "SELECT", "BACK" -> 0x0100
            "START" -> 0x0200
            "LSTICK_CLICK" -> 0x0400
            "RSTICK_CLICK" -> 0x0800
            else -> 0
        }
    }

    // ViewModel Factory
    class Factory(
        private val context: Context,
        private val repository: GamepadRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GamepadViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GamepadViewModel(context, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
