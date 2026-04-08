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
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.protocol.Mems16Protocol
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

        private const val DELAY_REQUEST_DATA = 500L

        private val TAG = CommunicationManager::class.java.simpleName
    }

    var mIsConnected = false

    private lateinit var mTuningButtonId: TuningButtonId

    private var mHandler: ConnectionManagerHandler

    private var mDevice: UsbDevice? = null

    private var mMems16Protocol: Mems16Protocol? = null

    private val mConnectRunnable = Runnable {
        connect()
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
            postMessage(what = MessageId.DISCONNECT.ordinal)
            return
        }

        val driver = FtdiDriver(transceiver)
        if (!driver.initialize()) {
            postMessage(what = MessageId.DISCONNECT.ordinal)
            return
        }

        mMems16Protocol = Mems16Protocol(driver)
        if (mMems16Protocol?.initialize() == false) {
            postMessage(what = MessageId.DISCONNECT.ordinal)
            return
        }

        connected()
    }

    private fun disconnect() {
        mMems16Protocol?.close()
        mMems16Protocol = null
        disconnected()
    }

    private fun requestData16() {
        val mems16Protocol = mMems16Protocol ?: run {
            postMessage(what = MessageId.DISCONNECT.ordinal)
            return
        }

        val dataPacket = mems16Protocol.requestLiveData() ?: run {
            postMessage(what = MessageId.DISCONNECT.ordinal)
            return
        }

        mMainActivity.postMessage(
            what = MainActivity.Companion.MessageId.LIVE_DATA.ordinal,
            obj = dataPacket
        )
    }

    private fun clearFaultCodes() {
        val mems16Protocol = mMems16Protocol ?: run {
            postMessage(what = MessageId.DISCONNECT.ordinal)
            return
        }

        if (!mems16Protocol.clearFaultCodes()) {
            postMessage(what = MessageId.DISCONNECT.ordinal)
            return
        }

        mMainActivity.postMessage(MainActivity.Companion.MessageId.FAULT_CODES.ordinal)
    }

    private fun tune() {
        val mems16Protocol = mMems16Protocol ?: run {
            postMessage(what = MessageId.DISCONNECT.ordinal)
            return
        }

        val newValue = mems16Protocol.performTuning(mTuningButtonId) ?: run {
            postMessage(what = MessageId.DISCONNECT.ordinal)
            return
        }

        mMainActivity.postMessage(
            what = MainActivity.Companion.MessageId.TUNING.ordinal,
            obj = Pair(mTuningButtonId, newValue)
        )
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