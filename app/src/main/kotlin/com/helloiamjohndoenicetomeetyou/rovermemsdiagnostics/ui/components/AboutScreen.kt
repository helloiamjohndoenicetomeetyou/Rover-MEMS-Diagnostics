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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.BuildConfig
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.about_application,
                            stringResource(R.string.application_name)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
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
        ) {
            Section(
                title = stringResource(R.string.application_version),
                description = BuildConfig.VERSION_NAME
            )
            Section(
                title = stringResource(R.string.supported_ecu),
                description = stringResource(R.string.supported_ecu_list)
            )
            Section(
                title = buildAnnotatedString {
                    withLink(
                        LinkAnnotation.Url(
                            url = stringResource(R.string.github_pages_url),
                            styles = TextLinkStyles()
                        )
                    ) {
                        append(stringResource(R.string.more_information))
                    }
                }
            )
        }
    }
}

@Composable
private fun Section(title: String, description: String) {
    Spacer(modifier = Modifier.size(16.dp))

    Text(text = title, style = MaterialTheme.typography.bodyLarge)
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Section(title: AnnotatedString) {
    Spacer(modifier = Modifier.size(16.dp))

    Text(text = title, style = MaterialTheme.typography.bodyLarge)
}