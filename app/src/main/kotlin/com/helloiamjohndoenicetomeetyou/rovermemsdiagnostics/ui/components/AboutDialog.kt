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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.BuildConfig
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.R

@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        },
        title = {
            Text(text = "About")
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.app_name) + "\n" +
                            "\n" +
                            "Supported ECU Version:\n" +
                            "MEMS 1.6\n" +
                            "\n" +
                            "Application Version:\n" +
                            "${BuildConfig.VERSION_NAME}\n",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                url = "https://helloiamjohndoenicetomeetyou.github.io/Rover-MEMS-Diagnostics/"
                            )
                        ) {
                            append("More Information (GitHub)")
                        }
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    )
}