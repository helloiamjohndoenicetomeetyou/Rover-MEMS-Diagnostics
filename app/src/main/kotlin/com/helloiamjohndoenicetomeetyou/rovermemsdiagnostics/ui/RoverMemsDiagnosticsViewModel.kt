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
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.R
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.CommunicationManager
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningButtonId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoverMemsDiagnosticsViewModel(application: Application) :
    AndroidViewModel(application = application) {
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

    private val _uiState = MutableStateFlow(RoverMemsDiagnosticsState())

    val uiState: StateFlow<RoverMemsDiagnosticsState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<String>()

    val uiEvent = _uiEvent.receiveAsFlow()

    private val _isConnected = MutableStateFlow(false)

    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _requestConnectEvent = MutableSharedFlow<Unit>()

    val requestConnectEvent: SharedFlow<Unit> = _requestConnectEvent.asSharedFlow()

    override fun onCleared() {
        super.onCleared()

        communicationManager.release()
    }

    fun setIsConnected(newStatus: Boolean) {
        _isConnected.update {
            newStatus
        }

        if (newStatus) {
            _uiState.value = RoverMemsDiagnosticsState()
        }
    }

    fun requestConnect() {
        viewModelScope.launch {
            _requestConnectEvent.emit(Unit)
        }
    }

    fun connect(device: UsbDevice?) {
        communicationManager.connect(device)
    }

    /**
     * Called when LiveData and FaultCodes are received.
     */
    fun onLiveDataReceived(data: DataPacket) {
        _uiState.update {
            it.copy(
                engineSpeed = data.engineSpeed,
                idleSpeedDeviation = data.idleSpeedDeviation,
                idleSwitch = getStatusText(data.idleSwitch),
                throttlePotentiometerVoltage = data.throttlePotentiometerVoltage,
                idleAirControlMotorPosition = data.idleAirControlMotorPosition,
                manifoldAbsolutePressure = data.manifoldAbsolutePressure,
                batteryVoltage = data.batteryVoltage,
                waterTemperature = data.waterTemperature,
                intakeAirTemperature = data.intakeAirTemperature,
                neutralSwitch = getStatusText(data.neutralSwitch),
                oxygenSensorVoltage = data.oxygenSensorVoltage,
                fuelTrimLoopOperation = getStatusText(data.fuelTrimLoopOperation),
                shortTermFuelTrim = data.shortTermFuelTrim,

                coolerSwitch = getStatusText(data.coolerSwitch),
                idleSetPoint = data.idleSetPoint,
                hotIdlePosition = data.hotIdlePosition,
                idleBasePosition = data.idleBasePosition,
                ignitionTimingOffset = data.ignitionTimingOffset,
                ignitionTiming = data.ignitionTiming,
                ignitionCoilDwellTime = data.ignitionCoilDwellTime,

                crankshaftAngleSensorFault = getStatusText(data.crankshaftAngleSensorFault),
                throttlePotentiometerFault = getStatusText(data.throttlePotentiometerFault),
                manifoldAbsolutePressureSensorFault = getStatusText(data.manifoldAbsolutePressureSensorFault),
                waterTemperatureSensorFault = getStatusText(data.waterTemperatureSensorFault),
                intakeAirTemperatureSensorFault = getStatusText(data.intakeAirTemperatureSensorFault)
            )
        }
    }

    fun requestClearFaultCodes() {
        communicationManager.clearFaultCodes()
    }

    fun onFaultCodesCleared(result: Boolean) {
        if (result) {
            showSnackbar(application.getString(R.string.cleared_fault_codes))
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
                        tuningIgnitionTiming = application.getString(R.string.hyphen),
                        tuningIdleSpeed = application.getString(R.string.hyphen),
                        tuningIdleDecay = application.getString(R.string.hyphen),
                        tuningFuelTrim = application.getString(R.string.hyphen)
                    )
            }
        }

        if (data.tuningButtonId == TuningButtonId.RESET_TUNING) {
            showSnackbar(application.getString(R.string.reset_tuning_successfully))
        }
    }

    fun disconnect() {
        communicationManager.disconnect()
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(message)
        }
    }

    private fun getStatusText(status: String): String = when (status) {
        DataPacket.OPEN -> application.getString(R.string.status_open)
        DataPacket.CLOSED -> application.getString(R.string.status_closed)
        DataPacket.ON -> application.getString(R.string.status_on)
        DataPacket.OFF -> application.getString(R.string.status_off)
        DataPacket.FAULT -> application.getString(R.string.status_fault)
        DataPacket.NO_FAULT -> application.getString(R.string.status_no_fault)
        else -> status
    }
}