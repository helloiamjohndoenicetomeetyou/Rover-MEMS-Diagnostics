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

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.driver

import android.hardware.usb.UsbConstants
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.transceiver.UsbTransceiver

class FtdiDriver(private val mUsbTransceiver: UsbTransceiver) {
    companion object {
        private const val USB_RECIPIENT_DEVICE = 0x00

        private const val USB_ENDPOINT_OUT = 0x00

        private const val REQUEST_TYPE_OUT =
            UsbConstants.USB_TYPE_VENDOR or USB_RECIPIENT_DEVICE or USB_ENDPOINT_OUT

        private const val FTDI_SIO_REQUEST_RESET = 0

        private const val FTDI_SIO_REQUEST_SET_BAUD_RATE = 3

        private const val FTDI_SIO_REQUEST_SET_DATA = 4

        private const val FTDI_SIO_VALUE_RESET_SIO = 0

        private const val FTDI_SIO_VALUE_RESET_PURGE_RX = 1

        private const val FTDI_SIO_VALUE_RESET_PURGE_TX = 2

        private const val FTDI_SIO_VALUE_BAUD_RATE_9600 = 0x4138

        // 8 bits
        private const val DATA_BITS = 8

        // No Parity
        private const val PARITY_NONE = 0x00

        // 1 stop bit
        private const val STOP_BITS = 0x00

        private const val FTDI_SIO_VALUE_DATA_8N1 =
            (((0 or DATA_BITS) or (PARITY_NONE shl 8)) or (STOP_BITS shl 11))

        private const val TIMEOUT_ECU_RESPONSE_MS = 1000

        private const val SIZE_FTDI_HEADER = 2

        private const val SIZE_READ_BUFFER = 1024
    }

    fun initialize(): Boolean {

        // Reset FTDI chip.
        if (controlTransfer(FTDI_SIO_REQUEST_RESET, FTDI_SIO_VALUE_RESET_SIO) < 0) {
            return false
        }

        // Set baud rate.
        if (controlTransfer(FTDI_SIO_REQUEST_SET_BAUD_RATE, FTDI_SIO_VALUE_BAUD_RATE_9600) < 0) {
            return false
        }

        // Set data bits, parity, and stop bits.
        if (controlTransfer(FTDI_SIO_REQUEST_SET_DATA, FTDI_SIO_VALUE_DATA_8N1) < 0) {
            return false
        }

        // Purge RX and TX buffers.
        if (!purge()) {
            return false
        }

        return true
    }

    fun close() = mUsbTransceiver.close()

    /**
     * Reads data from the device and stores it in the provided ByteArray.
     *
     * @param bytes The ByteArray to store the received data.
     */
    fun read(bytes: ByteArray): Boolean {
        var cursor = 0
        val response = ByteArray(SIZE_READ_BUFFER)
        val startTime = System.currentTimeMillis()

        do {
            val result = mUsbTransceiver.read(response)

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

            response.fill(0)
        } while (true)

        bytes[0] = cursor.toByte()

        return true
    }

    fun write(bytes: ByteArray): Boolean = mUsbTransceiver.write(bytes)

    private fun controlTransfer(request: Int, value: Int): Int =
        mUsbTransceiver.controlTransfer(
            requestType = REQUEST_TYPE_OUT,
            request = request,
            value = value
        )

    private fun purge(): Boolean {
        val rx = controlTransfer(FTDI_SIO_REQUEST_RESET, FTDI_SIO_VALUE_RESET_PURGE_RX)
        val tx = controlTransfer(FTDI_SIO_REQUEST_RESET, FTDI_SIO_VALUE_RESET_PURGE_TX)

        return 0 <= rx && 0 <= tx
    }
}