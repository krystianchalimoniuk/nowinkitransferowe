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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pl.nowinkitransferowe.feature.search.SearchRoute
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

private val URL_CHARACTER_ENCODING = UTF_8.name()

const val SEARCH_ROUTE = "search_route"
const val SEARCH_QUERY_ARG = "searchQuery"

fun NavController.navigateToSearch(query: String?, navOptions: NavOptions? = null) =
    navigate(createSearchRoute(query), navOptions)

fun createSearchRoute(query: String?): String {
    query?.let {
        val encodedQuery = URLEncoder.encode(query, URL_CHARACTER_ENCODING)
        return "$SEARCH_ROUTE/$encodedQuery"
    }
    return "$SEARCH_ROUTE/$query"
}

fun NavGraphBuilder.searchScreen(
    onBackClick: () -> Unit,
    onNewsClick: (String) -> Unit,
    onTransferClick: (String) -> Unit,
) {
    // TODO: Handle back stack for each top-level destination. At the moment each top-level
    // destination may have own search screen's back stack.
    composable(
        route = "$SEARCH_ROUTE/{$SEARCH_QUERY_ARG}",
        arguments = listOf(
            navArgument(SEARCH_QUERY_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) {
        SearchRoute(
            onBackClick = onBackClick,
            onNewsClick = onNewsClick,
            onTransferClick = onTransferClick,
        )
    }
}
