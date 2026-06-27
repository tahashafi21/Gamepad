package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class BluetoothHidManager(private val context: Context) {

    private val executor = Executors.newSingleThreadExecutor()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    
    var onConnectionStateChanged: ((Boolean, String?) -> Unit)? = null

    // Standard Gamepad HID Report Descriptor
    private val hidDescriptor = byteArrayOf(
        0x05.toByte(), 0x01.toByte(),        // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x05.toByte(),        // USAGE (Gamepad)
        0xa1.toByte(), 0x01.toByte(),        // COLLECTION (Application)
        0x85.toByte(), 0x01.toByte(),        //   REPORT_ID (1)
        
        // 16 Buttons (bitmask)
        0x05.toByte(), 0x09.toByte(),        //   USAGE_PAGE (Button)
        0x19.toByte(), 0x01.toByte(),        //   USAGE_MINIMUM (Button 1)
        0x29.toByte(), 0x10.toByte(),        //   USAGE_MAXIMUM (Button 16)
        0x15.toByte(), 0x00.toByte(),        //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(),        //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(),        //   REPORT_SIZE (1)
        0x95.toByte(), 0x10.toByte(),        //   REPORT_COUNT (16)
        0x81.toByte(), 0x02.toByte(),        //   INPUT (Data,Var,Abs)
        
        // 4 Analog Axes: LX, LY, RX, RY (0 to 255, 127 is center)
        0x05.toByte(), 0x01.toByte(),        //   USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x30.toByte(),        //   USAGE (X)
        0x09.toByte(), 0x31.toByte(),        //   USAGE (Y)
        0x09.toByte(), 0x32.toByte(),        //   USAGE (Z)
        0x09.toByte(), 0x35.toByte(),        //   USAGE (Rz)
        0x15.toByte(), 0x00.toByte(),        //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0xff.toByte(),        //   LOGICAL_MAXIMUM (255)
        0x75.toByte(), 0x08.toByte(),        //   REPORT_SIZE (8)
        0x95.toByte(), 0x04.toByte(),        //   REPORT_COUNT (4)
        0x81.toByte(), 0x02.toByte(),        //   INPUT (Data,Var,Abs)
        
        // D-Pad (8-way Hat Switch: 0-7, 8 is released)
        0x09.toByte(), 0x39.toByte(),        //   USAGE (Hat switch)
        0x15.toByte(), 0x00.toByte(),        //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x07.toByte(),        //   LOGICAL_MAXIMUM (7)
        0x35.toByte(), 0x00.toByte(),        //   PHYSICAL_MINIMUM (0)
        0x46.toByte(), 0x3b.toByte(), 0x01.toByte(), // PHYSICAL_MAXIMUM (315)
        0x65.toByte(), 0x14.toByte(),        //   UNIT (Eng Rot: Angular Pos)
        0x75.toByte(), 0x04.toByte(),        //   REPORT_SIZE (4)
        0x95.toByte(), 0x01.toByte(),        //   REPORT_COUNT (1)
        0x81.toByte(), 0x42.toByte(),        //   INPUT (Data,Var,Abs,Null)
        
        // Padding (4 bits to align to whole byte)
        0x95.toByte(), 0x01.toByte(),        //   REPORT_COUNT (1)
        0x75.toByte(), 0x04.toByte(),        //   REPORT_SIZE (4)
        0x81.toByte(), 0x03.toByte(),        //   INPUT (Cnst,Var,Abs)
        
        0xc0.toByte()                        // END_COLLECTION
    )

    private val sdpSettings by lazy {
        BluetoothHidDeviceAppSdpSettings(
            "Bluetooth Gamepad",
            "Controller",
            "Google",
            0x08.toByte(), // Gamepad HID subclass
            hidDescriptor
        )
    }

    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothAdapter = manager?.adapter
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun start() {
        if (!isBluetoothSupported()) return
        
        try {
            bluetoothAdapter?.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE)
        } catch (e: Exception) {
            Log.e("BluetoothHidManager", "Error binding HID Device Profile: ${e.message}")
        }
    }

    fun stop() {
        try {
            hidDevice?.let {
                it.unregisterApp()
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, it)
            }
            hidDevice = null
            connectedDevice = null
            onConnectionStateChanged?.invoke(false, null)
        } catch (e: Exception) {
            Log.e("BluetoothHidManager", "Error unbinding HID profile: ${e.message}")
        }
    }

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as? BluetoothHidDevice
                registerHidApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
                connectedDevice = null
                onConnectionStateChanged?.invoke(false, null)
            }
        }
    }

    private fun registerHidApp() {
        val device = hidDevice ?: return
        try {
            device.registerApp(
                sdpSettings,
                null,
                null,
                executor,
                callback
            )
            Log.d("BluetoothHidManager", "HID Device App Registered successfully")
        } catch (e: Exception) {
            Log.e("BluetoothHidManager", "Failed to register HID App: ${e.message}")
        }
    }

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d("BluetoothHidManager", "onAppStatusChanged: registered = $registered")
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            val connected = state == BluetoothProfile.STATE_CONNECTED
            connectedDevice = if (connected) device else null
            val deviceName = device?.name ?: device?.address
            Log.d("BluetoothHidManager", "onConnectionStateChanged: state = $state, device = $deviceName")
            
            onConnectionStateChanged?.invoke(connected, deviceName)
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            // Not strictly required for simple controller output
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            // Can be used for rumbling/vibration commands from PC
        }

        override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
            Log.d("BluetoothHidManager", "Protocol set: $protocol")
        }

        override fun onInterruptData(device: BluetoothDevice?, reportId: Byte, data: ByteArray?) {
            // Handle incoming interrupt data
        }
    }

    /**
     * Sends Gamepad Input Report.
     * Report payload structure mapping to hidDescriptor (7 bytes):
     * - Byte 0, 1: buttonsMask (16-bit Short, little-endian)
     * - Byte 2: lx (0 to 255)
     * - Byte 3: ly (0 to 255)
     * - Byte 4: rx (0 to 255)
     * - Byte 5: ry (0 to 255)
     * - Byte 6: Hat Switch (D-Pad, 0-7. 8 is neutral)
     */
    fun sendReport(
        lx: Float, // -1f to 1f
        ly: Float,
        rx: Float,
        ry: Float,
        buttonsMask: Short,
        dpadMask: Byte // 0-7, 8 is release
    ) {
        val device = hidDevice ?: return
        val target = connectedDevice ?: return

        executor.submit {
            try {
                // Map float sticks (-1f..1f) to (0..255), 127 is neutral center
                val lXByte = ((lx + 1f) * 127.5f).coerceIn(0f, 255f).toInt().toByte()
                val lYByte = ((ly + 1f) * 127.5f).coerceIn(0f, 255f).toInt().toByte()
                val rXByte = ((rx + 1f) * 127.5f).coerceIn(0f, 255f).toInt().toByte()
                val rYByte = ((ry + 1f) * 127.5f).coerceIn(0f, 255f).toInt().toByte()

                val report = ByteArray(7)
                // Buttons mask (2 bytes)
                report[0] = (buttonsMask.toInt() and 0xFF).toByte()
                report[1] = ((buttonsMask.toInt() shr 8) and 0xFF).toByte()
                // Sticks
                report[2] = lXByte
                report[3] = lYByte
                report[4] = rXByte
                report[5] = rYByte
                // D-Pad + padding (Lower 4 bits are D-Pad, Upper 4 bits padding)
                report[6] = dpadMask

                device.sendReport(target, 1, report)
            } catch (e: Exception) {
                Log.e("BluetoothHidManager", "Error sending HID Report: ${e.message}")
            }
        }
    }
}
