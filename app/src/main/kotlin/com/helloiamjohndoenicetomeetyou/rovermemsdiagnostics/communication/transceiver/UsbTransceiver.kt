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
    private val mInterface: UsbInterface,
    private val mConnection: UsbDeviceConnection,
    private val mEndpointIn: UsbEndpoint,
    private val mEndpointOut: UsbEndpoint
) : Transceiver {
    companion object {
        private const val TIMEOUT_CONTROL_MS = 5000

        private const val TIMEOUT_BULK_TRANSFER_MS = 100

        fun create(usbManager: UsbManager, usbDevice: UsbDevice): UsbTransceiver? {
            val connection = usbManager.openDevice(usbDevice) ?: return null
            val usbInterface = usbDevice.getInterface(0)

            if (!connection.claimInterface(usbInterface, /* force = */ true)) {
                connection.close()
                return null
            }

            var endpointIn: UsbEndpoint? = null
            var endpointOut: UsbEndpoint? = null

            for (i in 0 until usbInterface.endpointCount) {
                val endpoint = usbInterface.getEndpoint(i)

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
                connection.releaseInterface(usbInterface)
                connection.close()
                return null
            }

            return UsbTransceiver(usbInterface, connection, endpointIn, endpointOut)
        }
    }

    override fun read(bytes: ByteArray): Int =
        mConnection.bulkTransfer(mEndpointIn, bytes, bytes.size, TIMEOUT_BULK_TRANSFER_MS)

    /**
     * Sends the provided ByteArray.
     *
     * @param bytes ByteArray to write.
     */
    override fun write(bytes: ByteArray): Int =
        mConnection.bulkTransfer(mEndpointOut, bytes, bytes.size, TIMEOUT_BULK_TRANSFER_MS)

    override fun close() {
        mConnection.releaseInterface(mInterface)
        mConnection.close()
    }

    fun controlTransfer(
        requestType: Int,
        request: Int,
        value: Int,
        index: Int = 0,
        buffer: ByteArray? = null,
        length: Int = 0,
        timeout: Int = TIMEOUT_CONTROL_MS
    ): Int = mConnection.controlTransfer(
        requestType,
        request,
        value,
        index,
        buffer,
        length,
        timeout
    )
}