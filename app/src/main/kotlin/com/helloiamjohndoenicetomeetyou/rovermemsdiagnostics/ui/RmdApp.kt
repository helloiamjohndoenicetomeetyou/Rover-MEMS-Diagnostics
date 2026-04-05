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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.BuildConfig
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.DataPacket
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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
fun SectionTitle(
    title: String,
    onTitleClick: (Int) -> Unit = {}
) {
    val yOffset = remember {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                yOffset.intValue = coordinates.positionInParent().y.toInt()
            }
    ) {
        Spacer(modifier = Modifier.size(40.dp))

        Text(
            text = title,
            modifier = Modifier
                .clickable {
                    onTitleClick(yOffset.intValue)
                },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearFaultCodesDialog(viewModel: RmdAppViewModel, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.requestClearFaultCodes()
                    onDismissRequest()
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("No")
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

@Composable
fun FaultCodesSection(uiState: RmdAppState, isConnected: Boolean, onShowDialog: () -> Unit) {
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

        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

        Text(
            text = "Clear",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun TuningSection(
    uiState: RmdAppState,
    isConnected: Boolean,
    onTuningButtonClick: (TuningButtonId) -> Unit,
    onShowDialog: () -> Unit,
) {
    val tuningList = listOf(
        TuningModel(
            label = "Ignition Timing",
            subLabel = "Range: 0x74 \u2013 0x8C\nDefault: 0x80",
            value = uiState.tuningIgnitionTiming,
            incrementButtonId = TuningButtonId.INCREMENT_IGNITION_TIMING,
            decrementButtonId = TuningButtonId.DECREMENT_IGNITION_TIMING
        ),
        TuningModel(
            label = "Idle Speed",
            subLabel = "Range: 0x78 \u2013 0x88\nDefault: 0x80",
            value = uiState.tuningIdleSpeed,
            incrementButtonId = TuningButtonId.INCREMENT_IDLE_SPEED,
            decrementButtonId = TuningButtonId.DECREMENT_IDLE_SPEED
        ),
        TuningModel(
            label = "Idle Decay",
            subLabel = "Range: 0x0A \u2013 0x3C\nDefault: 0x23",
            value = uiState.tuningIdleDecay,
            incrementButtonId = TuningButtonId.INCREMENT_IDLE_DECAY,
            decrementButtonId = TuningButtonId.DECREMENT_IDLE_DECAY
        ),
        TuningModel(
            label = "Fuel Trim",
            subLabel = "Range: 0x00 \u2013 0xFE\nDefault: 0x80",
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
                    text = "\u2193", // Down Arrow
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
                    text = "\u2191", // Up Arrow
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

        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

        Text(
            text = "Reset",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetTuningDialog(viewModel: RmdAppViewModel, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.performTuning(TuningButtonId.RESET_TUNING)
                    onDismissRequest()
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("No")
            }
        },
        title = {
            Text(text = "Reset Tuning")
        },
        text = {
            Text(text = "Are you sure you want to reset tuning?")
        }
    )
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

data class RmdAppState(
    val engineSpeed: String = "-",
    val idleSpeedDeviation: String = "-",
    val idleSwitch: String = "-",
    val throttlePotentiometerVoltage: String = "-",
    val idleAirControlMotorPosition: String = "-",
    val manifoldAbsolutePressure: String = "-",
    val batteryVoltage: String = "-",
    val waterTemperature: String = "-",
    val intakeAirTemperature: String = "-",
    val neutralSwitch: String = "-",
    val oxygenSensorVoltage: String = "-",
    val fuelTrimLoopOperation: String = "-",
    val shortTermFuelTrim: String = "-",

    val crankshaftAngleSensorFault: String = "-",
    val throttlePotentiometerFault: String = "-",
    val manifoldAbsolutePressureSensorFault: String = "-",
    val waterTemperatureSensorFault: String = "-",
    val intakeAirTemperatureSensorFault: String = "-",

    val coolerSwitch: String = "-",
    val idleSetPoint: String = "-",
    val hotIdlePosition: String = "-",
    val idleBasePosition: String = "-",
    val ignitionTimingOffset: String = "-",
    val ignitionTiming: String = "-",
    val ignitionCoilDwellTime: String = "-",

    val tuningIgnitionTiming: String = "-",
    val tuningIdleSpeed: String = "-",
    val tuningIdleDecay: String = "-",
    val tuningFuelTrim: String = "-"
)

class RmdAppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RmdAppState())

    val uiState: StateFlow<RmdAppState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<String>()

    val uiEvent = _uiEvent.receiveAsFlow()

    private val _isConnected = MutableStateFlow(false)

    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun setIsConnected(isConnected: Boolean) {
        _isConnected.value = isConnected
    }

    var onConnectRequested: (() -> Unit)? = null

    fun requestConnect() {
        onConnectRequested?.invoke()
    }

    var onDisconnectRequested: (() -> Unit)? = null

    fun requestDisconnect() {
        onDisconnectRequested?.invoke()
    }

    var onClearFaultCodesRequested: (() -> Unit)? = null

    fun requestClearFaultCodes() {
        onClearFaultCodesRequested?.invoke()
    }

    /**
     * Called when LiveData and FaultCodes are received.
     */
    fun onLiveDataReceived(data: DataPacket) {
        _uiState.update {
            it.copy(
                engineSpeed = data.engineSpeed,
                idleSpeedDeviation = data.idleSpeedDeviation,
                idleSwitch = data.idleSwitch,
                throttlePotentiometerVoltage = data.throttlePotentiometerVoltage,
                idleAirControlMotorPosition = data.idleAirControlMotorPosition,
                manifoldAbsolutePressure = data.manifoldAbsolutePressure,
                batteryVoltage = data.batteryVoltage,
                waterTemperature = data.waterTemperature,
                intakeAirTemperature = data.intakeAirTemperature,
                neutralSwitch = data.neutralSwitch,
                oxygenSensorVoltage = data.oxygenSensorVoltage,
                fuelTrimLoopOperation = data.fuelTrimLoopOperation,
                shortTermFuelTrim = data.shortTermFuelTrim,

                coolerSwitch = data.coolerSwitch,
                idleSetPoint = data.idleSetPoint,
                hotIdlePosition = data.hotIdlePosition,
                idleBasePosition = data.idleBasePosition,
                ignitionTimingOffset = data.ignitionTimingOffset,
                ignitionTiming = data.ignitionTiming,
                ignitionCoilDwellTime = data.ignitionCoilDwellTime,

                crankshaftAngleSensorFault = data.crankshaftAngleSensorFault,
                throttlePotentiometerFault = data.throttlePotentiometerFault,
                manifoldAbsolutePressureSensorFault = data.manifoldAbsolutePressureSensorFault,
                waterTemperatureSensorFault = data.waterTemperatureSensorFault,
                intakeAirTemperatureSensorFault = data.intakeAirTemperatureSensorFault
            )
        }
    }

    var onPerformTuningRequested: ((TuningButtonId) -> Unit)? = null

    fun performTuning(buttonId: TuningButtonId) {
        onPerformTuningRequested?.invoke(buttonId)
    }

    /**
     * Called when the tuning value is changed.
     * @param buttonId The ID of the Button that was clicked.
     * @param value The new value of the Tuning.
     */
    fun onTuningValueChanged(buttonId: TuningButtonId, value: String) {
        _uiState.update {
            when (buttonId) {
                TuningButtonId.INCREMENT_IGNITION_TIMING,
                TuningButtonId.DECREMENT_IGNITION_TIMING ->
                    it.copy(tuningIgnitionTiming = value)

                TuningButtonId.INCREMENT_IDLE_SPEED,
                TuningButtonId.DECREMENT_IDLE_SPEED ->
                    it.copy(tuningIdleSpeed = value)

                TuningButtonId.INCREMENT_IDLE_DECAY,
                TuningButtonId.DECREMENT_IDLE_DECAY ->
                    it.copy(tuningIdleDecay = value)

                TuningButtonId.INCREMENT_FUEL_TRIM,
                TuningButtonId.DECREMENT_FUEL_TRIM ->
                    it.copy(tuningFuelTrim = value)

                TuningButtonId.RESET_TUNING ->
                    it.copy(
                        tuningIgnitionTiming = "-",
                        tuningIdleSpeed = "-",
                        tuningIdleDecay = "-",
                        tuningFuelTrim = "-"
                    )
            }
        }

        if (buttonId == TuningButtonId.RESET_TUNING) {
            showSnackbar("Resetting tuning successfully completed.")
        }
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(message)
        }
    }
}

data class LiveDataModel(val label: String, val value: String)

data class FaultCodeModel(val label: String, val value: String)

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