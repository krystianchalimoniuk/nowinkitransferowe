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

package pl.nowinkitransferowe.feature.bookmarks.impl

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.scrollbar.DraggableScrollbar
import pl.nowinkitransferowe.core.designsystem.scrollbar.rememberDraggableScroller
import pl.nowinkitransferowe.core.designsystem.scrollbar.scrollbarState
import pl.nowinkitransferowe.core.designsystem.theme.LocalTintTheme
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.ui.ArticleFeedUiState
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import pl.nowinkitransferowe.core.ui.UserArticlePreviewParameterProvider
import pl.nowinkitransferowe.core.ui.articleFeed
import pl.nowinkitransferowe.core.ui.transferFeed

@Composable
internal fun BookmarksRoute(
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
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
        removeFromArticleBookmarks = viewModel::removeArticleBookmark,
        onArticleViewed = { viewModel.setArticleViewed(it, true) },
        removeFromTransferBookmarks = viewModel::removeTransferBookmark,
        onTransferViewed = { viewModel.setTransferViewed(it, true) },
        onArticleClick = onArticleClick,
        onTransferClick = onTransferClick,
        onTopicClick = onTopicClick,
        modifier = modifier,
        shouldDisplayUndoBookmark = viewModel.shouldDisplayUndoBookmark,
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
    newsFeedState: ArticleFeedUiState,
    transferFeedState: TransferFeedUiState,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    removeFromArticleBookmarks: (Int) -> Unit,
    onArticleViewed: (Int) -> Unit,
    removeFromTransferBookmarks: (Int) -> Unit,
    onTransferViewed: (List<Int>) -> Unit,
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    shouldDisplayUndoBookmark: Boolean = false,
    undoBookmarkRemoval: () -> Unit = {},
    clearUndoState: () -> Unit = {},
) {
    val bookmarkRemovedMessage = stringResource(id = R.string.feature_bookmarks_impl_removed)
    val undoText = stringResource(id = R.string.feature_bookmarks_impl_undo)

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
    if (newsFeedState == ArticleFeedUiState.Loading || transferFeedState == TransferFeedUiState.Loading) {
        LoadingState()
    } else if (itemsAvailable == 0) {
        EmptyState()
    } else {
        BookmarksGrid(
            newsFeedState = newsFeedState,
            transferFeedState = transferFeedState,
            removeFromArticleBookmarks = removeFromArticleBookmarks,
            onArticleViewed = onArticleViewed,
            removeFromTransferBookmarks = removeFromTransferBookmarks,
            onTransferViewed = onTransferViewed,
            onArticleClick = onArticleClick,
            onTransferClick = onTransferClick,
            onTopicClick = onTopicClick,

        )
    }
    TrackScreenViewEvent(screenName = "Saved")
}

@Composable
fun BookmarksGrid(
    newsFeedState: ArticleFeedUiState,
    transferFeedState: TransferFeedUiState,
    removeFromArticleBookmarks: (Int) -> Unit,
    onArticleViewed: (Int) -> Unit,
    removeFromTransferBookmarks: (Int) -> Unit,
    onTransferViewed: (List<Int>) -> Unit,
    onArticleClick: (Int) -> Unit,
    onTopicClick: (String) -> Unit,
    onTransferClick: (Int) -> Unit,
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
            if (newsFeedState is ArticleFeedUiState.Success && newsFeedState.feed.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(id = R.string.feature_bookmarks_impl_news))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            articleFeed(
                feedState = newsFeedState,
                onArticleSavedChanged = { id, _ -> removeFromArticleBookmarks(id) },
                onArticleViewed = onArticleViewed,
                onArticleClick = onArticleClick,
                onArticleSelected = {},
                onTopicClick = onTopicClick,
            )
            if (transferFeedState is TransferFeedUiState.Success && transferFeedState.feed.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(id = R.string.feature_bookmarks_impl_transfers))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            transferFeed(
                transferFeedState,
                onTransferSavedChanged = { id, _ -> removeFromTransferBookmarks(id) },
                onTransferViewed = onTransferViewed,
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
fun feedItemsSize(newsFeedState: ArticleFeedUiState, transferFeedState: TransferFeedUiState): Int {
    val newsFeedSize = when (newsFeedState) {
        ArticleFeedUiState.Loading -> 0
        is ArticleFeedUiState.Success -> newsFeedState.feed.size
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
            .testTag("bookmarks:loading"),
        contentDesc = stringResource(id = R.string.feature_bookmarks_impl_loading),
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
            painter = painterResource(id = R.drawable.feature_bookmarks_impl_img_empty_bookmarks),
            colorFilter = if (iconTint != Color.Unspecified) ColorFilter.tint(iconTint) else null,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(id = R.string.feature_bookmarks_impl_empty_error),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.feature_bookmarks_impl_empty_description),
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
    @PreviewParameter(UserArticlePreviewParameterProvider::class)
    userArticles: List<UserArticle>,
) {
    NtTheme {
        BookmarksGrid(
            newsFeedState = ArticleFeedUiState.Success(userArticles),
            transferFeedState = TransferFeedUiState.Success(emptyList()),
            removeFromArticleBookmarks = {},
            removeFromTransferBookmarks = {},
            onArticleViewed = {},
            onTransferViewed = {},
            onArticleClick = {},
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
