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

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.RmdApp
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.RmdAppViewModel
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.components.NotSupportedDialog
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningButtonId
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    companion object {
        enum class MessageId {
            HOME,
            CONNECTED,
            DISCONNECTED,
            LIVE_DATA,
            FAULT_CODES,
            TUNING
        }

        private const val VENDOR_ID_FTDI = 0x0403

        private const val PRODUCT_ID_FTDI_FT232R = 0x6001

        private const val ACTION_USB_PERMISSION =
            "com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.USB_PERMISSION"

        private class MainActivityHandler(looper: Looper, mainActivity: MainActivity) :
            Handler(looper) {
            private val mMainActivityWeakReference = WeakReference(mainActivity)

            override fun handleMessage(message: Message) {
                val mainActivity = mMainActivityWeakReference.get() ?: return
                when (message.what) {
                    MessageId.HOME.ordinal -> {
                        // Nothing to do.
                    }

                    MessageId.CONNECTED.ordinal -> {
                        mainActivity.viewModel.setIsConnected(true)
                    }

                    MessageId.DISCONNECTED.ordinal -> {
                        mainActivity.viewModel.setIsConnected(false)
                    }

                    MessageId.LIVE_DATA.ordinal -> {
                        mainActivity.updateLiveData(message.obj)
                    }

                    MessageId.FAULT_CODES.ordinal -> {
                        mainActivity.showSnackBar("Clearing fault codes successfully completed.")
                    }

                    MessageId.TUNING.ordinal -> {
                        mainActivity.updateTuning(message.obj)
                    }
                }
            }
        }
    }

    private lateinit var mUsbManager: UsbManager

    private lateinit var mHandler: MainActivityHandler

    private lateinit var mCommunicationManager: CommunicationManager

    private var mDevice: UsbDevice? = null

    private val viewModel: RmdAppViewModel by viewModels()

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                synchronized(this@MainActivity) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        showSnackBar("USB Permission granted.")
                        connect()
                    } else {
                        showSnackBar("USB Permission denied.")
                        return
                    }
                }
            }

            if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                requestUsbPermission()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isUsbHostSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)

        mUsbManager = getSystemService(USB_SERVICE) as UsbManager

        mHandler = MainActivityHandler(Looper.getMainLooper(), this)

        mCommunicationManager = CommunicationManager(this, mUsbManager)

        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_USB_PERMISSION)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)

        ContextCompat.registerReceiver(
            this,
            mBroadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                RmdApp(viewModel = viewModel)

                if (!isUsbHostSupported) {
                    NotSupportedDialog(
                        onConfirm = {
                            finishAndRemoveTask()
                        }
                    )
                }
            }
        }

        viewModel.onConnectRequested = {
            // Get USB permission before connecting.
            requestUsbPermission()
        }

        viewModel.onDisconnectRequested = {
            mCommunicationManager.postMessage(
                what = CommunicationManager.Companion.MessageId.DISCONNECT.ordinal
            )
        }

        viewModel.onClearFaultCodesRequested = {
            mCommunicationManager.postMessage(
                what = CommunicationManager.Companion.MessageId.CLEAR_FAULT_CODES.ordinal
            )
        }

        viewModel.onPerformTuningRequested = { buttonId ->
            mCommunicationManager.postMessage(
                what = CommunicationManager.Companion.MessageId.PERFORM_TUNING.ordinal,
                obj = buttonId
            )
        }
    }

    override fun onDestroy() {
        unregisterReceiver(mBroadcastReceiver)

        super.onDestroy()
    }

    fun postMessage(what: Int = -1, arg1: Int = 0, arg2: Int = 0, obj: Any? = null) {
        mHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget()
    }

    /**
     * Prompts for USB permission before connecting.
     */
    private fun requestUsbPermission() {
        val list = mUsbManager.deviceList ?: run {
            disconnected()
            return
        }

        if (list.size != 1) {
            disconnected()
            return
        }

        val device = list.values.toList()[0]

        if ((device.vendorId != VENDOR_ID_FTDI) && (device.productId != PRODUCT_ID_FTDI_FT232R)) {
            disconnected()
            return
        }

        mDevice = device

        val permissionIntent = Intent(ACTION_USB_PERMISSION).apply {
            `package` = packageName
        }

        val flags = if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(this, 0, permissionIntent, flags)
        mUsbManager.requestPermission(device, pendingIntent)
    }

    /**
     * Attempts to connect to the ECU once USB permission is granted.
     * @see mBroadcastReceiver
     */
    private fun connect() {
        mCommunicationManager.postMessage(
            what = CommunicationManager.Companion.MessageId.CONNECT.ordinal,
            obj = mDevice
        )
    }

    /**
     * Sets the UI to disconnected state.
     */
    private fun disconnected() {
        postMessage(what = MessageId.DISCONNECTED.ordinal)
    }

    /**
     * Updates the UI, Live Data Section and Fault Codes Section.
     */
    private fun updateLiveData(obj: Any?) {
        (obj as? DataPacket)?.let { dataPacket ->
            viewModel.onLiveDataReceived(dataPacket)
        }
    }

    private fun updateTuning(obj: Any?) {
        (obj as? Pair<*, *>)?.run {
            val buttonId = first as? TuningButtonId ?: return
            val value = second as? String ?: return
            viewModel.onTuningValueChanged(buttonId, value)
        }
    }

    /**
     * Shows a SnackBar. (Replacement for Toast)
     */
    private fun showSnackBar(text: String) {
        viewModel.showSnackbar(text)
    }
}