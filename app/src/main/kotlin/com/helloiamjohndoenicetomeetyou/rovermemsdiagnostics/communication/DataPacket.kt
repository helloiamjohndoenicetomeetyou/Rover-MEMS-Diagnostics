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

import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningButtonId

class DataPacket(
    val engineSpeed: String = "",

    val waterTemperature: String = "",

    val intakeAirTemperature: String = "",

    val manifoldAbsolutePressure: String = "",

    val batteryVoltage: String = "",

    val throttlePotentiometerVoltage: String = "",

    val idleSwitch: String = "",

    val neutralSwitch: String = "",

    val coolerSwitch: String = "",

    val waterTemperatureSensorFault: String = "",

    val intakeAirTemperatureSensorFault: String = "",

    val crankshaftAngleSensorFault: String = "",

    val fuelPumpCircuitFault: String = "",

    val purgeValveFault: String = "",

    val manifoldAbsolutePressureSensorFault: String = "",

    val throttlePotentiometerFault: String = "",

    val idleSetPoint: String = "",

    val hotIdlePosition: String = "",

    val idleAirControlMotorPosition: String = "",

    val idleSpeedDeviation: String = "",

    val ignitionTimingOffset: String = "",

    val ignitionTiming: String = "",

    val ignitionCoilDwellTime: String = "",

    val ignitionSwitch: String = "",

    val throttleAngle: String = "",

    val airFuelRatio: String = "",

    val oxygenSensorVoltage: String = "",

    val fuelTrimLoopOperation: String = "",

    val shortTermFuelTrim: String = "",

    val idleBasePosition: String = "",

    val tuningButtonId: TuningButtonId = TuningButtonId.RESET_TUNING,

    val tunedValue: String = ""
) {
    companion object {
        const val IDLE = "Idle"

        const val NOT_IDLE = "Not Idle"

        const val OPENED = "Opened"

        const val CLOSED = "Closed"

        const val ON = "ON"

        const val OFF = "OFF"

        const val FAULT = "Fault"

        const val NO_FAULT = "No Fault"

        const val NEUTRAL = "Neutral"

        const val GEARED = "Geared"
    }
}