/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.nowinkitransferowe.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import pl.nowinkitransferowe.feature.bookmarks.navigation.bookmarksScreen
import pl.nowinkitransferowe.feature.details.navigation.detailsScreen
import pl.nowinkitransferowe.feature.details.navigation.navigateToDetails
import pl.nowinkitransferowe.feature.details.transfers.navigation.detailsTransferScreen
import pl.nowinkitransferowe.feature.details.transfers.navigation.navigateToTransferDetails
import pl.nowinkitransferowe.feature.news.navigation.NEWS_ROUTE
import pl.nowinkitransferowe.feature.search.navigation.navigateToSearch
import pl.nowinkitransferowe.feature.search.navigation.searchScreen
import pl.nowinkitransferowe.ui.NtAppState
import pl.nowinkitransferowe.ui.news2pane.newsListDetailScreen
import pl.nowinkitransferowe.ui.transfers2pane.transferListDetailScreen

/**
 * Top-level navigation graph. Navigation is organized as explained at
 * https://d.android.com/jetpack/compose/nav-adaptive
 *
 * The navigation graph defined in this file defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@Composable
fun NtNavHost(
    appState: NtAppState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    startDestination: String = NEWS_ROUTE,
) {
    val navController = appState.navController
    NavHost(

        navController = navController,
        startDestination = startDestination,
//        enterTransition = enterTransition,
//        exitTransition = exitTransition,
//        popEnterTransition = popEnterTransition,
//        popExitTransition = popExitTransition,
        modifier = modifier,

    ) {
        newsListDetailScreen(
            onTopicClick = navController::navigateToSearch,
        )
        transferListDetailScreen()

        bookmarksScreen(
            onNewsClick = navController::navigateToDetails,
            onTransferClick = navController::navigateToTransferDetails,
            onTopicClick = navController::navigateToSearch,
            onShowSnackbar = onShowSnackbar,
        )
        detailsScreen(
            showBackButton = true,
            onBackClick = navController::popBackStack,
            onTopicClick = navController::navigateToSearch,
        )
        detailsTransferScreen(
            showBackButton = true,
            onBackClick = navController::popBackStack,
        )

        searchScreen(
            onBackClick = navController::popBackStack,
            onNewsClick = navController::navigateToDetails,
            onTransferClick = navController::navigateToTransferDetails,
        )
    }
}

private const val TIME_DURATION = 300

val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing),
    )
}

val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing),
    )
}

val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { -it / 3 },
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing),
    )
}

val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing),
    )
}
