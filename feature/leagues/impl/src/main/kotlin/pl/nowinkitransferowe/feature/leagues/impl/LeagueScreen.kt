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

package pl.nowinkitransferowe.feature.leagues.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtCrest
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtSectionHeader
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.model.LeagueClub
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TransferListItem
import pl.nowinkitransferowe.core.ui.millionsLabel

@Composable
internal fun LeagueRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onClubClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    viewModel: LeagueViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LeagueScreen(uiState, showBackButton, onBackClick, onClubClick, onTransferClick, modifier)
}

/**
 * A league in a season as on the website: its totals, its clubs, the most expensive arrivals and
 * departures and how it compares with the other leagues.
 */
@Composable
internal fun LeagueScreen(
    uiState: LeagueUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onClubClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (uiState) {
            LeagueUiState.Loading -> NtLoadingWheel(
                contentDesc = stringResource(R.string.feature_leagues_impl_loading),
                modifier = Modifier.padding(top = 32.dp),
            )

            LeagueUiState.NotFound -> Column(Modifier.fillMaxWidth()) {
                TopRow(showBackButton, onBackClick)
                EmptyText(stringResource(R.string.feature_leagues_impl_unavailable))
            }

            is LeagueUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .testTag("league:${uiState.league.topic}:details"),
            ) {
                item(key = "top") { TopRow(showBackButton, onBackClick) }
                item(key = "title") {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(uiState.name.uppercase(), style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic))
                        Text(
                            stringResource(R.string.feature_leagues_impl_season, uiState.season.label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item(key = "totals") { Totals(uiState) }
                item(key = "clubs") {
                    NtSectionHeader(stringResource(R.string.feature_leagues_impl_clubs), Modifier.padding(horizontal = 16.dp).padding(top = 8.dp))
                }
                items(uiState.league.clubs, key = { "club:${it.clubId}" }) { club ->
                    ClubRow(club, uiState, onClick = { onClubClick(club.clubId) })
                }
                transfers("arrivals", R.string.feature_leagues_impl_top_arrivals, uiState.topArrivals, onTransferClick)
                transfers("departures", R.string.feature_leagues_impl_top_departures, uiState.topDepartures, onTransferClick)
                if (uiState.allLeagues.size > 1) {
                    item(key = "compare") {
                        LeaguesChart(uiState.allLeagues, uiState.names, stringResource(R.string.feature_leagues_impl_compare))
                    }
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "League")
}

@Composable
private fun TopRow(showBackButton: Boolean, onBackClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(4.dp).height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(NtIcons.ArrowBack, contentDescription = stringResource(R.string.feature_leagues_impl_back))
            }
        }
    }
}

/** Spent, earned, balance and transfers, two tiles per row so the amounts fit on a phone. */
@Composable
private fun Totals(state: LeagueUiState.Success) {
    val league = state.league
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(stringResource(R.string.feature_leagues_impl_spent), millionsLabel(league.spent), Modifier.weight(1f).fillMaxHeight())
            StatTile(stringResource(R.string.feature_leagues_impl_earned), millionsLabel(league.earned), Modifier.weight(1f).fillMaxHeight())
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                stringResource(R.string.feature_leagues_impl_balance),
                balanceLabel(league.balance),
                Modifier.weight(1f).fillMaxHeight(),
                valueColor = balanceColor(league.balance),
            )
            StatTile(stringResource(R.string.feature_leagues_impl_transfers_label), league.transfers.toString(), Modifier.weight(1f).fillMaxHeight())
        }
    }
}

/** A club of the league: its crest and name, spending and income, the balance and moves in and out. */
@Composable
private fun ClubRow(club: LeagueClub, state: LeagueUiState.Success, onClick: () -> Unit) {
    val known = state.clubs[club.clubId]
    val name = known?.name ?: stringResource(R.string.feature_leagues_impl_unknown_club)
    ChevronRow(onClick = onClick, modifier = Modifier.testTag("league-club:${club.clubId}")) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NtCrest(imageUrl = known?.crestUrl, name = name, size = 32.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(balanceLabel(club.balance), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = balanceColor(club.balance))
                }
                Text(
                    stringResource(R.string.feature_leagues_impl_spent_earned, millionsLabel(club.spent), millionsLabel(club.earned)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.feature_leagues_impl_moves, club.arrivals, club.departures),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun LazyListScope.transfers(key: String, title: Int, transfers: List<UserTransfer>, onTransferClick: (Int) -> Unit) {
    if (transfers.isEmpty()) return
    item(key = key) { NtSectionHeader(stringResource(title), Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)) }
    items(transfers, key = { "$key:${it.id}" }) { transfer ->
        TransferListItem(transfer, onClick = { onTransferClick(transfer.id) }, modifier = Modifier.padding(horizontal = 4.dp))
    }
}
