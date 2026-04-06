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

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NotSupportedDialog(onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            // Nothing To Do.
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Close")
            }
        },
        title = {
            Text(text = "Not Supported")
        },
        text = {
            Text(text = "Your device does not support USB Host Mode, which is required for this application.")
        }
    )
}