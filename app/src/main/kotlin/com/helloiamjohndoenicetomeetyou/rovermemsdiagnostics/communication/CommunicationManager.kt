/*
 * Copyright (C) 2025 helloiamjohndoenicetomeetyou
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

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.driver.FtdiDriver
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.protocol.Mems16Protocol
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.protocol.MemsProtocol
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.transceiver.UsbTransceiver
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningButtonId
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CommunicationManager(
    private val usbManager: UsbManager,
    private val onStatusChanged: (isConnected: Boolean) -> Unit,
    private val onLiveDataReceived: (data: DataPacket) -> Unit,
    private val onFaultCodesCleared: (result: Boolean) -> Unit,
    private val onTuningPerformed: (data: DataPacket) -> Unit
) {
    companion object {
        // private const val LIB_USB_ENDPOINT_IN: Int = 0x80

        // private const val FTDI_SIO_SET_BREAK_ON = 1 shl 14

        // private const val FTDI_SIO_SET_BREAK_OFF = 0 shl 14

        /*
        private const val REQUEST_TYPE_IN: Int =
            UsbConstants.USB_TYPE_VENDOR or
                    LIB_USB_RECIPIENT_DEVICE or
                    LIB_USB_ENDPOINT_IN
         */

        private const val DELAY_REQUEST_DATA = 500L
    }

    private var memsProtocol: MemsProtocol? = null

    private var liveDataJob: Job? = null

    private val isConnected: Boolean get() = memsProtocol != null

    private val communicationDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val coroutineScope = CoroutineScope(communicationDispatcher + SupervisorJob())

    private val communicationMutex = Mutex()

    private fun connectInternal(device: UsbDevice?) {
        coroutineScope.launch {
            try {
                disconnectInternal()

                device ?: throw Exception("Null Device")

                val transceiver = UsbTransceiver.create(usbManager, device)
                    ?: throw Exception("Failed to create transceiver.")

                val driver = FtdiDriver(transceiver)
                if (!driver.initialize()) {
                    driver.close()
                    throw Exception("Failed to initialize driver.")
                }

                val protocol = Mems16Protocol(driver)
                if (!protocol.initialize()) {
                    protocol.close()
                    throw Exception("Failed to initialize protocol.")
                }

                memsProtocol = protocol

                onStatusChanged(isConnected)

                requestLiveData()
            } catch (e: Exception) {
                e.printStackTrace()
                handleError()
            }
        }
    }

    fun connect(device: UsbDevice?) {
        connectInternal(device)
    }

    private fun requestLiveData() {
        liveDataJob?.cancel()
        liveDataJob = coroutineScope.launch {
            while (isActive && isConnected) {
                communicationMutex.withLock {
                    val protocol = memsProtocol ?: return@withLock

                    val data = protocol.requestLiveData()
                    if (data != null) {
                        onLiveDataReceived(data)
                    } else {
                        handleError()
                    }
                }

                delay(timeMillis = DELAY_REQUEST_DATA)
            }
        }
    }

    fun clearFaultCodes() {
        coroutineScope.launch {
            if (!isConnected) {
                return@launch
            }

            communicationMutex.withLock {
                val protocol = memsProtocol ?: return@withLock

                if (protocol.clearFaultCodes()) {
                    onFaultCodesCleared(true)
                } else {
                    handleError()
                }
            }
        }
    }

    fun performTuning(buttonId: TuningButtonId) {
        coroutineScope.launch {
            if (!isConnected) {
                return@launch
            }

            communicationMutex.withLock {
                val protocol = memsProtocol ?: return@withLock

                val data = protocol.performTuning(buttonId)
                if (data != null) {
                    onTuningPerformed(data)
                } else {
                    handleError()
                }
            }
        }
    }

    private suspend fun disconnectInternal() {
        communicationMutex.withLock {
            liveDataJob?.cancel()
            memsProtocol?.close()
            memsProtocol = null
            onStatusChanged(isConnected)
        }
    }

    fun disconnect() {
        coroutineScope.launch {
            disconnectInternal()
        }
    }

    private fun handleError() {
        if (!isConnected) {
            return
        }

        coroutineScope.launch {
            disconnectInternal()
        }
    }

    fun release() {
        coroutineScope.cancel()
        communicationDispatcher.close()
    }
}