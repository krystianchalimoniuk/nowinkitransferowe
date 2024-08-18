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

package pl.nowinkitransferowe.feature.details.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pl.nowinkitransferowe.feature.details.DetailsRoute

const val LINKED_NEWS_RESOURCE_ID = "linkedNewsResourceId"
const val DETAILS_ROUTE = "details_route"

fun NavController.navigateToDetails(
    detailsId: String,
    navOptions: NavOptionsBuilder.() -> Unit = {},
) {
    navigate(createDetailsRoute(detailsId)) {
        navOptions()
    }
}

fun createDetailsRoute(newsId: String): String {
    return "$DETAILS_ROUTE/$newsId"
}

fun NavGraphBuilder.detailsScreen(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
) {
    composable(
        route = "$DETAILS_ROUTE/{$LINKED_NEWS_RESOURCE_ID}",
        arguments = listOf(
            navArgument(LINKED_NEWS_RESOURCE_ID) { type = NavType.StringType },
        ),
    ) {
        DetailsRoute(
            showBackButton = showBackButton,
            onBackClick = onBackClick,
            onTopicClick = onTopicClick,
        )
    }
}
