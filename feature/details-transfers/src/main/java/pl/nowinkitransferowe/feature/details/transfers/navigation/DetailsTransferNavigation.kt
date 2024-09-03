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

package pl.nowinkitransferowe.feature.details.transfers.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pl.nowinkitransferowe.feature.details.transfers.DetailsTransferRoute

const val LINKED_TRANSFER_RESOURCE_ID = "linkedTransferResourceId"
const val DETAILS_TRANSFER_ROUTE = "details_transfer_route"

fun NavController.navigateToTransferDetails(
    detailsId: String,
    navOptions: NavOptionsBuilder.() -> Unit = {},
) {
    navigate(createDetailsTransferRoute(detailsId)) {
        navOptions()
    }
}

fun createDetailsTransferRoute(transferId: String): String {
    return "$DETAILS_TRANSFER_ROUTE/$transferId"
}

fun NavGraphBuilder.detailsTransferScreen(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
) {
    composable(
        route = "$DETAILS_TRANSFER_ROUTE/{$LINKED_TRANSFER_RESOURCE_ID}",
        arguments = listOf(
            navArgument(LINKED_TRANSFER_RESOURCE_ID) { type = NavType.StringType },
        ),
    ) {
        DetailsTransferRoute(
            showBackButton = showBackButton,
            onBackClick = onBackClick,
        )
    }
}
