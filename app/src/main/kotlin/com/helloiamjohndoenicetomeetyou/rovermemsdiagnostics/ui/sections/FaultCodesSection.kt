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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.R
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.RoverMemsDiagnosticsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearFaultCodesDialog(onConfirm: () -> Unit, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismissRequest()
                }
            ) {
                Text(text = "Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "No")
            }
        },
        title = {
            Text(text = "Clear Fault Codes")
        },
        text = {
            Text(text = "Are you sure you want to clear fault codes?")
        }
    )
}

data class FaultCodeModel(val label: String, val value: String)

@Composable
fun FaultCodesSection(
    uiState: RoverMemsDiagnosticsState,
    isConnected: Boolean,
    onShowDialog: () -> Unit
) {
    val faultCodeList = listOf(
        FaultCodeModel(
            label = "Crankshaft Angle Sensor",
            value = uiState.crankshaftAngleSensorFault
        ),
        FaultCodeModel(
            label = "Throttle Potentiometer Circuit",
            value = uiState.throttlePotentiometerFault
        ),
        FaultCodeModel(
            label = "Manifold Absolute Pressure Sensor",
            value = uiState.manifoldAbsolutePressureSensorFault
        ),
        FaultCodeModel(
            label = "Water Temperature Sensor",
            value = uiState.waterTemperatureSensorFault
        ),
        FaultCodeModel(
            label = "Intake Air Temperature Sensor",
            value = uiState.intakeAirTemperatureSensorFault
        )
    )

    faultCodeList.forEach { faultCode ->
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = faultCode.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = faultCode.value,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    // Clear Fault Codes Button
    FilledTonalButton(
        onClick = onShowDialog,
        modifier = Modifier.padding(top = 16.dp),
        enabled = isConnected
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_button_clear),
            contentDescription = null
        )

        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))

        Text(
            text = "Clear",
            style = MaterialTheme.typography.titleMedium
        )
    }
}