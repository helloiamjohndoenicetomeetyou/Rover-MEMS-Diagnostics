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

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager

class UsbTransceiver private constructor(
    private val usbInterface: UsbInterface,
    private val usbDeviceConnection: UsbDeviceConnection,
    private val usbEndpointIn: UsbEndpoint,
    private val usbEndpointOut: UsbEndpoint
) : Transceiver {
    companion object {
        private const val TIMEOUT_CONTROL_MS = 5000

        private const val TIMEOUT_BULK_TRANSFER_MS = 100

        fun create(usbManager: UsbManager, usbDevice: UsbDevice): UsbTransceiver? {
            val connection = usbManager.openDevice(usbDevice) ?: return null
            val interface0 = usbDevice.getInterface(0)

            if (!connection.claimInterface(interface0, /* force = */ true)) {
                connection.close()
                return null
            }

            var endpointIn: UsbEndpoint? = null
            var endpointOut: UsbEndpoint? = null

            for (i in 0 until interface0.endpointCount) {
                val endpoint = interface0.getEndpoint(i)

                when {
                    endpointIn == null && endpoint.direction == UsbConstants.USB_DIR_IN -> {
                        endpointIn = endpoint
                    }

                    endpointOut == null && endpoint.direction == UsbConstants.USB_DIR_OUT -> {
                        endpointOut = endpoint
                    }
                }

                if (endpointIn != null && endpointOut != null) {
                    break
                }
            }

            if (endpointIn == null || endpointOut == null) {
                connection.releaseInterface(interface0)
                connection.close()
                return null
            }

            return UsbTransceiver(interface0, connection, endpointIn, endpointOut)
        }
    }

    override fun read(bytes: ByteArray): Int =
        usbDeviceConnection.bulkTransfer(usbEndpointIn, bytes, bytes.size, TIMEOUT_BULK_TRANSFER_MS)

    /**
     * Sends the provided ByteArray.
     *
     * @param bytes ByteArray to write.
     */
    override fun write(bytes: ByteArray): Int =
        usbDeviceConnection.bulkTransfer(
            usbEndpointOut,
            bytes,
            bytes.size,
            TIMEOUT_BULK_TRANSFER_MS
        )

    override fun close() {
        usbDeviceConnection.releaseInterface(usbInterface)
        usbDeviceConnection.close()
    }

    fun controlTransfer(
        requestType: Int,
        request: Int,
        value: Int,
        index: Int = 0,
        buffer: ByteArray? = null,
        length: Int = 0,
        timeout: Int = TIMEOUT_CONTROL_MS
    ): Int = usbDeviceConnection.controlTransfer(
        requestType,
        request,
        value,
        index,
        buffer,
        length,
        timeout
    )
}