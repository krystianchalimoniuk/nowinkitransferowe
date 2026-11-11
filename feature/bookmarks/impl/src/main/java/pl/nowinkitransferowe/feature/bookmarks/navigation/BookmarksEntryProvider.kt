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

package pl.nowinkitransferowe.feature.bookmarks.navigation

import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.feature.bookmarks.BookmarksRoute
import pl.nowinkitransferowe.feature.bookmarks.api.navigation.BookmarksNavKey
import pl.nowinkitransferowe.feature.details.api.navigation.navigateToNewsDetails
import pl.nowinkitransferowe.feature.details.transfers.api.navigation.navigateToTransferDetails
import pl.nowinkitransferowe.feature.search.api.navigation.navigateToSearch

fun EntryProviderScope<NavKey>.bookmarksEntry(navigator: Navigator) {
    entry<BookmarksNavKey> {
        val snackbarHostState = LocalSnackbarHostState.current
        BookmarksRoute(
            onNewsClick = navigator::navigateToNewsDetails,
            onTransferClick = navigator::navigateToTransferDetails,
            onTopicClick = navigator::navigateToSearch,
            onShowSnackbar = { message, action ->
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = action,
                    duration = Short,
                ) == ActionPerformed
            },
        )
    }
}

/**
 * Snackbar host of the app, so screens shown in the navigation display can show snackbars.
 */
val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState state should be initialized at runtime")
}
