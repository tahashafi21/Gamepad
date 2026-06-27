package com.example.network

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors

class UdpGamepadTransmitter {

    private val executor = Executors.newSingleThreadExecutor()
    private var socket: DatagramSocket? = null
    private var ipAddress: InetAddress? = null
    private var port: Int = 5001

    @Volatile
    private var isConnected = false

    fun start(ip: String, targetPort: Int) {
        executor.submit {
            try {
                closeSocket()
                socket = DatagramSocket()
                ipAddress = InetAddress.getByName(ip)
                port = targetPort
                isConnected = true
                Log.d("UdpTransmitter", "UDP socket started to $ip:$targetPort")
            } catch (e: Exception) {
                Log.e("UdpTransmitter", "Error starting UDP transmitter: ${e.message}")
                isConnected = false
            }
        }
    }

    fun stop() {
        isConnected = false
        executor.submit {
            closeSocket()
        }
    }

    private fun closeSocket() {
        try {
            socket?.close()
            socket = null
        } catch (e: Exception) {
            Log.e("UdpTransmitter", "Error closing socket: ${e.message}")
        }
    }

    /**
     * Sends the current gamepad state to the PC over UDP.
     * Compact 24-byte binary packet structure:
     * - Header: 'G' 'P' 'A' 'D' (4 bytes)
     * - LX, LY: (2 floats, 8 bytes)
     * - RX, RY: (2 floats, 8 bytes)
     * - Buttons mask: (2 bytes / 16 bits short)
     * - D-Pad mask: (1 byte)
     * - Battery level: (1 byte)
     */
    fun sendState(
        lx: Float,
        ly: Float,
        rx: Float,
        ry: Float,
        buttonsMask: Short,
        dpadMask: Byte,
        batteryPercent: Int
    ) {
        if (!isConnected) return

        executor.submit {
            try {
                val buffer = ByteBuffer.allocate(24).apply {
                    order(ByteOrder.LITTLE_ENDIAN)
                    put('G'.toByte())
                    put('P'.toByte())
                    put('A'.toByte())
                    put('D'.toByte())
                    putFloat(lx)
                    putFloat(ly)
                    putFloat(rx)
                    putFloat(ry)
                    putShort(buttonsMask)
                    put(dpadMask)
                    put(batteryPercent.toByte())
                }

                val data = buffer.array()
                val currentSocket = socket
                val currentIp = ipAddress
                if (currentSocket != null && currentIp != null) {
                    val packet = DatagramPacket(data, data.size, currentIp, port)
                    currentSocket.send(packet)
                }
            } catch (e: Exception) {
                Log.e("UdpTransmitter", "Error sending UDP packet: ${e.message}")
            }
        }
    }
}
