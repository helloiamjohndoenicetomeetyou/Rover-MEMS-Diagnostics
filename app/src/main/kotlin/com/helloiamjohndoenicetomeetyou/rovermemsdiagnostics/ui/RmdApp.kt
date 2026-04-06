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

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.BuildConfig
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.R
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.ClearFaultCodesDialog
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.FaultCodesSection
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.LiveDataExperimentalSection
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.LiveDataSection
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.ResetTuningDialog
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.SectionTitle
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.sections.TuningSection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun RmdApp(viewModel: RmdAppViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()

    val scrollToSection: (Int) -> Unit = { offset ->
        coroutineScope.launch {
            scrollState.animateScrollTo(offset)
        }
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val expanded = remember {
        mutableStateOf(false)
    }

    val showClearFaultCodesDialog = remember {
        mutableStateOf(false)
    }

    val showResetTuningDialog = remember {
        mutableStateOf(false)
    }

    val showAboutDialog = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                actions = {
                    Switch(
                        checked = isConnected,
                        onCheckedChange = {
                            viewModel.setIsConnected(it)

                            if (it) {
                                viewModel.requestConnect()
                            } else {
                                viewModel.requestDisconnect()
                            }
                        }
                    )

                    IconButton(
                        onClick = {
                            expanded.value = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = {
                            expanded.value = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "About",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            onClick = {
                                showAboutDialog.value = true
                                expanded.value = false
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Live Data Section
            SectionTitle(
                title = "Live Data",
                onTitleClick = scrollToSection
            )
            LiveDataSection(uiState = uiState)

            // Live Data (Experimental) Section
            SectionTitle(
                title = "Live Data (Experimental)",
                onTitleClick = scrollToSection
            )
            LiveDataExperimentalSection(uiState = uiState)

            //Fault Codes Section
            SectionTitle(
                title = "Fault Codes",
                onTitleClick = scrollToSection
            )
            FaultCodesSection(
                uiState = uiState,
                isConnected = isConnected,
                onShowDialog = {
                    showClearFaultCodesDialog.value = true
                }
            )

            // Tuning Section
            SectionTitle(
                title = "Tuning",
                onTitleClick = scrollToSection
            )
            TuningSection(
                uiState = uiState,
                isConnected = isConnected,
                onTuningButtonClick = { buttonId ->
                    viewModel.performTuning(buttonId)
                },
                onShowDialog = {
                    showResetTuningDialog.value = true
                }
            )
        }
    }

    if (showClearFaultCodesDialog.value) {
        ClearFaultCodesDialog(
            viewModel = viewModel,
            onDismissRequest = {
                showClearFaultCodesDialog.value = false
            }
        )
    }

    if (showResetTuningDialog.value) {
        ResetTuningDialog(
            viewModel = viewModel,
            onDismissRequest = {
                showResetTuningDialog.value = false
            }
        )
    }

    if (showAboutDialog.value) {
        AboutDialog(
            onDismissRequest = {
                showAboutDialog.value = false
            }
        )
    }
}

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

@Composable
fun NotSupportedDialog(onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = {
            // Nothing To Do.
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
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