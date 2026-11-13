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

package pl.nowinkitransferowe.feature.articles.impl

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus.Denied
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.collectLatest
import pl.nowinkitransferowe.core.designsystem.component.NtOverlayLoadingWheel
import pl.nowinkitransferowe.core.designsystem.scrollbar.DraggableScrollbar
import pl.nowinkitransferowe.core.designsystem.scrollbar.rememberDraggableScroller
import pl.nowinkitransferowe.core.designsystem.scrollbar.scrollbarState
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.ui.ArticleFeedUiState
import pl.nowinkitransferowe.core.ui.DevicePreviews
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.UserArticlePreviewParameterProvider
import pl.nowinkitransferowe.core.ui.articleFeed

@Composable
fun ArticlesRoute(
    onArticleClick: (Int) -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    highlightSelectedArticle: Boolean = false,
    viewModel: ArticlesViewModel = hiltViewModel(),
) {
    val feedState by viewModel.feedState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val selectedArticleId by viewModel.selectedArticleId.collectAsStateWithLifecycle()
    val page by viewModel.page.collectAsStateWithLifecycle()
    val newsCounts by viewModel.newsCount.collectAsStateWithLifecycle()

    ArticlesScreen(
        modifier = modifier,
        isSyncing = isSyncing,
        feedState = feedState,
        onArticleClick = onArticleClick,
        highlightSelectedArticle = highlightSelectedArticle,
        loadNextPage = { viewModel.loadNextPage(page, newsCounts) },
        selectedArticleId = selectedArticleId,
        onTopicClick = onTopicClick,
        onArticleSavedChanged = viewModel::updateArticleSaved,
        onArticleViewed = { viewModel.setArticleViewed(it, true) },
        onArticleSelected = viewModel::onArticleClick,
    )
}

@Composable
internal fun ArticlesScreen(
    modifier: Modifier = Modifier,
    isSyncing: Boolean,
    feedState: ArticleFeedUiState,
    selectedArticleId: Int? = null,
    highlightSelectedArticle: Boolean = false,
    loadNextPage: () -> Unit,
    onArticleClick: (Int) -> Unit,
    onTopicClick: (String) -> Unit,
    onArticleSavedChanged: (Int, Boolean) -> Unit,
    onArticleViewed: (Int) -> Unit,
    onArticleSelected: (Int?) -> Unit,
) {
    val isFeedLoading = feedState is ArticleFeedUiState.Loading

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
    TrackScrollJank(scrollableState = state, stateName = "news:feed")

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
                .testTag("news:feed"),
            state = state,
        ) {
            articleFeed(
                feedState = feedState,
                onArticleSavedChanged = onArticleSavedChanged,
                onArticleViewed = onArticleViewed,
                selectedArticleId = selectedArticleId,
                highlightSelectedArticle = highlightSelectedArticle,
                onArticleClick = onArticleClick,
                onTopicClick = onTopicClick,
                onArticleSelected = onArticleSelected,
            )
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
            val loadingContentDescription = stringResource(id = R.string.feature_articles_impl_loading)
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
    TrackScreenViewEvent(screenName = "News")
    NotificationPermissionEffect()
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun NotificationPermissionEffect() {
    // Permission requests should only be made from an Activity Context, which is not present
    // in previews
    if (LocalInspectionMode.current) return
    if (VERSION.SDK_INT < VERSION_CODES.TIRAMISU) return
    val notificationsPermissionState = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS,
    )
    LaunchedEffect(notificationsPermissionState) {
        val status = notificationsPermissionState.status
        if (status is Denied && !status.shouldShowRationale) {
            notificationsPermissionState.launchPermissionRequest()
        }
    }
}

private fun feedItemsSize(
    feedState: ArticleFeedUiState,
): Int {
    val feedSize = when (feedState) {
        ArticleFeedUiState.Loading -> 0
        is ArticleFeedUiState.Success -> feedState.feed.size
    }
    return feedSize
}

@DevicePreviews
@Composable
fun ForYouScreenLoading() {
    NtTheme {
        ArticlesScreen(
            isSyncing = false,
            feedState = ArticleFeedUiState.Loading,
            onArticleClick = {},
            onTopicClick = {},
            loadNextPage = {},
            onArticleSavedChanged = { _, _ -> },
            onArticleSelected = {},
            onArticleViewed = {},
        )
    }
}

@DevicePreviews
@Composable
fun ForYouScreenPopulatedAndLoading(
    @PreviewParameter(UserArticlePreviewParameterProvider::class)
    userArticles: List<UserArticle>,
) {
    NtTheme {
        ArticlesScreen(
            isSyncing = true,
            feedState = ArticleFeedUiState.Success(
                feed = userArticles,
            ),
            onArticleClick = {},
            onTopicClick = {},
            loadNextPage = {},
            onArticleSavedChanged = { _, _ -> },
            onArticleSelected = {},
            onArticleViewed = {},
        )
    }
}
