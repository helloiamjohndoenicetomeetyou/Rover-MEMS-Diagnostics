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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.R
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.RoverMemsDiagnosticsState
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.RoverMemsDiagnosticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetTuningDialog(viewModel: RoverMemsDiagnosticsViewModel, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.performTuning(TuningButtonId.RESET_TUNING)
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.no))
            }
        },
        title = {
            Text(text = stringResource(R.string.reset_tuning))
        },
        text = {
            Text(text = stringResource(R.string.reset_tuning_message))
        }
    )
}

data class TuningModel(
    val label: String,
    val subLabel: String,
    val value: String,
    val incrementButtonId: TuningButtonId,
    val decrementButtonId: TuningButtonId
)

enum class TuningButtonId {
    RESET_TUNING,
    INCREMENT_IGNITION_TIMING,
    DECREMENT_IGNITION_TIMING,
    INCREMENT_IDLE_SPEED,
    DECREMENT_IDLE_SPEED,
    INCREMENT_IDLE_DECAY,
    DECREMENT_IDLE_DECAY,
    INCREMENT_FUEL_TRIM,
    DECREMENT_FUEL_TRIM
}

@Composable
fun TuningSection(
    uiState: RoverMemsDiagnosticsState,
    isConnected: Boolean,
    onTuningButtonClick: (TuningButtonId) -> Unit,
    onShowDialog: () -> Unit,
) {
    val tuningList = listOf(
        TuningModel(
            label = stringResource(R.string.ignition_timing),
            subLabel = stringResource(R.string.ignition_timing_range),
            value = uiState.tuningIgnitionTiming,
            incrementButtonId = TuningButtonId.INCREMENT_IGNITION_TIMING,
            decrementButtonId = TuningButtonId.DECREMENT_IGNITION_TIMING
        ),
        TuningModel(
            label = stringResource(R.string.idle_speed),
            subLabel = stringResource(R.string.idle_speed_range),
            value = uiState.tuningIdleSpeed,
            incrementButtonId = TuningButtonId.INCREMENT_IDLE_SPEED,
            decrementButtonId = TuningButtonId.DECREMENT_IDLE_SPEED
        ),
        TuningModel(
            label = stringResource(R.string.idle_decay),
            subLabel = stringResource(R.string.idle_decay_range),
            value = uiState.tuningIdleDecay,
            incrementButtonId = TuningButtonId.INCREMENT_IDLE_DECAY,
            decrementButtonId = TuningButtonId.DECREMENT_IDLE_DECAY
        ),
        TuningModel(
            label = stringResource(R.string.fuel_trim),
            subLabel = stringResource(R.string.fuel_trim_range),
            value = uiState.tuningFuelTrim,
            incrementButtonId = TuningButtonId.INCREMENT_FUEL_TRIM,
            decrementButtonId = TuningButtonId.DECREMENT_FUEL_TRIM
        )
    )

    tuningList.forEach { tuning ->

        // Label and Value
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = tuning.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = tuning.value,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Range and Default Value
        Text(
            text = tuning.subLabel,
            style = MaterialTheme.typography.bodyMedium
        )

        // Increment Button and Decrement Button
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            // Decrement Button
            FilledTonalButton(
                onClick = { onTuningButtonClick(tuning.decrementButtonId) },
                enabled = isConnected
            ) {
                Text(
                    text = stringResource(R.string.arrow_down),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Increment Button
            FilledTonalButton(
                onClick = {
                    onTuningButtonClick(tuning.incrementButtonId)
                },
                enabled = isConnected
            ) {
                Text(
                    text = stringResource(R.string.arrow_up),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    // Reset Tuning Button
    FilledTonalButton(
        onClick = onShowDialog,
        modifier = Modifier.padding(top = 16.dp),
        enabled = isConnected
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_button_reset),
            contentDescription = null
        )

        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))

        Text(
            text = stringResource(R.string.reset),
            style = MaterialTheme.typography.titleMedium
        )
    }
}