package pl.nowinkitransferowe.feature.transfers.navigation

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import pl.nowinkitransferowe.core.designsystem.component.NtOverlayLoadingWheel
import pl.nowinkitransferowe.core.designsystem.scrollbar.DraggableScrollbar
import pl.nowinkitransferowe.core.designsystem.scrollbar.rememberDraggableScroller
import pl.nowinkitransferowe.core.designsystem.scrollbar.scrollbarState
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.ui.DevicePreviews
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import pl.nowinkitransferowe.core.ui.UserNewsResourcePreviewParameterProvider
import pl.nowinkitransferowe.core.ui.transferFeed
import pl.nowinkitransferowe.feature.transfers.R

@Composable
internal fun TransferRoute(
    modifier: Modifier = Modifier,
    onCleanBackStack: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel(),
) {
    val feedState by viewModel.feedUiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val deepLinkedUserTransferResource by viewModel.deepLinkedTransferResource.collectAsStateWithLifecycle()
    val page by viewModel.page.collectAsStateWithLifecycle()
    val transfersCounts by viewModel.transfersCount.collectAsStateWithLifecycle()
    TransferScreen(
        isSyncing = isSyncing,
        feedState = feedState,
        deepLinkedUserTransferResource = deepLinkedUserTransferResource,
        onDeepLinkOpened = viewModel::onDeepLinkOpened,
        onCleanBackStack = onCleanBackStack,
        onTransferResourcesCheckedChanged = viewModel::updateTransferResourceSaved,
        onTransferResourceViewed = { viewModel.setTransferResourceViewed(it, true) },
        loadNextPage = { viewModel.loadNextPage(page = page, transferCount = transfersCounts) },
        modifier = modifier,
    )
}


@Composable
internal fun TransferScreen(
    isSyncing: Boolean,
    feedState: TransferFeedUiState,
    deepLinkedUserTransferResource: UserTransferResource?,
    onDeepLinkOpened: (String) -> Unit,
    onCleanBackStack: () -> Unit,
    onTransferResourcesCheckedChanged: (String, Boolean) -> Unit,
    onTransferResourceViewed: (String) -> Unit,
    loadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DeepLinkEffect(
        deepLinkedUserTransferResource,
        onDeepLinkOpened,
        onCleanBackStack
    )
    val isFeedLoading = feedState is TransferFeedUiState.Loading

    // This code should be called when the UI is ready for use and relates to Time To Full Display.
    ReportDrawnWhen { !isSyncing && !isFeedLoading }

    val itemsAvailable = feedItemsSize(feedState)

    val state = rememberLazyStaggeredGridState()
    val scrollbarState = state.scrollbarState(
        itemsAvailable = itemsAvailable,
    )
    LaunchedEffect(feedState) {
        snapshotFlow {
            // Get the index of the last visible item
            val layoutInfo = state.layoutInfo
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisibleItemIndex
        }.collectLatest { lastVisibleItemIndex ->
            if (lastVisibleItemIndex != null && lastVisibleItemIndex >= itemsAvailable - 1) {
                loadNextPage()
            }
        }
    }
    TrackScrollJank(scrollableState = state, stateName = "transfers:feed")

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(300.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 24.dp,
            modifier = Modifier
                .testTag("transfers:feed"),
            state = state,
        ) {

            transferFeed(
                feedState = feedState,
                onTransferResourcesCheckedChanged = onTransferResourcesCheckedChanged,
                onTransferResourceViewed = onTransferResourceViewed,
            )

            item(span = StaggeredGridItemSpan.FullLine, contentType = "bottomSpacing") {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Add space for the content to clear the "offline" snackbar.
                    // TODO: Check that the Scaffold handles this correctly in NiaApp
                    // if (isOffline) Spacer(modifier = Modifier.height(48.dp))
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
                }
            }
        }
        AnimatedVisibility(
            visible = isSyncing || isFeedLoading,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> -fullHeight },
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
            ) + fadeOut(),
        ) {
            val loadingContentDescription = stringResource(id = R.string.feature_transfers_loading)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                NtOverlayLoadingWheel(
                    modifier = Modifier
                        .align(Alignment.Center),
                    contentDesc = loadingContentDescription,
                )
            }
        }
        state.DraggableScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 2.dp)
                .align(Alignment.CenterEnd),
            state = scrollbarState,
            orientation = Orientation.Vertical,
            onThumbMoved = state.rememberDraggableScroller(
                itemsAvailable = itemsAvailable,
            ),
        )
    }
    TrackScreenViewEvent(screenName = "Transfer")

}

@Composable
private fun DeepLinkEffect(
    userTransferResource: UserTransferResource?,
    onDeepLinkOpened: (String) -> Unit,
    onCleanBackStack: () -> Unit,
) {

    LaunchedEffect(userTransferResource) {
        if (userTransferResource == null) return@LaunchedEffect
        else {
            onDeepLinkOpened(userTransferResource.id)
            onCleanBackStack()

        }

    }
}

private fun feedItemsSize(
    feedState: TransferFeedUiState,
): Int {
    val feedSize = when (feedState) {
        TransferFeedUiState.Loading -> 0
        is TransferFeedUiState.Success -> feedState.feed.size
    }

    return feedSize
}

@DevicePreviews
@Composable
fun TransferScreenLoading() {
    NtTheme {
        TransferScreen(
            isSyncing = false,
            feedState = TransferFeedUiState.Loading,
            deepLinkedUserTransferResource = null,
            onTransferResourcesCheckedChanged = { _, _ -> },
            onTransferResourceViewed = {},
            onDeepLinkOpened = {},
            onCleanBackStack = {},
            loadNextPage = {}
        )
    }
}

@DevicePreviews
@Composable
fun TransferScreenPopulatedAndLoading(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userTransferResources: List<UserTransferResource>,
) {
    NtTheme {
        TransferScreen(
            isSyncing = true,
            feedState = TransferFeedUiState.Success(
                feed = userTransferResources,
            ),
            deepLinkedUserTransferResource = null,
            onTransferResourcesCheckedChanged = { _, _ -> },
            onTransferResourceViewed = {},
            onDeepLinkOpened = {},
            onCleanBackStack = {},
            loadNextPage = {}
        )
    }
}
