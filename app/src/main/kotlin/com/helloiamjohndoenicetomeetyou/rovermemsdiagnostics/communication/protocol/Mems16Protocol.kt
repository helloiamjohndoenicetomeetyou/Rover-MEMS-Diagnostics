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

import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.DataPacket
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.driver.FtdiDriver
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.toHexStringRmd
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningButtonId

class Mems16Protocol(private val mDriver: FtdiDriver) {
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

    fun initialize(): Boolean {
        val bytes = ByteArray(SIZE_BUFFER)

        if (!sendCommand(COMMAND_INITIALIZE_ECU_16, bytes)) {
            return false
        }

        if (!sendCommand(COMMAND_NOP, bytes)) {
            return false
        }

        return true
    }

    fun close() = mDriver.close()

    fun requestLiveData(): DataPacket? {
        val bytes80 = ByteArray(SIZE_BUFFER)
        if (!sendCommand(COMMAND_REQUEST_DATA_16_80, bytes80)) {
            return null
        }

        val bytes7D = ByteArray(SIZE_BUFFER)
        if (!sendCommand(COMMAND_REQUEST_DATA_16_7D, bytes7D)) {
            return null
        }

        return DataPacket(bytes80, bytes7D)
    }

    fun clearFaultCodes(): Boolean =
        sendCommand(COMMAND_CLEAR_FAULT_CODES, ByteArray(SIZE_BUFFER))

    fun performTuning(buttonId: TuningButtonId): String? {
        val bytes = ByteArray(SIZE_BUFFER)

        if (!sendCommand(getTuningCommand(buttonId), bytes)) {
            return null
        }

        return bytes[2].toHexStringRmd()
    }

    private fun sendCommand(command: ByteArray, bytes: ByteArray): Boolean {
        command.forEach { byte ->
            if (!mDriver.write(byteArrayOf(byte))) {
                return false
            }

            if (!mDriver.read(bytes)) {
                return false
            }
        }

        return true
    }

    private fun getTuningCommand(buttonId: TuningButtonId): ByteArray =
        when (buttonId) {

            // Ignition Timing
            TuningButtonId.INCREMENT_IGNITION_TIMING -> COMMAND_INCREMENT_IGNITION_TIMING
            TuningButtonId.DECREMENT_IGNITION_TIMING -> COMMAND_DECREMENT_IGNITION_TIMING

            // Idle Speed
            TuningButtonId.INCREMENT_IDLE_SPEED -> COMMAND_INCREMENT_IDLE_SPEED
            TuningButtonId.DECREMENT_IDLE_SPEED -> COMMAND_DECREMENT_IDLE_SPEED

            // Idle Decay
            TuningButtonId.INCREMENT_IDLE_DECAY -> COMMAND_INCREMENT_IDLE_DECAY
            TuningButtonId.DECREMENT_IDLE_DECAY -> COMMAND_DECREMENT_IDLE_DECAY

            // Fuel Trim
            TuningButtonId.INCREMENT_FUEL_TRIM -> COMMAND_INCREMENT_FUEL_TRIM
            TuningButtonId.DECREMENT_FUEL_TRIM -> COMMAND_DECREMENT_FUEL_TRIM

            // Reset
            TuningButtonId.RESET_TUNING -> COMMAND_RESET_TUNING
        }
}