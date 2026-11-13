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

package pl.nowinkitransferowe.feature.articles.impl

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus.Denied
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.collectLatest
import pl.nowinkitransferowe.core.designsystem.component.NtOverlayLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtSectionHeader
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.ui.ArticleHeroCard
import pl.nowinkitransferowe.core.ui.ArticleListItem
import pl.nowinkitransferowe.core.ui.RumourCarouselCard
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransferCarouselCard

@Composable
fun ArticlesRoute(
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    onAllTransfersClick: () -> Unit,
    onRumourClick: (Int) -> Unit,
    onAllRumoursClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlightSelectedArticle: Boolean = false,
    viewModel: ArticlesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val selectedArticleId by viewModel.selectedArticleId.collectAsStateWithLifecycle()

    ArticlesScreen(
        uiState = uiState,
        isSyncing = isSyncing,
        selectedArticleId = selectedArticleId,
        highlightSelectedArticle = highlightSelectedArticle,
        onTabSelected = viewModel::selectTab,
        onTopicSelected = viewModel::selectTopic,
        onArticleClick = { id ->
            viewModel.onArticleClick(id)
            viewModel.setArticleViewed(id, true)
            onArticleClick(id)
        },
        onArticleSavedChanged = viewModel::updateArticleSaved,
        onTransferClick = onTransferClick,
        onAllTransfersClick = onAllTransfersClick,
        onRumourClick = { rumour -> onRumourClick(rumour.id) },
        onAllRumoursClick = onAllRumoursClick,
        loadNextPage = viewModel::loadNextPage,
        modifier = modifier,
    )
}

/**
 * The home screen as on the website: the kind tabs, topic chips, the featured article, the
 * transfer and rumour carousels and the latest articles.
 */
@Composable
internal fun ArticlesScreen(
    uiState: HomeUiState,
    isSyncing: Boolean,
    onTabSelected: (HomeTab) -> Unit,
    onTopicSelected: (String?) -> Unit,
    onArticleClick: (Int) -> Unit,
    onArticleSavedChanged: (Int, Boolean) -> Unit,
    onTransferClick: (Int) -> Unit,
    onAllTransfersClick: () -> Unit,
    onRumourClick: (Rumour) -> Unit,
    onAllRumoursClick: () -> Unit,
    loadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
    selectedArticleId: Int? = null,
    highlightSelectedArticle: Boolean = false,
) {
    ReportDrawnWhen { !isSyncing && uiState is HomeUiState.Success }

    val listState = rememberLazyListState()
    TrackScrollJank(scrollableState = listState, stateName = "news:feed")

    LaunchedEffect(uiState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index to listState.layoutInfo.totalItemsCount }
            .collectLatest { (last, total) -> if (last != null && last >= total - 2) loadNextPage() }
    }

    Box(modifier.fillMaxSize()) {
        when (uiState) {
            HomeUiState.Loading -> Unit
            is HomeUiState.Success -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .widthIn(max = MaxContentWidth)
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .testTag("news:feed"),
            ) {
                item(key = "tabs") { HomeTabs(uiState.tab, onTabSelected) }
                item(key = "topics") { TopicChips(uiState.topics, uiState.topic, onTopicSelected) }
                uiState.featured?.let { featured ->
                    item(key = "featured:${featured.id}") {
                        ArticleHeroCard(
                            article = featured,
                            onClick = { onArticleClick(featured.id) },
                            onToggleBookmark = { onArticleSavedChanged(featured.id, !featured.isSaved) },
                            selected = highlightSelectedArticle && featured.id == selectedArticleId,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
                if (uiState.showTransfers) {
                    item(key = "transfers") {
                        TransfersCarousel(uiState.transfers, onTransferClick, onAllTransfersClick)
                    }
                }
                if (uiState.showRumours) {
                    item(key = "rumours") {
                        RumoursCarousel(uiState.rumours, onRumourClick, onAllRumoursClick)
                    }
                }
                if (uiState.latest.isNotEmpty()) {
                    item(key = "latest") {
                        NtSectionHeader(stringResource(R.string.feature_articles_impl_latest), Modifier.padding(horizontal = 16.dp))
                    }
                }
                items(uiState.latest, key = { it.id }, contentType = { "article" }) { article ->
                    ArticleListItem(
                        article = article,
                        onClick = { onArticleClick(article.id) },
                        onToggleBookmark = { onArticleSavedChanged(article.id, !article.isSaved) },
                        selected = highlightSelectedArticle && article.id == selectedArticleId,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        }
        if (isSyncing || uiState is HomeUiState.Loading) {
            NtOverlayLoadingWheel(
                contentDesc = stringResource(R.string.feature_articles_impl_loading),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
            )
        }
    }
    TrackScreenViewEvent(screenName = "Home")
    NotificationPermissionEffect()
}

/** Wide windows without a detail pane keep the feed readable instead of stretching the cards. */
private val MaxContentWidth = 720.dp

@Composable
private fun HomeTabs(selected: HomeTab, onSelected: (HomeTab) -> Unit) {
    PrimaryTabRow(selectedTabIndex = selected.ordinal) {
        HomeTab.entries.forEach { tab ->
            Tab(
                selected = tab == selected,
                onClick = { onSelected(tab) },
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = {
                    Text(
                        stringResource(
                            when (tab) {
                                HomeTab.ALL -> R.string.feature_articles_impl_tab_all
                                HomeTab.OFFICIAL -> R.string.feature_articles_impl_tab_official
                                HomeTab.RUMOURS -> R.string.feature_articles_impl_tab_rumours
                            },
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun TopicChips(topics: List<Topic>, selected: String?, onSelected: (String?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text(stringResource(R.string.feature_articles_impl_all_topics)) },
            )
        }
        items(topics, key = { it.slug }) { topic ->
            FilterChip(
                selected = topic.slug == selected,
                onClick = { onSelected(if (topic.slug == selected) null else topic.slug) },
                label = { Text(topic.name) },
            )
        }
    }
}

@Composable
private fun TransfersCarousel(transfers: List<UserTransfer>, onTransferClick: (Int) -> Unit, onAllClick: () -> Unit) {
    NtSectionHeader(stringResource(R.string.feature_articles_impl_transfer_window), Modifier.padding(horizontal = 16.dp), onClick = onAllClick)
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(transfers, key = { it.id }) { transfer ->
            TransferCarouselCard(transfer, onClick = { onTransferClick(transfer.id) })
        }
    }
}

@Composable
private fun RumoursCarousel(rumours: List<Rumour>, onRumourClick: (Rumour) -> Unit, onAllClick: () -> Unit) {
    NtSectionHeader(stringResource(R.string.feature_articles_impl_rumours), Modifier.padding(horizontal = 16.dp).padding(top = 8.dp), onClick = onAllClick)
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(rumours, key = { it.id }) { rumour ->
            RumourCarouselCard(rumour, onClick = { onRumourClick(rumour) })
        }
    }
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
