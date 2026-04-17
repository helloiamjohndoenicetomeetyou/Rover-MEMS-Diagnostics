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

data class RoverMemsDiagnosticsState(
    val engineSpeed: String = "-",
    val idleSpeedDeviation: String = "-",
    val idleSwitch: String = "-",
    val throttlePotentiometerVoltage: String = "-",
    val idleAirControlMotorPosition: String = "-",
    val manifoldAbsolutePressure: String = "-",
    val batteryVoltage: String = "-",
    val waterTemperature: String = "-",
    val intakeAirTemperature: String = "-",
    val neutralSwitch: String = "-",
    val oxygenSensorVoltage: String = "-",
    val fuelTrimLoopOperation: String = "-",
    val shortTermFuelTrim: String = "-",

    val coolerSwitch: String = "-",
    val idleSetPoint: String = "-",
    val hotIdlePosition: String = "-",
    val idleBasePosition: String = "-",
    val ignitionTimingOffset: String = "-",
    val ignitionTiming: String = "-",
    val ignitionCoilDwellTime: String = "-",

    val crankshaftAngleSensorFault: String = "-",
    val throttlePotentiometerFault: String = "-",
    val manifoldAbsolutePressureSensorFault: String = "-",
    val waterTemperatureSensorFault: String = "-",
    val intakeAirTemperatureSensorFault: String = "-",

    val tuningIgnitionTiming: String = "-",
    val tuningIdleSpeed: String = "-",
    val tuningIdleDecay: String = "-",
    val tuningFuelTrim: String = "-"
)