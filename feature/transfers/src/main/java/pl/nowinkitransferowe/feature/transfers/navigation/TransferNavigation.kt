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

fun NavGraphBuilder.transferScreen(onCleanBackStack: () -> Unit) {
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
        TransferRoute(onCleanBackStack = onCleanBackStack)
    }
}