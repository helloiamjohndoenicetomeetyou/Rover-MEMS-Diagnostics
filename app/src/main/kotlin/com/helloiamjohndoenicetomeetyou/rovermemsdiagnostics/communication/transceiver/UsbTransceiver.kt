/*
 * Copyright (C) 2026 helloiamjohndoenicetomeetyou
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.transceiver

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager

class UsbTransceiver private constructor(
    private val mInterface: UsbInterface,
    private val mConnection: UsbDeviceConnection,
    private val mEndpointIn: UsbEndpoint,
    private val mEndpointOut: UsbEndpoint
) {
    companion object {
        private const val TIMEOUT_CONTROL_MS = 5000

        private const val TIMEOUT_BULK_TRANSFER_MS = 100

        private const val TIMEOUT_ECU_RESPONSE_MS = 1000

        private const val SIZE_FTDI_HEADER = 2

        private const val SIZE_READ_BUFFER = 1024

        fun create(usbManager: UsbManager, usbDevice: UsbDevice): UsbTransceiver? {
            val connection = usbManager.openDevice(usbDevice) ?: return null
            val usbInterface = usbDevice.getInterface(0)

            if (!connection.claimInterface(usbInterface, /* force = */ true)) {
                connection.close()
                return null
            }

            usbInterface.endpointCount.let { count ->
                if (count < 2) {
                    connection.releaseInterface(usbInterface)
                    connection.close()
                    return null
                }
            }

            val endpointIn = usbInterface.getEndpoint(0) ?: return null
            val endpointOut = usbInterface.getEndpoint(1) ?: return null

            return UsbTransceiver(usbInterface, connection, endpointIn, endpointOut)
        }
    }

    fun controlTransfer(
        requestType: Int,
        request: Int,
        value: Int,
        index: Int = 0,
        buffer: ByteArray? = null,
        length: Int = 0,
        timeout: Int = TIMEOUT_CONTROL_MS
    ) = mConnection.controlTransfer(
        requestType,
        request,
        value,
        index,
        buffer,
        length,
        timeout
    )

    fun close() {
        mConnection.releaseInterface(mInterface)
        mConnection.close()
    }

    /**
     * Reads data from the device and stores it in the provided ByteArray.
     *
     * @param bytes The ByteArray to store the received data.
     */
    fun read(bytes: ByteArray): Boolean {
        var result: Int
        var cursor = 0
        val startTime = System.currentTimeMillis()
        do {
            val response = ByteArray(SIZE_READ_BUFFER)
            result = mConnection.bulkTransfer(
                mEndpointIn,
                response,
                response.size,
                TIMEOUT_BULK_TRANSFER_MS
            )

            if (result < 0) {
                return false
            }

            if (result == SIZE_FTDI_HEADER && cursor != 0) {
                break
            }

            for (i in SIZE_FTDI_HEADER until result) {
                if (cursor < bytes.size - 1) {
                    bytes[++cursor] = response[i]
                } else {
                    return false
                }
            }

            if (TIMEOUT_ECU_RESPONSE_MS < (System.currentTimeMillis() - startTime)) {
                return false
            }
        } while (true)

        // First byte is total read size.
        bytes[0] = cursor.toByte()

        return true
    }

    /**
     * Sends the provided ByteArray byte-by-byte.
     *
     * @param bytes ByteArray to write.
     */
    fun write(bytes: ByteArray): Boolean {
        bytes.forEach { byte ->
            val result = mConnection.bulkTransfer(
                mEndpointOut,
                byteArrayOf(byte),
                /* length = */ 1,
                TIMEOUT_BULK_TRANSFER_MS
            )
            if (result < 0) {
                return false
            }
        }
        return true
    }
}