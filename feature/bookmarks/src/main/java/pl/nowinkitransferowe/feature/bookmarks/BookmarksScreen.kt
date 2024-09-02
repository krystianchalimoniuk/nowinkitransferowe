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

package pl.nowinkitransferowe.feature.bookmarks

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.scrollbar.DraggableScrollbar
import pl.nowinkitransferowe.core.designsystem.scrollbar.rememberDraggableScroller
import pl.nowinkitransferowe.core.designsystem.scrollbar.scrollbarState
import pl.nowinkitransferowe.core.designsystem.theme.LocalTintTheme
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.ui.NewsFeedUiState
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import pl.nowinkitransferowe.core.ui.UserNewsResourcePreviewParameterProvider
import pl.nowinkitransferowe.core.ui.newsFeed
import pl.nowinkitransferowe.core.ui.transferFeed

@Composable
internal fun BookmarksRoute(
    onNewsClick: (String) -> Unit,
    onTransferClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val newsFeedState by viewModel.newsFeedUiState.collectAsStateWithLifecycle()
    val transferFeedState by viewModel.transferFeedUiState.collectAsStateWithLifecycle()

    BookmarksScreen(
        newsFeedState = newsFeedState,
        transferFeedState = transferFeedState,
        onShowSnackbar = onShowSnackbar,
        removeFromNewsBookmarks = viewModel::removeFromSavedNewsResources,
        onNewsResourceViewed = { viewModel.setNewsResourceViewed(it, true) },
        removeFromTransferBookmarks = viewModel::removeFromSavedTransferResources,
        onTransferResourceViewed = { viewModel.setTransferResourceViewed(it, true) },
        onNewsClick = onNewsClick,
        onTransferClick = onTransferClick,
        onTopicClick = onTopicClick,
        modifier = modifier,
        shouldDisplayUndoBookmark = viewModel.shouldDisplayUndoNewsBookmark,
        undoBookmarkRemoval = viewModel::undoBookmarkRemoval,
        clearUndoState = viewModel::clearUndoState,
    )
}

/**
 * Displays the user's bookmarked articles. Includes support for loading and empty states.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun BookmarksScreen(
    newsFeedState: NewsFeedUiState,
    transferFeedState: TransferFeedUiState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    removeFromNewsBookmarks: (String) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    removeFromTransferBookmarks: (String) -> Unit,
    onTransferResourceViewed: (String) -> Unit,
    onNewsClick: (String) -> Unit,
    onTransferClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    shouldDisplayUndoBookmark: Boolean = false,
    undoBookmarkRemoval: () -> Unit = {},
    clearUndoState: () -> Unit = {},
) {
    val bookmarkRemovedMessage = stringResource(id = R.string.feature_bookmarks_removed)
    val undoText = stringResource(id = R.string.feature_bookmarks_undo)

    val itemsAvailable = feedItemsSize(newsFeedState, transferFeedState)

    LaunchedEffect(shouldDisplayUndoBookmark) {
        if (shouldDisplayUndoBookmark) {
            val snackBarResult = onShowSnackbar(bookmarkRemovedMessage, undoText)
            if (snackBarResult) {
                undoBookmarkRemoval()
            } else {
                clearUndoState()
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        clearUndoState()
    }
    if (newsFeedState == NewsFeedUiState.Loading || transferFeedState == TransferFeedUiState.Loading) {
        LoadingState()
    } else if (itemsAvailable == 0) {
        EmptyState()
    } else {
        BookmarksGrid(
            newsFeedState = newsFeedState,
            transferFeedState = transferFeedState,
            removeFromNewsBookmarks = removeFromNewsBookmarks,
            onNewsResourceViewed = onNewsResourceViewed,
            removeFromTransferBookmarks = removeFromTransferBookmarks,
            onTransferResourceViewed = onTransferResourceViewed,
            onNewsClick = onNewsClick,
            onTransferClick = onTransferClick,
            onTopicClick = onTopicClick,

        )
    }
    TrackScreenViewEvent(screenName = "Saved")
}

@Composable
fun BookmarksGrid(
    newsFeedState: NewsFeedUiState,
    transferFeedState: TransferFeedUiState,
    removeFromNewsBookmarks: (String) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    removeFromTransferBookmarks: (String) -> Unit,
    onTransferResourceViewed: (String) -> Unit,
    onNewsClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onTransferClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollableState = rememberLazyStaggeredGridState()
    TrackScrollJank(scrollableState = scrollableState, stateName = "bookmarks:grid")
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
                .testTag("bookmarks:feed"),
            state = scrollableState,
        ) {
            if (newsFeedState is NewsFeedUiState.Success && newsFeedState.feed.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(id = R.string.feature_bookmarks_news))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            newsFeed(
                feedState = newsFeedState,
                onNewsResourcesCheckedChanged = { id, _ -> removeFromNewsBookmarks(id) },
                onNewsResourceViewed = onNewsResourceViewed,
                onNewsClick = onNewsClick,
                onNewsSelected = {},
                onTopicClick = onTopicClick,
            )
            if (transferFeedState is TransferFeedUiState.Success && transferFeedState.feed.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(id = R.string.feature_bookmarks_transfers))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            transferFeed(
                transferFeedState,
                onTransferResourcesCheckedChanged = { id, _ -> removeFromTransferBookmarks(id) },
                onTransferResourceViewed = onTransferResourceViewed,
                onTransferClick = onTransferClick,
            )
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }
        val itemsAvailable = feedItemsSize(newsFeedState, transferFeedState)
        val scrollbarState = scrollableState.scrollbarState(
            itemsAvailable = itemsAvailable,
        )
        scrollableState.DraggableScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 2.dp)
                .align(Alignment.CenterEnd),
            state = scrollbarState,
            orientation = Orientation.Vertical,
            onThumbMoved = scrollableState.rememberDraggableScroller(
                itemsAvailable = itemsAvailable,
            ),
        )
    }
}

@Composable
fun feedItemsSize(newsFeedState: NewsFeedUiState, transferFeedState: TransferFeedUiState): Int {
    val newsFeedSize = when (newsFeedState) {
        NewsFeedUiState.Loading -> 0
        is NewsFeedUiState.Success -> newsFeedState.feed.size
    }
    val transferFeedSize = when (transferFeedState) {
        TransferFeedUiState.Loading -> 0
        is TransferFeedUiState.Success -> transferFeedState.feed.size
    }

    return newsFeedSize + transferFeedSize
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    NtLoadingWheel(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize()
            .testTag("forYou:loading"),
        contentDesc = stringResource(id = R.string.feature_bookmarks_loading),
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .testTag("bookmarks:empty"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val iconTint = LocalTintTheme.current.iconTint
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(id = R.drawable.feature_bookmarks_img_empty_bookmarks),
            colorFilter = if (iconTint != Color.Unspecified) ColorFilter.tint(iconTint) else null,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(id = R.string.feature_bookmarks_empty_error),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.feature_bookmarks_empty_description),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
private fun LoadingStatePreview() {
    NtTheme {
        LoadingState()
    }
}

@Preview
@Composable
private fun BookmarksGridPreview(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    NtTheme {
        BookmarksGrid(
            newsFeedState = NewsFeedUiState.Success(userNewsResources),
            transferFeedState = TransferFeedUiState.Success(emptyList()),
            removeFromNewsBookmarks = {},
            removeFromTransferBookmarks = {},
            onNewsResourceViewed = {},
            onTransferResourceViewed = {},
            onNewsClick = {},
            onTopicClick = {},
            onTransferClick = {},
        )
    }
}

@Preview
@Composable
private fun EmptyStatePreview() {
    NtTheme {
        EmptyState()
    }
}
