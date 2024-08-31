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

fun NavController.navigateToDetails(
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
