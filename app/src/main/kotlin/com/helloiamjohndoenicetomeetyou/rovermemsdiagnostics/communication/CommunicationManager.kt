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
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.MainActivity
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
    private val mMainActivity: MainActivity,
    private val mUsbManager: UsbManager
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

    val isConnected: Boolean get() = mMemsProtocol != null

    private var mMemsProtocol: MemsProtocol? = null

    private var mLiveDataJob: Job? = null

    private val mCommunicationDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val mScope = CoroutineScope(mCommunicationDispatcher + SupervisorJob())

    private val mCommunicationMutex = Mutex()

    private fun connectInternal(device: UsbDevice?) {
        mScope.launch {
            try {
                disconnectInternal()

                device ?: throw Exception("Null Device")

                val transceiver = UsbTransceiver.create(mUsbManager, device)
                    ?: throw Exception("Failed to create transceiver.")

                val driver = FtdiDriver(transceiver)
                if (!driver.initialize()) {
                    driver.close()
                    throw Exception("Failed to initialize driver.")
                }

                val memsProtocol = Mems16Protocol(driver)
                if (!memsProtocol.initialize()) {
                    memsProtocol.close()
                    throw Exception("Failed to initialize protocol.")
                }

                mMemsProtocol = memsProtocol

                mMainActivity.postMessage(MainActivity.Companion.MessageId.CONNECTED.ordinal)

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
        mLiveDataJob?.cancel()
        mLiveDataJob = mScope.launch {
            while (isActive && isConnected) {
                mCommunicationMutex.withLock {
                    val memsProtocol = mMemsProtocol ?: return@withLock

                    val dataPacket = memsProtocol.requestLiveData()
                    if (dataPacket != null) {
                        mMainActivity.postMessage(
                            what = MainActivity.Companion.MessageId.LIVE_DATA.ordinal,
                            obj = dataPacket
                        )
                    } else {
                        handleError()
                    }
                }

                delay(timeMillis = DELAY_REQUEST_DATA)
            }
        }
    }

    fun clearFaultCodes() {
        mScope.launch {
            if (!isConnected) {
                return@launch
            }

            mCommunicationMutex.withLock {
                val memsProtocol = mMemsProtocol ?: return@withLock

                if (memsProtocol.clearFaultCodes()) {
                    mMainActivity.postMessage(MainActivity.Companion.MessageId.FAULT_CODES.ordinal)
                } else {
                    handleError()
                }
            }
        }
    }

    fun performTuning(buttonId: TuningButtonId) {
        mScope.launch {
            if (!isConnected) {
                return@launch
            }

            mCommunicationMutex.withLock {
                val memsProtocol = mMemsProtocol ?: return@withLock

                val newValue = memsProtocol.performTuning(buttonId.ordinal)
                if (newValue != null) {
                    mMainActivity.postMessage(
                        what = MainActivity.Companion.MessageId.TUNING.ordinal,
                        obj = Pair(buttonId, newValue)
                    )
                } else {
                    handleError()
                }
            }
        }
    }

    private suspend fun disconnectInternal() {
        mCommunicationMutex.withLock {
            mLiveDataJob?.cancel()
            mMemsProtocol?.close()
            mMemsProtocol = null
            mMainActivity.postMessage(MainActivity.Companion.MessageId.DISCONNECTED.ordinal)
        }
    }

    fun disconnect() {
        mScope.launch {
            disconnectInternal()
        }
    }

    private fun handleError() {
        if (!isConnected) {
            return
        }

        mScope.launch {
            disconnectInternal()
        }
    }

    fun release() {
        mScope.cancel()
        mCommunicationDispatcher.close()
    }
}