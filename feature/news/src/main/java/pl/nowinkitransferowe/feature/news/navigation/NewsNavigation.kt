package pl.nowinkitransferowe.feature.news.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import pl.nowinkitransferowe.feature.news.NewsRoute

const val NEWS_ROUTE = "news_route"
const val LINKED_NEWS_RESOURCE_ID = "linkedNewsResourceId"


fun NavController.navigateToNews(navOptions: NavOptions) = navigate(NEWS_ROUTE, navOptions)

fun NavGraphBuilder.newsScreen(onNewsClick: (String) -> Unit, onTopicClick: (String) -> Unit) {
    composable(
        route = NEWS_ROUTE,
    ) {
        NewsRoute(onNewsClick, onTopicClick)
    }
}
