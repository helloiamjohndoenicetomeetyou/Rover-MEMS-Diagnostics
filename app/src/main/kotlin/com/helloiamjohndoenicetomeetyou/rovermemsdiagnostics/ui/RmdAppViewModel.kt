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

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui

import android.app.Application
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.CommunicationManager
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningButtonId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RmdAppViewModel(application: Application) : AndroidViewModel(application = application) {
    private val usbManager: UsbManager =
        application.getSystemService(Context.USB_SERVICE) as UsbManager

    private val communicationManager = CommunicationManager(
        usbManager = usbManager,
        onStatusChanged = { state ->
            setIsConnected(state)
        },
        onLiveDataReceived = { data ->
            onLiveDataReceived(data)
        },
        onFaultCodesCleared = { result ->
            onFaultCodesCleared(result)
        },
        onTuningPerformed = { data ->
            onTuningPerformed(data)
        }
    )

    private val _uiState = MutableStateFlow(RmdAppState())

    val uiState: StateFlow<RmdAppState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<String>()

    val uiEvent = _uiEvent.receiveAsFlow()

    private val _isConnected = MutableStateFlow(false)

    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    override fun onCleared() {
        super.onCleared()

        communicationManager.release()
    }

    fun setIsConnected(isConnected: Boolean) {
        _isConnected.value = isConnected
    }

    var onConnectRequested: (() -> Unit)? = null

    fun requestConnect() {
        onConnectRequested?.invoke()
    }

    fun connect(device: UsbDevice?) {
        communicationManager.connect(device)
    }

    fun disconnect() {
        communicationManager.disconnect()
    }

    fun requestClearFaultCodes() {
        communicationManager.clearFaultCodes()
    }

    /**
     * Called when LiveData and FaultCodes are received.
     */
    fun onLiveDataReceived(data: DataPacket) {
        _uiState.update {
            it.copy(
                engineSpeed = data.engineSpeed,
                idleSpeedDeviation = data.idleSpeedDeviation,
                idleSwitch = data.idleSwitch,
                throttlePotentiometerVoltage = data.throttlePotentiometerVoltage,
                idleAirControlMotorPosition = data.idleAirControlMotorPosition,
                manifoldAbsolutePressure = data.manifoldAbsolutePressure,
                batteryVoltage = data.batteryVoltage,
                waterTemperature = data.waterTemperature,
                intakeAirTemperature = data.intakeAirTemperature,
                neutralSwitch = data.neutralSwitch,
                oxygenSensorVoltage = data.oxygenSensorVoltage,
                fuelTrimLoopOperation = data.fuelTrimLoopOperation,
                shortTermFuelTrim = data.shortTermFuelTrim,

                coolerSwitch = data.coolerSwitch,
                idleSetPoint = data.idleSetPoint,
                hotIdlePosition = data.hotIdlePosition,
                idleBasePosition = data.idleBasePosition,
                ignitionTimingOffset = data.ignitionTimingOffset,
                ignitionTiming = data.ignitionTiming,
                ignitionCoilDwellTime = data.ignitionCoilDwellTime,

                crankshaftAngleSensorFault = data.crankshaftAngleSensorFault,
                throttlePotentiometerFault = data.throttlePotentiometerFault,
                manifoldAbsolutePressureSensorFault = data.manifoldAbsolutePressureSensorFault,
                waterTemperatureSensorFault = data.waterTemperatureSensorFault,
                intakeAirTemperatureSensorFault = data.intakeAirTemperatureSensorFault
            )
        }
    }

    fun onFaultCodesCleared(result: Boolean) {
        if (result) {
            showSnackbar("Fault codes cleared successfully.")
        }
    }

    fun performTuning(buttonId: TuningButtonId) {
        communicationManager.performTuning(buttonId)
    }

    /**
     * Called when the tuning value is changed.
     * @param data A [DataPacket] containing the tuning value.
     */
    fun onTuningPerformed(data: DataPacket) {
        _uiState.update {
            when (data.tuningButtonId) {
                TuningButtonId.INCREMENT_IGNITION_TIMING,
                TuningButtonId.DECREMENT_IGNITION_TIMING ->
                    it.copy(tuningIgnitionTiming = data.tunedValue)

                TuningButtonId.INCREMENT_IDLE_SPEED,
                TuningButtonId.DECREMENT_IDLE_SPEED ->
                    it.copy(tuningIdleSpeed = data.tunedValue)

                TuningButtonId.INCREMENT_IDLE_DECAY,
                TuningButtonId.DECREMENT_IDLE_DECAY ->
                    it.copy(tuningIdleDecay = data.tunedValue)

                TuningButtonId.INCREMENT_FUEL_TRIM,
                TuningButtonId.DECREMENT_FUEL_TRIM ->
                    it.copy(tuningFuelTrim = data.tunedValue)

                TuningButtonId.RESET_TUNING ->
                    it.copy(
                        tuningIgnitionTiming = "-",
                        tuningIdleSpeed = "-",
                        tuningIdleDecay = "-",
                        tuningFuelTrim = "-"
                    )
            }
        }

        if (data.tuningButtonId == TuningButtonId.RESET_TUNING) {
            showSnackbar("Resetting tuning successfully completed.")
        }
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(message)
        }
    }
}