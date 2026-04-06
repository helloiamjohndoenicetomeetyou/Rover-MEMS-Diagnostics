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

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.RmdAppState

@Composable
fun LiveDataItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun LiveDataItem2(label: String, value: String, subLabel: String, subValue: String) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium
        )
        Row {
            Text(
                text = subLabel,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

data class LiveDataModel(val label: String, val value: String)

@Composable
fun LiveDataSection(uiState: RmdAppState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            LiveDataItem2(
                label = "Engine Speed - RPM",
                value = uiState.engineSpeed,
                subLabel = "\u00B1",
                subValue = uiState.idleSpeedDeviation
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            LiveDataItem2(
                label = "Idle Switch",
                value = uiState.idleSwitch,
                subLabel = "",
                subValue = ""
            )
        }
    }

    val livedataList = listOf(
        LiveDataModel(
            label = "Throttle - V",
            value = uiState.throttlePotentiometerVoltage
        ),
        LiveDataModel(
            label = "IAC (Step)",
            value = uiState.idleAirControlMotorPosition
        ),
        LiveDataModel(
            label = "MAP - kPa",
            value = uiState.manifoldAbsolutePressure
        ),
        LiveDataModel(
            label = "Battery - V",
            value = uiState.batteryVoltage
        ),
        LiveDataModel(
            label = "Water - \u00B0C",
            value = uiState.waterTemperature
        ),
        LiveDataModel(
            label = "Air - \u00B0C",
            value = uiState.intakeAirTemperature
        ),
        LiveDataModel(
            label = "Neutral Switch (AT)",
            value = uiState.neutralSwitch
        ),
        LiveDataModel(
            label = "Lambda (O2) - mV",
            value = uiState.oxygenSensorVoltage
        ),
        LiveDataModel(
            label = "FT Loop",
            value = uiState.fuelTrimLoopOperation
        ),
        LiveDataModel(
            label = "Short FT - %",
            value = uiState.shortTermFuelTrim
        )
    )

    val rows = livedataList.chunked(2)

    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { liveData ->
                Box(modifier = Modifier.weight(1f)) {
                    LiveDataItem(
                        label = liveData.label,
                        value = liveData.value
                    )
                }
            }
        }
    }
}

@Composable
fun LiveDataExperimentalSection(uiState: RmdAppState) {
    val list = listOf(
        LiveDataModel(
            label = "Cooler Switch",
            value = uiState.coolerSwitch
        ),
        LiveDataModel(
            label = "Idle Set Point",
            value = uiState.idleSetPoint
        ),
        LiveDataModel(
            label = "Hot Idle Position",
            value = uiState.hotIdlePosition
        ),
        LiveDataModel(
            label = "Idle Base Position",
            value = uiState.idleBasePosition
        ),
        LiveDataModel(
            label = "Ignition Timing Offset",
            value = uiState.ignitionTimingOffset
        ),
        LiveDataModel(
            label = "Ignition Timing",
            value = uiState.ignitionTiming
        ),
        LiveDataModel(
            label = "Ignition Coil Dwell Time - ms",
            value = uiState.ignitionCoilDwellTime
        )
    )

    list.forEach { data ->
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = data.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = data.value,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}