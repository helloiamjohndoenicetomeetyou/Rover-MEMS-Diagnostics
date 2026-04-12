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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.RmdApp
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.RmdAppViewModel
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.components.NotSupportedDialog
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        private const val VENDOR_ID_FTDI = 0x0403

        private const val PRODUCT_ID_FTDI_FT232R = 0x6001

        private const val ACTION_USB_PERMISSION: String =
            BuildConfig.APPLICATION_ID + "USB_PERMISSION"
    }

    private val viewModel: RmdAppViewModel by viewModels()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    synchronized(this@MainActivity) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            showSnackBar("USB Permission granted.")

                            val device = IntentCompat.getParcelableExtra(
                                intent,
                                UsbManager.EXTRA_DEVICE,
                                UsbDevice::class.java
                            )
                            connect(device)
                        } else {
                            showSnackBar("USB Permission denied.")
                            return
                        }
                    }
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    requestUsbPermission()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isUsbHostSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)

        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_USB_PERMISSION)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)

        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.requestConnectEvent.collect {
                    requestUsbPermission()
                }
            }
        }

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
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)

        super.onDestroy()
    }

    /**
     * Prompts for USB permission before connecting.
     */
    private fun requestUsbPermission() {
        val usbManager = getSystemService(USB_SERVICE) as UsbManager

        val device = usbManager.deviceList.values.firstOrNull { device ->
            device.vendorId == VENDOR_ID_FTDI && device.productId == PRODUCT_ID_FTDI_FT232R
        }

        device ?: run {
            disconnected()
            return
        }

        val permissionIntent = Intent(ACTION_USB_PERMISSION).apply {
            `package` = packageName
        }

        val flags = if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(this, 0, permissionIntent, flags)
        usbManager.requestPermission(device, pendingIntent)
    }

    /**
     * Attempts to connect to the ECU once USB permission is granted.
     * @see broadcastReceiver
     */
    private fun connect(device: UsbDevice?) {
        viewModel.connect(device)
    }

    /**
     * Sets the UI to disconnected state.
     */
    private fun disconnected() {
        viewModel.setIsConnected(false)
    }

    /**
     * Shows a SnackBar. (Replacement for Toast)
     */
    private fun showSnackBar(text: String) {
        viewModel.showSnackbar(text)
    }
}