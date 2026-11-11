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

package pl.nowinkitransferowe.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import pl.nowinkitransferowe.core.data.repository.UserNewsResourceRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferResourceRepository
import pl.nowinkitransferowe.core.data.util.NetworkMonitor
import pl.nowinkitransferowe.core.data.util.TimeZoneMonitor
import pl.nowinkitransferowe.core.navigation.NavigationState
import pl.nowinkitransferowe.core.navigation.rememberNavigationState
import pl.nowinkitransferowe.core.ui.TrackDisposableJank
import pl.nowinkitransferowe.feature.bookmarks.api.navigation.BookmarksNavKey
import pl.nowinkitransferowe.feature.news.api.navigation.NewsNavKey
import pl.nowinkitransferowe.feature.transfers.api.navigation.TransfersNavKey
import pl.nowinkitransferowe.navigation.TOP_LEVEL_NAV_ITEMS

@Composable
fun rememberNtAppState(
    networkMonitor: NetworkMonitor,
    userNewsResourceRepository: UserNewsResourceRepository,
    userTransferResourceRepository: UserTransferResourceRepository,
    timeZoneMonitor: TimeZoneMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): NtAppState {
    val navigationState = rememberNavigationState(
        startKey = NewsNavKey,
        topLevelKeys = TOP_LEVEL_NAV_ITEMS.keys,
    )

    NavigationTrackingSideEffect(navigationState)

    return remember(
        navigationState,
        coroutineScope,
        networkMonitor,
        userNewsResourceRepository,
        userTransferResourceRepository,
        timeZoneMonitor,
    ) {
        NtAppState(
            navigationState = navigationState,
            coroutineScope = coroutineScope,
            networkMonitor = networkMonitor,
            userNewsResourceRepository = userNewsResourceRepository,
            userTransferResourceRepository = userTransferResourceRepository,
            timeZoneMonitor = timeZoneMonitor,
        )
    }
}

@Stable
class NtAppState(
    val navigationState: NavigationState,
    coroutineScope: CoroutineScope,
    networkMonitor: NetworkMonitor,
    userNewsResourceRepository: UserNewsResourceRepository,
    userTransferResourceRepository: UserTransferResourceRepository,
    timeZoneMonitor: TimeZoneMonitor,
) {
    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * The top level nav keys that have unread news or transfer resources.
     */
    val topLevelNavKeysWithUnreadResources: StateFlow<Set<NavKey>> =
        combine(
            userNewsResourceRepository.observeAll(),
            userNewsResourceRepository.observeAllBookmarked(),
            userTransferResourceRepository.observeAll(),
            userTransferResourceRepository.observeAllBookmarked(),
        ) { newsResources, bookmarkedNewsResources, transferResources, bookmarkedTransfersResources ->
            setOfNotNull(
                NewsNavKey.takeIf { newsResources.any { !it.hasBeenViewed } },
                TransfersNavKey.takeIf { transferResources.any { !it.hasBeenViewed } },
                BookmarksNavKey.takeIf {
                    bookmarkedNewsResources.any { !it.hasBeenViewed } ||
                        bookmarkedTransfersResources.any { !it.hasBeenViewed }
                },
            )
        }
            .stateIn(
                coroutineScope,
                SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet(),
            )

    val currentTimeZone = timeZoneMonitor.currentTimeZone
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            TimeZone.currentSystemDefault(),
        )
}

/**
 * Stores information about navigation events to be used with JankStats
 */
@Composable
private fun NavigationTrackingSideEffect(navigationState: NavigationState) {
    TrackDisposableJank(navigationState.currentKey) { metricsHolder ->
        metricsHolder.state?.putState("Navigation", navigationState.currentKey.toString())
        onDispose {}
    }
}
