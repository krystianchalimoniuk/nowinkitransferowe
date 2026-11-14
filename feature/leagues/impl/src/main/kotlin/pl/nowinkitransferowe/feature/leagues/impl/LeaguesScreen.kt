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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtConnectedButtonGroup
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtSectionHeader
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.millionsLabel

@Composable
internal fun LeaguesRoute(
    onLeagueClick: (topic: String, season: Season) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeaguesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LeaguesScreen(uiState, onSeasonSelected = viewModel::selectSeason, onLeagueClick = onLeagueClick, modifier = modifier)
}

/**
 * The leagues compared as on the website: money in and out of each league in the picked season and
 * the leagues as list items with their transfers and spending, opening their details.
 */
@Composable
internal fun LeaguesScreen(
    uiState: LeaguesUiState,
    onSeasonSelected: (Season) -> Unit,
    onLeagueClick: (topic: String, season: Season) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (uiState) {
            LeaguesUiState.Loading -> NtLoadingWheel(
                contentDesc = stringResource(R.string.feature_leagues_impl_loading),
                modifier = Modifier.padding(top = 32.dp),
            )

            LeaguesUiState.Unavailable -> EmptyText(stringResource(R.string.feature_leagues_impl_unavailable))

            is LeaguesUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .testTag("leagues:list"),
            ) {
                if (uiState.seasons.size >= 2) {
                    item(key = "seasons") {
                        val seasons = uiState.seasons.map { it.season }
                        NtConnectedButtonGroup(
                            options = seasons.map { it.label },
                            selectedIndex = seasons.indexOf(uiState.season),
                            onSelect = { onSeasonSelected(seasons[it]) },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
                if (uiState.leagues.isEmpty()) {
                    item(key = "empty") { EmptyText(stringResource(R.string.feature_leagues_impl_unavailable)) }
                } else {
                    item(key = "chart") {
                        LeaguesChart(uiState.leagues, uiState.names, stringResource(R.string.feature_leagues_impl_chart, uiState.season.label))
                    }
                    item(key = "numbers") {
                        NtSectionHeader(stringResource(R.string.feature_leagues_impl_numbers), Modifier.padding(horizontal = 16.dp).padding(top = 8.dp))
                    }
                    items(uiState.leagues, key = { it.topic }) { league ->
                        ChevronRow(onClick = { onLeagueClick(league.topic, uiState.season) }, modifier = Modifier.testTag("league:${league.topic}")) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    uiState.names[league.topic] ?: league.topic,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    balanceLabel(league.balance),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = balanceColor(league.balance),
                                )
                            }
                            Text(
                                stringResource(R.string.feature_leagues_impl_spent_earned, millionsLabel(league.spent), millionsLabel(league.earned)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                pluralStringResource(R.plurals.feature_leagues_impl_transfers, league.transfers, league.transfers),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "Leagues")
}
