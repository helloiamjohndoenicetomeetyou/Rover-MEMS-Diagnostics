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

class DataPacket() {
    companion object {
        private const val IDLE = "Idle"

        private const val NOT_IDLE = "Not Idle"

        private const val OPENED = "Opened"

        private const val CLOSED = "Closed"

        private const val ON = "ON"

        private const val OFF = "OFF"

        private const val FAULT = "Fault"

        private const val NO_FAULT = "No Fault"

        private const val NEUTRAL = "Neutral"

        private const val NOT_NEUTRAL = "Geared"
    }

    lateinit var engineSpeed: String

    lateinit var waterTemperature: String

    lateinit var intakeAirTemperature: String

    lateinit var manifoldAbsolutePressure: String

    lateinit var batteryVoltage: String

    lateinit var throttlePotentiometerVoltage: String

    lateinit var idleSwitch: String

    lateinit var neutralSwitch: String

    lateinit var coolerSwitch: String

    lateinit var waterTemperatureSensorFault: String

    lateinit var intakeAirTemperatureSensorFault: String

    lateinit var crankshaftAngleSensorFault: String

    lateinit var fuelPumpCircuitFault: String

    lateinit var purgeValveFault: String

    lateinit var manifoldAbsolutePressureSensorFault: String

    lateinit var throttlePotentiometerFault: String

    lateinit var idleSetPoint: String

    lateinit var hotIdlePosition: String

    lateinit var idleAirControlMotorPosition: String

    lateinit var idleSpeedDeviation: String

    lateinit var ignitionTimingOffset: String

    lateinit var ignitionTiming: String

    lateinit var ignitionCoilDwellTime: String

    lateinit var ignitionSwitch: String

    lateinit var throttleAngle: String

    lateinit var airFuelRatio: String

    lateinit var oxygenSensorVoltage: String

    lateinit var fuelTrimLoopOperation: String

    lateinit var shortTermFuelTrim: String

    lateinit var idleBasePosition: String

    constructor(buffer80: ByteArray, buffer7D: ByteArray) : this() {

        ////
        // Data Frame 0x80
        ////
        val int80 = IntArray(buffer80.size)

        // buffer80[0]: Total read size
        // buffer80[1]: Echo
        // buffer80[2]: Size of Data Frame 0x80
        for (i in 0..buffer80[2]) {
            int80[i] = (buffer80[i + 2].toInt() and 0xFF)
        }

        // int80: 0 0x00: Size of Data Frame 0x80

        // int80: 1 0x01
        // int80: 2 0x02: Engine Speed
        engineSpeed = ((int80[1] shl 8) + int80[2]).toString()

        // int80: 3 0x03: Water Temperature
        waterTemperature = (int80[3] - 55).toString()

        // int80: 4 0x04: Ambient Temperature

        // int80: 5 0x05: Intake Air Temperature
        intakeAirTemperature = (int80[5] - 55).toString()

        // int80: 6 0x06: Fuel Temperature

        // int80: 7 0x07: Manifold Absolute Pressure
        manifoldAbsolutePressure = int80[7].toString()

        // int80: 8 0x08: Battery Voltage
        batteryVoltage = "%.1f".format(int80[8] * 0.1)

        // int80: 9 0x09: Throttle Potentiometer Voltage
        throttlePotentiometerVoltage = "%.2f".format(int80[9] * 0.02)

        // int80: 10 0x0A: Idle Switch
        idleSwitch = if (int80[10] == 0) IDLE else NOT_IDLE

        // int80: 11 0x0B: Neutral Switch
        neutralSwitch = if (int80[11] == 0) NEUTRAL else NOT_NEUTRAL

        // int80: 12 0x0C: Cooler Switch
        coolerSwitch = if (int80[12] == 0) ON else OFF

        // int80: 13 0x0D: Bit 0: Water Temperature Sensor Fault
        waterTemperatureSensorFault = if ((int80[13] and 0x01) != 0) FAULT else NO_FAULT

        // int80: 13 0x0D: Bit 1: Intake Air Temperature Sensor Fault
        intakeAirTemperatureSensorFault = if ((int80[13] and 0x02) != 0) FAULT else NO_FAULT

        // int80: 14 0x0E: Bit 1: Fuel Pump Circuit Fault
        fuelPumpCircuitFault = if ((int80[14] and 0x02) != 0) FAULT else NO_FAULT

        // int80: 14 0x0E: Bit 4: Purge Valve Fault
        purgeValveFault = if ((int80[14] and 0x10) != 0) FAULT else NO_FAULT

        // int80: 14 0x0E: Bit 5: Manifold Absolute Pressure Sensor Fault
        manifoldAbsolutePressureSensorFault = if ((int80[14] and 0x20) != 0) FAULT else NO_FAULT

        // int80: 14 0x0E: Bit 7: Throttle Potentiometer Fault
        throttlePotentiometerFault = if ((int80[14] and 0x80) != 0) FAULT else NO_FAULT

        // int80: 15 0x0F: Idle Set Point
        idleSetPoint = (int80[15] * 6.1).toString()

        // int80: 16 0x10: Hot Idle Position
        hotIdlePosition = (int80[16] - 35).toString()

        // int80: 17 0x11: Unknown

        // int80: 18 0x12: Idle Air Control Motor Position
        idleAirControlMotorPosition = int80[18].toString()

        // int80: 19 0x13
        // int80: 20 0x14: Idle Speed Deviation
        idleSpeedDeviation = ((int80[19] shl 8) + int80[20]).toString()

        // int80: 21 0x15: Ignition Timing Offset
        ignitionTimingOffset = int80[21].toString()

        // int80: 22 0x16: Ignition Timing
        ignitionTiming = "%.1f".format(int80[22] * 0.5 - 24)

        // int80: 23 0x17
        // int80: 24 0x18: Ignition Coil Dwell Time
        ignitionCoilDwellTime = (((int80[23] shl 8) + int80[24]) / 2).toString()

        // int80: 25 0x19: Unknown

        // int80: 26 0x20: Unknown

        // int80: 27 0x21: Unknown

        ////
        // Data Frame 0x7D
        ////
        val int7D = IntArray(buffer7D.size)

        // buffer7D[0]: Total read size
        // buffer7D[1]: Echo
        // buffer7D[2]: Size of Data Frame 0x7D
        for (i in 0..buffer7D[2]) {
            int7D[i] = (buffer7D[i + 2].toInt() and 0xFF)
        }

        // int7D: 0 0x00: Size of Data Frame 0x7D

        // int7D: 1 0x01: Ignition Switch
        ignitionSwitch = if (int7D[1] == 0) OFF else ON

        // int7D: 2 0x02: Throttle Angle
        throttleAngle = (int7D[2] * 0.5).toString()

        // int7D: 3 0x03: Unknown

        // int7D: 4 0x04: Air fuel ratio?
        airFuelRatio = (int7D[4] / 10.0).toString()

        // int7D: 5 0x05: Bit 4: Crank Shaft Angle Sensor Fault
        crankshaftAngleSensorFault = if ((int7D[5] and 0x10) != 0) FAULT else NO_FAULT

        // int7D: 6 0x06: Oxygen Sensor Voltage
        oxygenSensorVoltage = (int7D[6] * 5).toString()

        // int7D: 7 0x07: Oxygen Sensor - 0: OFF Greater: ON

        // int7D: 8 0x08: Oxygen Sensor Duty Cycle?

        // int7D: 9 0x09: Oxygen Sensor Status?

        // int7D: 10 0x10: Fuel Trim Loop Operation
        fuelTrimLoopOperation = if (int7D[10] == 0) OPENED else CLOSED

        // int7D: 11 0x11: Long Term Fuel Trim?

        // int7D: 12 0x12: Short Term Fuel Trim
        shortTermFuelTrim = int7D[12].toString()

        // int7D: 13 0x13: Carbon Canister Purge Valve Duty Cycle?

        // int7D: 14 0x14: DTC?

        // int7D: 15 0x15: Idle Base Position
        idleBasePosition = int7D[15].toString()

        // int7D: 16 0x16: Unknown

        // int7D: 17 0x17: Unknown

        // int7D: 18 0x18: Unknown

        // int7D: 19 0x19: Unknown

        // int7D: 20 0x1A: Unknown

        // int7D: 21 0x1B: Unknown

        // int7D: 22 0x1C: Unknown

        // int7D: 23 0x1D: Unknown

        // int7D: 24 0x1E: Unknown

        // int7D: 25 0x1F: Unknown

        // int7D: 26 0x20: Unknown

        // int7D: 27 0x21: Unknown

        // int7D: 28 0x22: Unknown

        // int7D: 29 0x23: Unknown

        // int7D: 30 0x24: Unknown

        // int7D: 31 0x25: Unknown
    }
}