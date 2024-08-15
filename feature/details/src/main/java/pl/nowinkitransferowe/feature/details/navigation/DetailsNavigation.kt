package pl.nowinkitransferowe.feature.details.navigation

import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
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
