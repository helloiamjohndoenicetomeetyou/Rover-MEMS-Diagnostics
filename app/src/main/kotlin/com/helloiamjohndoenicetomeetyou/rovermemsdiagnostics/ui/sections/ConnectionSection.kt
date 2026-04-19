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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.R
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.communication.protocol.EcuVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSection(
    isConnected: Boolean,
    onCheckedChange: (Boolean, EcuVersion) -> Unit
) {
    val dropdownMenuExpanded = remember {
        mutableStateOf(false)
    }

    val selectedOption = remember {
        mutableStateOf(EcuVersion.MEMS_16.ecuVersion)
    }

    Row(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = dropdownMenuExpanded.value && !isConnected,
            onExpandedChange = {
                if (!isConnected) {
                    dropdownMenuExpanded.value = !dropdownMenuExpanded.value
                }
            }
        ) {
            OutlinedTextField(
                value = selectedOption.value,
                onValueChange = {
                },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                enabled = !isConnected,
                readOnly = true,
                textStyle = MaterialTheme.typography.titleMedium,
                label = {
                    Text(text = stringResource(R.string.ecu_version))
                }
            )

            ExposedDropdownMenu(
                expanded = dropdownMenuExpanded.value && !isConnected,
                onDismissRequest = {
                    dropdownMenuExpanded.value = false
                }
            ) {
                EcuVersion.entries.forEach { entry ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = entry.ecuVersion,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        onClick = {
                            selectedOption.value = entry.ecuVersion
                            dropdownMenuExpanded.value = false
                        }
                    )
                }
            }
        }

        Switch(
            checked = isConnected,
            onCheckedChange = { checked ->
                val ecuVersion = EcuVersion.fromString(selectedOption.value)
                    ?: throw Exception("Illegal ECU Version")
                onCheckedChange(checked, ecuVersion)
            }
        )
    }
}