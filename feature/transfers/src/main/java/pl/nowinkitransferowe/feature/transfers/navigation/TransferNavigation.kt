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

package pl.nowinkitransferowe.feature.transfers.navigation

import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

const val LINKED_TRANSFER_RESOURCE_ID = "linkedTransferResourceId"
const val TRANSFERS_ROUTE = "transfers_route"
private const val DEEP_LINK_URI_PATTERN =
    "http://nowinkitransferowe.pl/transfer/{$LINKED_TRANSFER_RESOURCE_ID}"

fun NavController.navigateToTransfer(navOptions: NavOptions) = navigate(TRANSFERS_ROUTE, navOptions)

fun NavGraphBuilder.transferScreen(onCleanBackStack: () -> Unit,  onTransferClick: (String) -> Unit) {
    composable(
        route = TRANSFERS_ROUTE,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = DEEP_LINK_URI_PATTERN
                action = Intent.ACTION_VIEW
            },
        ),
        arguments = listOf(
            navArgument(LINKED_TRANSFER_RESOURCE_ID) {
                type = NavType.StringType
                defaultValue = null
                nullable = true
            },
        ),
    ) {
        TransferRoute(onCleanBackStack = onCleanBackStack, onTransferClick = onTransferClick)
    }
}
