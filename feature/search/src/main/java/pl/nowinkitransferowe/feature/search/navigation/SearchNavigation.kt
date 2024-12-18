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

package pl.nowinkitransferowe.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.nowinkitransferowe.feature.search.SearchRoute

@Serializable data class SearchRoute(
    val searchQuery: String? = null,
)

fun NavController.navigateToSearch(query: String?, navOptions: NavOptions? = null) =
    navigate(SearchRoute(query), navOptions)

fun NavGraphBuilder.searchScreen(
    onBackClick: () -> Unit,
    onNewsClick: (String) -> Unit,
    onTransferClick: (String) -> Unit,
) {
    // TODO: Handle back stack for each top-level destination. At the moment each top-level
    // destination may have own search screen's back stack.
    composable<SearchRoute> {
        SearchRoute(
            onBackClick = onBackClick,
            onNewsClick = onNewsClick,
            onTransferClick = onTransferClick,
        )
    }
}
