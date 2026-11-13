/*
 * Copyright 2026 The Android Open Source Project
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.theme.LocalTintTheme
import pl.nowinkitransferowe.core.ui.ArticleListItem
import pl.nowinkitransferowe.core.ui.BookmarkButton
import pl.nowinkitransferowe.core.ui.RumourListItem
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransferListItem

@Composable
internal fun BookmarksRoute(
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    onRumourClick: (Int) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BookmarksScreen(
        uiState = uiState,
        onTabSelected = viewModel::selectTab,
        onShowSnackbar = onShowSnackbar,
        removeArticleBookmark = viewModel::removeArticleBookmark,
        removeTransferBookmark = viewModel::removeTransferBookmark,
        removeRumourBookmark = viewModel::removeRumourBookmark,
        onArticleClick = onArticleClick,
        onTransferClick = onTransferClick,
        onRumourClick = onRumourClick,
        modifier = modifier,
        shouldDisplayUndoBookmark = viewModel.shouldDisplayUndoBookmark,
        undoBookmarkRemoval = viewModel::undoBookmarkRemoval,
        clearUndoState = viewModel::clearUndoState,
    )
}

/**
 * The saved articles and transfers, each kind in its own tab. Removing a bookmark shows a
 * snackbar that can bring it back.
 */
@Composable
internal fun BookmarksScreen(
    uiState: BookmarksUiState,
    onTabSelected: (SavedTab) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    removeArticleBookmark: (Int) -> Unit,
    removeTransferBookmark: (Int) -> Unit,
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    removeRumourBookmark: (Int) -> Unit = {},
    onRumourClick: (Int) -> Unit = {},
    shouldDisplayUndoBookmark: Boolean = false,
    undoBookmarkRemoval: () -> Unit = {},
    clearUndoState: () -> Unit = {},
) {
    val bookmarkRemovedMessage = stringResource(id = R.string.feature_bookmarks_impl_removed)
    val undoText = stringResource(id = R.string.feature_bookmarks_impl_undo)

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

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (uiState) {
            BookmarksUiState.Loading -> NtLoadingWheel(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .testTag("bookmarks:loading"),
                contentDesc = stringResource(id = R.string.feature_bookmarks_impl_loading),
            )

            is BookmarksUiState.Success -> Column(
                Modifier
                    .widthIn(max = MaxContentWidth)
                    .fillMaxSize(),
            ) {
                SavedTabs(uiState, onTabSelected)
                when (uiState.tab) {
                    SavedTab.ARTICLES -> SavedList(
                        items = uiState.articles,
                        key = { it.id },
                        emptyText = R.string.feature_bookmarks_impl_empty_articles,
                    ) { article ->
                        ArticleListItem(
                            article = article,
                            onClick = { onArticleClick(article.id) },
                            onToggleBookmark = { removeArticleBookmark(article.id) },
                        )
                    }

                    SavedTab.TRANSFERS -> SavedList(
                        items = uiState.transfers,
                        key = { it.id },
                        emptyText = R.string.feature_bookmarks_impl_empty_transfers,
                    ) { transfer ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TransferListItem(
                                transfer = transfer,
                                onClick = { onTransferClick(transfer.id) },
                                modifier = Modifier.weight(1f),
                            )
                            BookmarkButton(isBookmarked = true, onClick = { removeTransferBookmark(transfer.id) })
                        }
                    }

                    SavedTab.RUMOURS -> SavedList(
                        items = uiState.rumours,
                        key = { it.id },
                        emptyText = R.string.feature_bookmarks_impl_empty_rumours,
                    ) { rumour ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RumourListItem(
                                rumour = rumour,
                                onClick = { onRumourClick(rumour.id) },
                                modifier = Modifier.weight(1f),
                            )
                            BookmarkButton(isBookmarked = true, onClick = { removeRumourBookmark(rumour.id) })
                        }
                    }
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "Saved")
}

@Composable
private fun SavedTabs(uiState: BookmarksUiState.Success, onSelected: (SavedTab) -> Unit) {
    PrimaryTabRow(selectedTabIndex = uiState.tab.ordinal) {
        SavedTab.entries.forEach { tab ->
            val (label, count) = when (tab) {
                SavedTab.ARTICLES -> R.string.feature_bookmarks_impl_articles to uiState.articles.size
                SavedTab.TRANSFERS -> R.string.feature_bookmarks_impl_transfers to uiState.transfers.size
                SavedTab.RUMOURS -> R.string.feature_bookmarks_impl_rumours to uiState.rumours.size
            }
            Tab(
                selected = tab == uiState.tab,
                onClick = { onSelected(tab) },
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = { Text(stringResource(R.string.feature_bookmarks_impl_tab, stringResource(label), count)) },
            )
        }
    }
}

@Composable
private fun <T> SavedList(
    items: List<T>,
    key: (T) -> Any,
    emptyText: Int,
    itemContent: @Composable (T) -> Unit,
) {
    if (items.isEmpty()) {
        EmptyState(emptyText)
        return
    }
    val listState = rememberLazyListState()
    TrackScrollJank(scrollableState = listState, stateName = "bookmarks:feed")
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("bookmarks:feed"),
    ) {
        items(items, key = key) { itemContent(it) }
    }
}

@Composable
private fun EmptyState(text: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(32.dp)
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
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(id = text),
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Wide windows keep the list readable instead of stretching the rows. */
private val MaxContentWidth = 720.dp
