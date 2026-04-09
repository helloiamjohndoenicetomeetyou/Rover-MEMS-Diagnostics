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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningButtonId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RmdAppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RmdAppState())

    val uiState: StateFlow<RmdAppState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<String>()

    val uiEvent = _uiEvent.receiveAsFlow()

    private val _isConnected = MutableStateFlow(false)

    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun setIsConnected(isConnected: Boolean) {
        _isConnected.value = isConnected
    }

    var onConnectRequested: (() -> Unit)? = null

    fun requestConnect() {
        onConnectRequested?.invoke()
    }

    var onDisconnectRequested: (() -> Unit)? = null

    fun requestDisconnect() {
        onDisconnectRequested?.invoke()
    }

    var onClearFaultCodesRequested: (() -> Unit)? = null

    fun requestClearFaultCodes() {
        onClearFaultCodesRequested?.invoke()
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

    var onPerformTuningRequested: ((TuningButtonId) -> Unit)? = null

    fun performTuning(buttonId: TuningButtonId) {
        onPerformTuningRequested?.invoke(buttonId)
    }

    /**
     * Called when the tuning value is changed.
     * @param buttonId The ID of the Button that was clicked.
     * @param value The new value of the Tuning.
     */
    fun onTuningValueChanged(buttonId: TuningButtonId, value: String) {
        _uiState.update {
            when (buttonId) {
                TuningButtonId.INCREMENT_IGNITION_TIMING,
                TuningButtonId.DECREMENT_IGNITION_TIMING ->
                    it.copy(tuningIgnitionTiming = value)

                TuningButtonId.INCREMENT_IDLE_SPEED,
                TuningButtonId.DECREMENT_IDLE_SPEED ->
                    it.copy(tuningIdleSpeed = value)

                TuningButtonId.INCREMENT_IDLE_DECAY,
                TuningButtonId.DECREMENT_IDLE_DECAY ->
                    it.copy(tuningIdleDecay = value)

                TuningButtonId.INCREMENT_FUEL_TRIM,
                TuningButtonId.DECREMENT_FUEL_TRIM ->
                    it.copy(tuningFuelTrim = value)

                TuningButtonId.RESET_TUNING ->
                    it.copy(
                        tuningIgnitionTiming = "-",
                        tuningIdleSpeed = "-",
                        tuningIdleDecay = "-",
                        tuningFuelTrim = "-"
                    )
            }
        }

        if (buttonId == TuningButtonId.RESET_TUNING) {
            showSnackbar("Resetting tuning successfully completed.")
        }
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(message)
        }
    }
}