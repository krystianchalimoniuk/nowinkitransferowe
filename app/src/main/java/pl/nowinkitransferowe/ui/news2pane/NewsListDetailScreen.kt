package pl.nowinkitransferowe.ui.news2pane

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
import pl.nowinkitransferowe.feature.details.DetailPlaceholder
import pl.nowinkitransferowe.feature.details.navigation.DETAILS_ROUTE
import pl.nowinkitransferowe.feature.details.navigation.LINKED_NEWS_RESOURCE_ID
import pl.nowinkitransferowe.feature.details.navigation.createDetailsRoute
import pl.nowinkitransferowe.feature.details.navigation.detailsScreen
import pl.nowinkitransferowe.feature.details.navigation.navigateToDetails
import pl.nowinkitransferowe.feature.news.NewsRoute
import pl.nowinkitransferowe.feature.news.navigation.NEWS_ROUTE
import java.util.UUID

private const val DETAIL_PANE_NAVHOST_ROUTE = "detail_pane_route"
private const val DEEP_LINK_URI_PATTERN =
    "http://nowinkitransferowe.pl/news/{$LINKED_NEWS_RESOURCE_ID}"

fun NavGraphBuilder.newsListDetailScreen(onTopicClick: (String) -> Unit) {
    composable(
        route = NEWS_ROUTE,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = DEEP_LINK_URI_PATTERN
                action = Intent.ACTION_VIEW
            },
        ),
        arguments = listOf(
            navArgument(LINKED_NEWS_RESOURCE_ID) {
                type = NavType.StringType
                defaultValue = null
                nullable = true
            },
        ),
    ) {
        NewsListDetailScreen(onTopicClick)
    }
}

@Composable
internal fun NewsListDetailScreen(
    onTopicClick: (String) -> Unit,
    viewModel: News2PaneViewModel = hiltViewModel(),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),

    ) {
    val selectedNewsId by viewModel.selectedNewsId.collectAsStateWithLifecycle()
    NewsListDetailScreen(
        selectedNewsId = selectedNewsId,
        onNewsClick = viewModel::onNewsClick,
        onTopicClick = onTopicClick,
        windowAdaptiveInfo = windowAdaptiveInfo

    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun NewsListDetailScreen(
    selectedNewsId: String?,
    onNewsClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    windowAdaptiveInfo: WindowAdaptiveInfo,


    ) {
    val listDetailNavigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = calculatePaneScaffoldDirective(windowAdaptiveInfo),
        initialDestinationHistory = listOfNotNull(
            ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List),
            ThreePaneScaffoldDestinationItem<Nothing>(ListDetailPaneScaffoldRole.Detail).takeIf {
                selectedNewsId != null
            },
        ),
    )
    BackHandler(listDetailNavigator.canNavigateBack()) {
        listDetailNavigator.navigateBack()
    }

    var nestedNavHostStartDestination by remember {
        mutableStateOf(selectedNewsId?.let(::createDetailsRoute) ?: DETAILS_ROUTE)
    }
    var nestedNavKey by rememberSaveable(
        stateSaver = Saver({ it.toString() }, UUID::fromString),
    ) {
        mutableStateOf(UUID.randomUUID())
    }
    val nestedNavController = key(nestedNavKey) {
        rememberNavController()
    }

    fun onNewsClickShowDetailPane(newsId: String) {
        onNewsClick(newsId)
        if (listDetailNavigator.isDetailPaneVisible()) {
            // If the detail pane was visible, then use the nestedNavController navigate call
            // directly
            nestedNavController.navigateToDetails(newsId) {
                popUpTo(DETAIL_PANE_NAVHOST_ROUTE)
            }
        } else {
            // Otherwise, recreate the NavHost entirely, and start at the new destination
            nestedNavHostStartDestination = createDetailsRoute(newsId)
            nestedNavKey = UUID.randomUUID()
        }
        listDetailNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

    ListDetailPaneScaffold(
        value = listDetailNavigator.scaffoldValue,
        directive = listDetailNavigator.scaffoldDirective,
        listPane = {
            AnimatedPane {
                NewsRoute(
                    onNewsClick = ::onNewsClickShowDetailPane,
                    onTopicClick = onTopicClick,
                    highlightSelectedNews = listDetailNavigator.isDetailPaneVisible(),
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
                        detailsScreen(
                            showBackButton = !listDetailNavigator.isListPaneVisible(),
                            onBackClick = listDetailNavigator::navigateBack,
                            onTopicClick = onTopicClick,
                        )
                        composable(route = DETAILS_ROUTE) {
                            DetailPlaceholder()
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
