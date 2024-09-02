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

package pl.nowinkitransferowe.ui.transfers2pane

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import pl.nowinkitransferowe.feature.details.transfers.DetailsTransferPlaceholder
import pl.nowinkitransferowe.feature.details.transfers.navigation.DETAILS_TRANSFER_ROUTE
import pl.nowinkitransferowe.feature.details.transfers.navigation.createDetailsTransferRoute
import pl.nowinkitransferowe.feature.details.transfers.navigation.detailsTransferScreen
import pl.nowinkitransferowe.feature.details.transfers.navigation.navigateToTransferDetails
import pl.nowinkitransferowe.feature.transfers.navigation.LINKED_TRANSFER_RESOURCE_ID
import pl.nowinkitransferowe.feature.transfers.navigation.TRANSFERS_ROUTE
import pl.nowinkitransferowe.feature.transfers.navigation.TransferRoute
import java.util.UUID

private const val DETAIL_PANE_NAVHOST_ROUTE = "detail_transfer_pane_route"
private const val DEEP_LINK_URI_PATTERN =
    "http://nowinkitransferowe.pl/transfer/{$LINKED_TRANSFER_RESOURCE_ID}"

fun NavGraphBuilder.transferListDetailScreen() {
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
        TransfersListDetailScreen()
    }
}

@Composable
internal fun TransfersListDetailScreen(
    viewModel: Transfers2PaneViewModel = hiltViewModel(),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val selectedTransferId by viewModel.selectedTransferId.collectAsStateWithLifecycle()
    TransfersListDetailScreen(
        selectedTransferId = selectedTransferId,
        onTransferClick = viewModel::onTransferClick,
        windowAdaptiveInfo = windowAdaptiveInfo,

    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun TransfersListDetailScreen(
    selectedTransferId: String?,
    onTransferClick: (String) -> Unit,
    windowAdaptiveInfo: WindowAdaptiveInfo,
) {
    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = calculatePaneScaffoldDirective(windowAdaptiveInfo),
        initialDestinationHistory = listOfNotNull(
            ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List),
            ThreePaneScaffoldDestinationItem<Nothing>(ListDetailPaneScaffoldRole.Detail).takeIf {
                selectedTransferId != null
            },
        ),
    )
    BackHandler(listDetailNavigator.canNavigateBack()) {
        listDetailNavigator.navigateBack()
    }

    var nestedNavHostStartDestination by remember {
        mutableStateOf(selectedTransferId?.let(::createDetailsTransferRoute) ?: DETAILS_TRANSFER_ROUTE)
    }
    var nestedNavKey by rememberSaveable(
        stateSaver = Saver({ it.toString() }, UUID::fromString),
    ) {
        mutableStateOf(UUID.randomUUID())
    }
    val nestedNavController = key(nestedNavKey) {
        rememberNavController()
    }

    fun onTransferClickShowDetailPane(transferId: String) {
        onTransferClick(transferId)
        if (listDetailNavigator.isDetailPaneVisible()) {
            // If the detail pane was visible, then use the nestedNavController navigate call
            // directly
            nestedNavController.navigateToTransferDetails(transferId) {
                popUpTo(DETAIL_PANE_NAVHOST_ROUTE)
            }
        } else {
            // Otherwise, recreate the NavHost entirely, and start at the new destination
            nestedNavHostStartDestination = createDetailsTransferRoute(transferId)
            nestedNavKey = UUID.randomUUID()
        }
        listDetailNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

    ListDetailPaneScaffold(
        value = listDetailNavigator.scaffoldValue,
        directive = listDetailNavigator.scaffoldDirective,
        listPane = {
            AnimatedPane {
                TransferRoute(
                    onTransferClick = ::onTransferClickShowDetailPane,
                    highlightSelectedTransfer = listDetailNavigator.isDetailPaneVisible(),
                    onCleanBackStack = {}
                )
            }
        },
        detailPane = {
            AnimatedPane {
                key(nestedNavKey) {
                    NavHost(
                        navController = nestedNavController,
                        startDestination = nestedNavHostStartDestination,
                        route = DETAIL_PANE_NAVHOST_ROUTE,
                    ) {
                        detailsTransferScreen(
                            showBackButton = !listDetailNavigator.isListPaneVisible(),
                            onBackClick = listDetailNavigator::navigateBack,
                        )
                        composable(route = DETAILS_TRANSFER_ROUTE) {
                            DetailsTransferPlaceholder()
                        }
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isListPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isDetailPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
