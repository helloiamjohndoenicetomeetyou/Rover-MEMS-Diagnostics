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

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.driver.FtdiDriver
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.transceiver.UsbTransceiver
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningButtonId

class CommunicationManager(
    private val mMainActivity: MainActivity,
    private val mUsbManager: UsbManager
) {
    companion object {
        enum class MessageId {
            CONNECT,
            DISCONNECT,
            CLEAR_FAULT_CODES,
            PERFORM_TUNING
        }

        // private const val LIB_USB_ENDPOINT_IN: Int = 0x80

        // private const val FTDI_SIO_SET_BREAK_ON = 1 shl 14

        // private const val FTDI_SIO_SET_BREAK_OFF = 0 shl 14

        /*
        private const val REQUEST_TYPE_IN: Int =
            UsbConstants.USB_TYPE_VENDOR or
                    LIB_USB_RECIPIENT_DEVICE or
                    LIB_USB_ENDPOINT_IN
         */

        private const val SIZE_BUFFER = 4096

        private val COMMAND_INITIALIZE_ECU_16 =
            byteArrayOf(0xCA.toByte(), 0x75.toByte(), 0xD0.toByte())

        private val COMMAND_NOP = byteArrayOf(0xF4.toByte())

        private val COMMAND_REQUEST_DATA_16_7D = byteArrayOf(0x7D.toByte())

        private val COMMAND_REQUEST_DATA_16_80 = byteArrayOf(0x80.toByte())

        private val COMMAND_CLEAR_FAULT_CODES = byteArrayOf(0xCC.toByte())

        private val COMMAND_INCREMENT_IGNITION_TIMING = byteArrayOf(0x93.toByte())

        private val COMMAND_DECREMENT_IGNITION_TIMING = byteArrayOf(0x94.toByte())

        private val COMMAND_INCREMENT_IDLE_SPEED = byteArrayOf(0x91.toByte())

        private val COMMAND_DECREMENT_IDLE_SPEED = byteArrayOf(0x92.toByte())

        private val COMMAND_INCREMENT_IDLE_DECAY = byteArrayOf(0x89.toByte())

        private val COMMAND_DECREMENT_IDLE_DECAY = byteArrayOf(0x8A.toByte())

        private val COMMAND_INCREMENT_FUEL_TRIM = byteArrayOf(0x79.toByte())

        private val COMMAND_DECREMENT_FUEL_TRIM = byteArrayOf(0x7A.toByte())

        private val COMMAND_RESET_TUNING = byteArrayOf(0xFA.toByte())

        private const val DELAY_REQUEST_DATA = 500L

        private val TAG = CommunicationManager::class.java.simpleName
    }

    var mIsConnected = false

    private lateinit var mTuningButtonId: TuningButtonId

    private var mHandler: ConnectionManagerHandler

    private var mDevice: UsbDevice? = null

    private var mFtdiDriver: FtdiDriver? = null

    private val mConnectRunnable = Runnable {
        connect()
    }

    private val mConnect16Runnable = Runnable {
        connect16()
    }

    private val mDisconnectRunnable = Runnable {
        disconnect()
    }

    private val mClearFaultCodesRunnable = Runnable {
        clearFaultCodes()
    }

    private val mTuningRunnable = Runnable {
        tune()
    }

    private val mRequestData16Runnable = object : Runnable {
        override fun run() {
            requestData16()
            if (mIsConnected) {
                mHandler.postDelayed(this, DELAY_REQUEST_DATA)
            }
        }
    }

    init {
        HandlerThread(TAG).run {
            start()
            mHandler = ConnectionManagerHandler(looper)
        }
    }

    fun postMessage(what: Int = -1, arg1: Int = 0, arg2: Int = 0, obj: Any? = null) {
        mHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget()
    }

    private fun connect() {
        val device = mDevice ?: return
        val transceiver = UsbTransceiver.create(mUsbManager, device) ?: run {
            disconnect()
            return
        }
        mFtdiDriver = FtdiDriver(transceiver)
        if (mFtdiDriver?.initialize() == false) {
            disconnect()
            return
        }

        mHandler.post(mConnect16Runnable)
    }

    private fun disconnect() {
        mFtdiDriver?.close()
        mFtdiDriver = null
        disconnected()
    }

    private fun connect16() {
        val readBuffer = ByteArray(SIZE_BUFFER)

        if (!sendCommand(COMMAND_INITIALIZE_ECU_16, readBuffer)) {
            return
        }

        for (i in readBuffer.indices) {
            readBuffer[i] = 0
        }

        if (!sendCommand(COMMAND_NOP, readBuffer)) {
            return
        }

        connected()
    }

    private fun requestData16() {
        val buffer80 = ByteArray(SIZE_BUFFER)
        if (!sendCommand(COMMAND_REQUEST_DATA_16_80, buffer80)) {
            return
        }

        val buffer7D = ByteArray(SIZE_BUFFER)
        if (!sendCommand(COMMAND_REQUEST_DATA_16_7D, buffer7D)) {
            return
        }

        mMainActivity.postMessage(
            what = MainActivity.Companion.MessageId.LIVE_DATA.ordinal,
            obj = DataPacket(buffer80, buffer7D)
        )
    }

    private fun clearFaultCodes() {
        val readBuffer = ByteArray(SIZE_BUFFER)
        if (!sendCommand(COMMAND_CLEAR_FAULT_CODES, readBuffer)) {
            return
        }
        mMainActivity.postMessage(MainActivity.Companion.MessageId.FAULT_CODES.ordinal)
    }

    private fun tune() {
        val command = when (mTuningButtonId) {

            // Ignition Timing
            TuningButtonId.INCREMENT_IGNITION_TIMING -> COMMAND_INCREMENT_IGNITION_TIMING
            TuningButtonId.DECREMENT_IGNITION_TIMING -> COMMAND_DECREMENT_IGNITION_TIMING

            // Idle Speed
            TuningButtonId.INCREMENT_IDLE_SPEED -> COMMAND_INCREMENT_IDLE_SPEED
            TuningButtonId.DECREMENT_IDLE_SPEED -> COMMAND_DECREMENT_IDLE_SPEED

            // Idle Decay
            TuningButtonId.INCREMENT_IDLE_DECAY -> COMMAND_INCREMENT_IDLE_DECAY
            TuningButtonId.DECREMENT_IDLE_DECAY -> COMMAND_DECREMENT_IDLE_DECAY

            // Fuel Trim
            TuningButtonId.INCREMENT_FUEL_TRIM -> COMMAND_INCREMENT_FUEL_TRIM
            TuningButtonId.DECREMENT_FUEL_TRIM -> COMMAND_DECREMENT_FUEL_TRIM

            // Reset
            TuningButtonId.RESET_TUNING -> COMMAND_RESET_TUNING
        }

        val readBuffer = ByteArray(SIZE_BUFFER)

        if (!sendCommand(command, readBuffer)) {
            return
        }

        mMainActivity.postMessage(
            what = MainActivity.Companion.MessageId.TUNING.ordinal,
            obj = Pair(mTuningButtonId, readBuffer[2].toHexStringRmd())
        )
    }

    private fun sendCommand(command: ByteArray, readBuffer: ByteArray): Boolean {
        val driver = mFtdiDriver ?: return false
        command.forEach { c ->
            if (!driver.write(byteArrayOf(c))) {
                postMessage(what = MessageId.DISCONNECT.ordinal)
                return false
            }
            if (!driver.read(readBuffer)) {
                postMessage(what = MessageId.DISCONNECT.ordinal)
                return false
            }
        }
        return true
    }

    private fun connected() {
        mIsConnected = true
        mMainActivity.postMessage(MainActivity.Companion.MessageId.CONNECTED.ordinal)
        mHandler.post(mRequestData16Runnable)
    }

    private fun disconnected() {
        mIsConnected = false
        mMainActivity.postMessage(MainActivity.Companion.MessageId.DISCONNECTED.ordinal)
    }

    private inner class ConnectionManagerHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(message: Message) {
            when (message.what) {
                MessageId.CONNECT.ordinal -> {
                    mDevice = message.obj as UsbDevice?
                    mHandler.post(mConnectRunnable)
                }

                MessageId.DISCONNECT.ordinal -> {
                    mHandler.removeCallbacksAndMessages(null)
                    mHandler.post(mDisconnectRunnable)
                }

                MessageId.CLEAR_FAULT_CODES.ordinal -> {
                    mHandler.post(mClearFaultCodesRunnable)
                }

                MessageId.PERFORM_TUNING.ordinal -> {
                    mTuningButtonId = message.obj as TuningButtonId
                    mHandler.post(mTuningRunnable)
                }
            }
        }
    }
}