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

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.protocol

import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.CLOSED
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.FAULT
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.GEARED
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.IDLE
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.NEUTRAL
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.NOT_IDLE
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.NO_FAULT
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.OFF
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.ON
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.DataPacket.Companion.OPENED
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.driver.DeviceDriver
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningButtonId
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.utilities.toHexStringRmd

class Mems16Protocol(private val deviceDriver: DeviceDriver) : MemsProtocol {
    companion object {
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

        private const val SIZE_BUFFER = 1024
    }

    override fun initialize(): Boolean {
        val bytes = ByteArray(SIZE_BUFFER)

        if (!sendCommand(COMMAND_INITIALIZE_ECU_16, bytes)) {
            return false
        }

        if (!sendCommand(COMMAND_NOP, bytes)) {
            return false
        }

        return true
    }

    override fun requestLiveData(): DataPacket? {
        val bytes80 = ByteArray(SIZE_BUFFER)
        if (!sendCommand(COMMAND_REQUEST_DATA_16_80, bytes80)) {
            return null
        }

        val bytes7D = ByteArray(SIZE_BUFFER)
        if (!sendCommand(COMMAND_REQUEST_DATA_16_7D, bytes7D)) {
            return null
        }

        return parseToDataPacket(bytes80 = bytes80, bytes7D = bytes7D)
    }

    override fun clearFaultCodes(): Boolean =
        sendCommand(COMMAND_CLEAR_FAULT_CODES, ByteArray(SIZE_BUFFER))

    override fun performTuning(buttonId: TuningButtonId): DataPacket? {
        val bytes = ByteArray(SIZE_BUFFER)

        if (!sendCommand(getTuningCommand(buttonId.ordinal), bytes)) {
            return null
        }

        return DataPacket(tuningButtonId = buttonId, tunedValue = bytes[2].toHexStringRmd())
    }

    override fun close() = deviceDriver.close()

    private fun sendCommand(command: ByteArray, bytes: ByteArray): Boolean {
        command.forEach { byte ->
            if (!deviceDriver.write(byteArrayOf(byte))) {
                return false
            }

            if (!deviceDriver.read(bytes)) {
                return false
            }
        }

        return true
    }

    private fun parseToDataPacket(bytes80: ByteArray, bytes7D: ByteArray): DataPacket {
        val ints80 = IntArray(bytes80.size)

        // bytes80[0]: Total read size
        // bytes80[1]: Echo
        // bytes80[2]: Size of Data Frame 0x80
        for (i in 0..bytes80[2]) {
            ints80[i] = (bytes80[i + 2].toInt() and 0xFF)
        }

        val ints7D = IntArray(bytes7D.size)

        // bytes7D[0]: Total read size
        // bytes7D[1]: Echo
        // bytes7D[2]: Size of Data Frame 0x7D
        for (i in 0..bytes7D[2]) {
            ints7D[i] = (bytes7D[i + 2].toInt() and 0xFF)
        }

        return DataPacket(
            ////////
            // ints80: 0 0x00: Size of Data Frame 0x80
            ////////

            // ints80: 1 0x01
            // ints80: 2 0x02: Engine Speed
            engineSpeed = ((ints80[1] shl 8) + ints80[2]).toString(),

            // ints80: 3 0x03: Water Temperature
            waterTemperature = (ints80[3] - 55).toString(),

            // ints80: 4 0x04: Ambient Temperature

            // ints80: 5 0x05: Intake Air Temperature
            intakeAirTemperature = (ints80[5] - 55).toString(),

            // ints80: 6 0x06: Fuel Temperature

            // ints80: 7 0x07: Manifold Absolute Pressure
            manifoldAbsolutePressure = ints80[7].toString(),

            // ints80: 8 0x08: Battery Voltage
            batteryVoltage = "%.1f".format(ints80[8] * 0.1),

            // ints80: 9 0x09: Throttle Potentiometer Voltage
            throttlePotentiometerVoltage = "%.2f".format(ints80[9] * 0.02),

            // ints80: 10 0x0A: Idle Switch
            idleSwitch = if (ints80[10] == 0) IDLE else NOT_IDLE,

            // ints80: 11 0x0B: Neutral Switch
            neutralSwitch = if (ints80[11] == 0) NEUTRAL else GEARED,

            // ints80: 12 0x0C: Cooler Switch
            coolerSwitch = if (ints80[12] == 0) ON else OFF,

            // ints80: 13 0x0D: Bit 0: Water Temperature Sensor Fault
            waterTemperatureSensorFault = if ((ints80[13] and 0x01) != 0) FAULT else NO_FAULT,

            // ints80: 13 0x0D: Bit 1: Intake Air Temperature Sensor Fault
            intakeAirTemperatureSensorFault = if ((ints80[13] and 0x02) != 0) FAULT else NO_FAULT,

            // ints80: 14 0x0E: Bit 1: Fuel Pump Circuit Fault
            fuelPumpCircuitFault = if ((ints80[14] and 0x02) != 0) FAULT else NO_FAULT,

            // ints80: 14 0x0E: Bit 4: Purge Valve Fault
            purgeValveFault = if ((ints80[14] and 0x10) != 0) FAULT else NO_FAULT,

            // ints80: 14 0x0E: Bit 5: Manifold Absolute Pressure Sensor Fault
            manifoldAbsolutePressureSensorFault = if ((ints80[14] and 0x20) != 0) FAULT else NO_FAULT,

            // ints80: 14 0x0E: Bit 7: Throttle Potentiometer Fault
            throttlePotentiometerFault = if ((ints80[14] and 0x80) != 0) FAULT else NO_FAULT,

            // ints80: 15 0x0F: Idle Set Point
            idleSetPoint = (ints80[15] * 6.1).toString(),

            // ints80: 16 0x10: Hot Idle Position
            hotIdlePosition = (ints80[16] - 35).toString(),

            // ints80: 17 0x11: Unknown

            // ints80: 18 0x12: Idle Air Control Motor Position
            idleAirControlMotorPosition = ints80[18].toString(),

            // ints80: 19 0x13
            // ints80: 20 0x14: Idle Speed Deviation
            idleSpeedDeviation = ((ints80[19] shl 8) + ints80[20]).toString(),

            // ints80: 21 0x15: Ignition Timing Offset
            ignitionTimingOffset = ints80[21].toString(),

            // ints80: 22 0x16: Ignition Timing
            ignitionTiming = "%.1f".format(ints80[22] * 0.5 - 24),

            // ints80: 23 0x17
            // ints80: 24 0x18: Ignition Coil Dwell Time
            ignitionCoilDwellTime = (((ints80[23] shl 8) + ints80[24]) / 2).toString(),

            // ints80: 25 0x19: Unknown

            // ints80: 26 0x20: Unknown

            // ints80: 27 0x21: Unknown

            ////////
            // ints7D: 0 0x00: Size of Data Frame 0x7D
            ////////

            // ints7D: 1 0x01: Ignition Switch
            ignitionSwitch = if (ints7D[1] == 0) OFF else ON,

            // ints7D: 2 0x02: Throttle Angle
            throttleAngle = (ints7D[2] * 0.5).toString(),

            // ints7D: 3 0x03: Unknown

            // ints7D: 4 0x04: Air fuel ratio?
            airFuelRatio = (ints7D[4] / 10.0).toString(),

            // ints7D: 5 0x05: Bit 4: Crank Shaft Angle Sensor Fault
            crankshaftAngleSensorFault = if ((ints7D[5] and 0x10) != 0) FAULT else NO_FAULT,

            // ints7D: 6 0x06: Oxygen Sensor Voltage
            oxygenSensorVoltage = (ints7D[6] * 5).toString(),

            // ints7D: 7 0x07: Oxygen Sensor - 0: OFF Greater: ON

            // ints7D: 8 0x08: Oxygen Sensor Duty Cycle?

            // ints7D: 9 0x09: Oxygen Sensor Status?

            // ints7D: 10 0x10: Fuel Trim Loop Operation
            fuelTrimLoopOperation = if (ints7D[10] == 0) OPENED else CLOSED,

            // ints7D: 11 0x11: Long Term Fuel Trim?

            // ints7D: 12 0x12: Short Term Fuel Trim
            shortTermFuelTrim = ints7D[12].toString(),

            // ints7D: 13 0x13: Carbon Canister Purge Valve Duty Cycle?

            // ints7D: 14 0x14: DTC?

            // ints7D: 15 0x15: Idle Base Position
            idleBasePosition = ints7D[15].toString(),

            // ints7D: 16 0x16: Unknown

            // ints7D: 17 0x17: Unknown

            // ints7D: 18 0x18: Unknown

            // ints7D: 19 0x19: Unknown

            // ints7D: 20 0x1A: Unknown

            // ints7D: 21 0x1B: Unknown

            // ints7D: 22 0x1C: Unknown

            // ints7D: 23 0x1D: Unknown

            // ints7D: 24 0x1E: Unknown

            // ints7D: 25 0x1F: Unknown

            // ints7D: 26 0x20: Unknown

            // ints7D: 27 0x21: Unknown

            // ints7D: 28 0x22: Unknown

            // ints7D: 29 0x23: Unknown

            // ints7D: 30 0x24: Unknown

            // ints7D: 31 0x25: Unknown
        )
    }

    private fun getTuningCommand(item: Int): ByteArray =
        when (item) {

            // Ignition Timing
            TuningButtonId.INCREMENT_IGNITION_TIMING.ordinal -> COMMAND_INCREMENT_IGNITION_TIMING
            TuningButtonId.DECREMENT_IGNITION_TIMING.ordinal -> COMMAND_DECREMENT_IGNITION_TIMING

            // Idle Speed
            TuningButtonId.INCREMENT_IDLE_SPEED.ordinal -> COMMAND_INCREMENT_IDLE_SPEED
            TuningButtonId.DECREMENT_IDLE_SPEED.ordinal -> COMMAND_DECREMENT_IDLE_SPEED

            // Idle Decay
            TuningButtonId.INCREMENT_IDLE_DECAY.ordinal -> COMMAND_INCREMENT_IDLE_DECAY
            TuningButtonId.DECREMENT_IDLE_DECAY.ordinal -> COMMAND_DECREMENT_IDLE_DECAY

            // Fuel Trim
            TuningButtonId.INCREMENT_FUEL_TRIM.ordinal -> COMMAND_INCREMENT_FUEL_TRIM
            TuningButtonId.DECREMENT_FUEL_TRIM.ordinal -> COMMAND_DECREMENT_FUEL_TRIM

            // Reset
            TuningButtonId.RESET_TUNING.ordinal -> COMMAND_RESET_TUNING

            else -> throw IllegalArgumentException("Invalid Tuning Item ID")
        }
}