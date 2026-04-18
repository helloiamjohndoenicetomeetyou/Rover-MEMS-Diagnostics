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

package com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.helloiamjohndoenicetomeetyou.rovermemsdiagnostics.ui.components.AboutScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object AboutRoute

private const val DURATION_MILLIS = 400

@Composable
fun RoverMemsDiagnosticsNavigationGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: RoverMemsDiagnosticsViewModel
) {
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute>(
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(DURATION_MILLIS)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(DURATION_MILLIS)
                )
            }
        ) {
            RoverMemsDiagnostics(navController = navController, viewModel = viewModel)
        }

        composable<AboutRoute>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(DURATION_MILLIS)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(DURATION_MILLIS)
                )
            }
        ) {
            AboutScreen(navController = navController)
        }
    }
}