package pl.nowinkitransferowe.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.nowinkitransferowe.core.analytics.LocalAnalyticsHelper
import androidx.compose.foundation.lazy.staggeredgrid.items
import pl.nowinkitransferowe.core.model.UserTransferResource


/**
 * An extension on [LazyListScope] defining a feed with news resources.
 * Depending on the [feedState], this might emit no items.
 */
@OptIn(ExperimentalFoundationApi::class)
fun LazyStaggeredGridScope.transferFeed(
    feedState: TransferFeedUiState,
    onTransferResourcesCheckedChanged: (String, Boolean) -> Unit,
    onTransferResourceViewed: (String) -> Unit,
    onTransferClick: () -> Unit = {},
) {
    when (feedState) {
        TransferFeedUiState.Loading -> Unit
        is TransferFeedUiState.Success -> {
            items(
                items = feedState.feed,
                key = { "transfer-${it.id}" },
                contentType = { "transferFeedItem" },
            ) { userTransferResource ->
                val analyticsHelper = LocalAnalyticsHelper.current

                TransferResourceCardExpanded(
                    userTransferResource = userTransferResource,
                    isBookmarked = userTransferResource.isSaved,
                    onClick = {
                        onTransferClick()
                        analyticsHelper.logNewsResourceOpened(
                            newsResourceId = userTransferResource.id,
                        )
                        onTransferResourceViewed(userTransferResource.id)
                    },
                    hasBeenViewed = userTransferResource.hasBeenViewed,
                    onToggleBookmark = {
                        onTransferResourcesCheckedChanged(
                            userTransferResource.id,
                            !userTransferResource.isSaved,
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .animateItemPlacement(),
                )
            }
        }
    }
}

//fun launchCustomChromeTab(context: Context, uri: Uri, @ColorInt toolbarColor: Int) {
//    val customTabBarColor = CustomTabColorSchemeParams.Builder()
//        .setToolbarColor(toolbarColor).build()
//    val customTabsIntent = CustomTabsIntent.Builder()
//        .setDefaultColorSchemeParams(customTabBarColor)
//        .build()
//
//    customTabsIntent.launchUrl(context, uri)
//}
/**
 * A sealed hierarchy describing the state of the feed of news resources.
 */
sealed interface TransferFeedUiState {
    /**
     * The feed is still loading.
     */
    data object Loading : TransferFeedUiState

    /**
     * The feed is loaded with the given list of news resources.
     */
    data class Success(
        /**
         * The list of news resources contained in this feed.
         */
        val feed: List<UserTransferResource>,
    ) : TransferFeedUiState
}
