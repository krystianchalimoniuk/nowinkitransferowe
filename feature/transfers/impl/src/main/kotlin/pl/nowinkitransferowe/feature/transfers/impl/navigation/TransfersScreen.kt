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

package pl.nowinkitransferowe.feature.transfers.impl.navigation

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import pl.nowinkitransferowe.core.designsystem.component.NtTextButton
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.ui.FollowedChip
import pl.nowinkitransferowe.core.ui.SyncIndicator
import pl.nowinkitransferowe.core.ui.SyncRefreshBox
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransferListItem
import pl.nowinkitransferowe.feature.transfers.impl.R

@Composable
fun TransfersRoute(
    onTransferClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightSelectedTransfer: Boolean = false,
    viewModel: TransfersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val selectedTransferId by viewModel.selectedTransferId.collectAsStateWithLifecycle()
    TransfersScreen(
        uiState = uiState,
        isSyncing = isSyncing,
        onRefresh = viewModel::refresh,
        selectedTransferId = selectedTransferId,
        highlightSelectedTransfer = highlightSelectedTransfer,
        onSeasonSelected = viewModel::selectSeason,
        onTopicSelected = viewModel::selectTopic,
        onTransferClick = { id ->
            viewModel.onTransferClick(id)
            viewModel.setTransfersViewed(listOf(id), true)
            onTransferClick(id)
        },
        onMarkAsRead = viewModel::markAllRead,
        loadNextPage = viewModel::loadNextPage,
        modifier = modifier,
    )
}

/**
 * The transfers of a season as on the website, filtered by league, newest first.
 */
@Composable
internal fun TransfersScreen(
    uiState: TransfersUiState,
    isSyncing: Boolean,
    onSeasonSelected: (Season) -> Unit,
    onTopicSelected: (String?) -> Unit,
    onTransferClick: (Int) -> Unit,
    onMarkAsRead: () -> Unit,
    loadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
    selectedTransferId: Int? = null,
    highlightSelectedTransfer: Boolean = false,
    onRefresh: () -> Unit = {},
) {
    ReportDrawnWhen { !isSyncing && uiState is TransfersUiState.Success }
    val listState = rememberLazyListState()
    TrackScrollJank(scrollableState = listState, stateName = "transfers:feed")

    LaunchedEffect(uiState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index to listState.layoutInfo.totalItemsCount }
            .collectLatest { (last, total) -> if (last != null && last >= total - 3) loadNextPage() }
    }

    SyncRefreshBox(isSyncing = isSyncing, onRefresh = onRefresh, modifier = modifier.fillMaxSize()) {
        if (uiState is TransfersUiState.Success) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .testTag("transfers:feed"),
            ) {
                item(key = "leagues") { LeagueChips(uiState.leagues, uiState.topic, onTopicSelected, uiState.hasFollowed, uiState.onlyFollowed) }
                item(key = "seasons") { SeasonChips(uiState.seasons, uiState.season, onSeasonSelected) }
                if (uiState.hasUnread) {
                    item(key = "markAsRead") {
                        Box(Modifier.padding(horizontal = 8.dp)) {
                            NtTextButton(onClick = onMarkAsRead) {
                                Icon(NtIcons.MarkAsRead, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text(stringResource(R.string.feature_transfers_impl_mark_as_read))
                            }
                        }
                    }
                }
                if (uiState.transfers.isEmpty()) {
                    item(key = "empty") {
                        Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (uiState.onlyFollowed) {
                                    stringResource(R.string.feature_transfers_impl_empty_followed, uiState.season.label)
                                } else {
                                    stringResource(R.string.feature_transfers_impl_empty)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            if (uiState.onlyFollowed) {
                                NtTextButton(onClick = { onTopicSelected(null) }) { Text(stringResource(R.string.feature_transfers_impl_show_all)) }
                            }
                        }
                    }
                }
                items(uiState.transfers, key = { it.id }, contentType = { "transfer" }) { transfer ->
                    TransferListItem(
                        transfer = transfer,
                        onClick = { onTransferClick(transfer.id) },
                        selected = highlightSelectedTransfer && transfer.id == selectedTransferId,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }
        SyncIndicator(
            isLoading = uiState is TransfersUiState.Loading,
            isSyncing = isSyncing,
            hasContent = uiState is TransfersUiState.Success && uiState.transfers.isNotEmpty(),
            contentDesc = stringResource(R.string.feature_transfers_impl_loading),
        )
    }
    TrackScreenViewEvent(screenName = "Transfers")
}

@Composable
private fun LeagueChips(
    leagues: List<Topic>,
    selected: String?,
    onSelected: (String?) -> Unit,
    hasFollowed: Boolean = false,
    onlyFollowed: Boolean = false,
) {
    val listState = rememberLazyListState()
    // The "Obserwowane" chip comes first once something is followed; show it then
    LaunchedEffect(hasFollowed) {
        if (hasFollowed && selected == null && !onlyFollowed) listState.scrollToItem(0)
    }
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (hasFollowed) {
            item(key = "followed") { FollowedChip(onlyFollowed) { onSelected(if (onlyFollowed) null else TransfersViewModel.FOLLOWED_TOPIC) } }
        }
        item(key = "all") {
            FilterChip(
                selected = selected == null && !onlyFollowed,
                onClick = { onSelected(null) },
                label = { Text(stringResource(R.string.feature_transfers_impl_all_leagues)) },
            )
        }
        items(leagues, key = { it.slug }) { league ->
            FilterChip(
                selected = league.slug == selected,
                onClick = { onSelected(if (league.slug == selected) null else league.slug) },
                label = { Text(league.name) },
            )
        }
    }
}

@Composable
private fun SeasonChips(seasons: List<Season>, selected: Season, onSelected: (Season) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(seasons, key = { it.startYear }) { season ->
            FilterChip(
                selected = season == selected,
                onClick = { onSelected(season) },
                label = { Text(season.label) },
            )
        }
    }
}
