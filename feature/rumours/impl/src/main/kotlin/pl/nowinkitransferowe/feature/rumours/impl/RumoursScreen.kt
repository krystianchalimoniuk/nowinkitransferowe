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

package pl.nowinkitransferowe.feature.rumours.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors
import pl.nowinkitransferowe.core.designsystem.theme.cardContainer
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStats
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.ui.RumourListItem
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.stageName
import kotlin.math.roundToInt

@Composable
internal fun RumoursRoute(
    onRumourClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightSelectedRumour: Boolean = false,
    viewModel: RumoursViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedRumourId by viewModel.selectedRumourId.collectAsStateWithLifecycle()
    RumoursScreen(
        uiState = uiState,
        onTopicSelected = viewModel::selectTopic,
        onStageSelected = viewModel::selectStage,
        onRumourClick = { id ->
            viewModel.onRumourClick(id)
            onRumourClick(id)
        },
        modifier = modifier,
        selectedRumourId = selectedRumourId,
        highlightSelectedRumour = highlightSelectedRumour,
    )
}

/**
 * The rumours board: the numbers of the season, league chips, a stage filter and the active
 * rumours, most recently moved first.
 */
@Composable
internal fun RumoursScreen(
    uiState: RumoursUiState,
    onTopicSelected: (String?) -> Unit,
    onStageSelected: (RumourStage?) -> Unit,
    onRumourClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedRumourId: Int? = null,
    highlightSelectedRumour: Boolean = false,
) {
    val listState = rememberLazyListState()
    TrackScrollJank(scrollableState = listState, stateName = "rumours:feed")
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (uiState) {
            RumoursUiState.Loading -> NtLoadingWheel(
                contentDesc = stringResource(R.string.feature_rumours_impl_loading),
                modifier = Modifier.padding(top = 32.dp),
            )

            is RumoursUiState.Success -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .testTag("rumours:feed"),
            ) {
                uiState.stats?.let { stats -> item(key = "stats") { StatsRow(stats) } }
                item(key = "filters") {
                    Filters(uiState.leagues, uiState.topic, uiState.stage, onTopicSelected, onStageSelected)
                }
                if (uiState.rumours.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            stringResource(R.string.feature_rumours_impl_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                        )
                    }
                }
                items(uiState.rumours, key = { it.id }) { rumour ->
                    RumourListItem(
                        rumour = rumour,
                        onClick = { onRumourClick(rumour.id) },
                        selected = highlightSelectedRumour && rumour.id == selectedRumourId,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "Rumours")
}

/** Active and confirmed rumours of the season and, from 20 closed rumours, how many came true. */
@Composable
private fun StatsRow(stats: RumourStats) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.feature_rumours_impl_season, stats.season.label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(stringResource(R.string.feature_rumours_impl_active), stats.active.toString(), Modifier.weight(1f))
            StatTile(
                stringResource(R.string.feature_rumours_impl_confirmed),
                stats.confirmed.toString(),
                Modifier.weight(1f),
                valueColor = LocalNtColors.current.success,
            )
            stats.accuracy?.let { accuracy ->
                StatTile(stringResource(R.string.feature_rumours_impl_accuracy), "${(accuracy * 100).roundToInt()}%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.Unspecified) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.cardContainer,
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun Filters(
    leagues: List<Topic>,
    topic: String?,
    stage: RumourStage?,
    onTopicSelected: (String?) -> Unit,
    onStageSelected: (RumourStage?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "stage") { StageFilter(stage, onStageSelected) }
        item(key = "all") {
            FilterChip(
                selected = topic == null,
                onClick = { onTopicSelected(null) },
                label = { Text(stringResource(R.string.feature_rumours_impl_all_leagues)) },
            )
        }
        items(leagues, key = { it.slug }) { league ->
            FilterChip(
                selected = league.slug == topic,
                onClick = { onTopicSelected(if (league.slug == topic) null else league.slug) },
                label = { Text(league.name) },
            )
        }
    }
}

/** A chip that opens the stages; the picked stage is its label. */
@Composable
private fun StageFilter(stage: RumourStage?, onStageSelected: (RumourStage?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = stage != null,
            onClick = { expanded = true },
            label = { Text(stage?.let { stageName(it) } ?: stringResource(R.string.feature_rumours_impl_stage)) },
            trailingIcon = { Icon(NtIcons.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.testTag("rumours:stage"),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feature_rumours_impl_all_stages)) },
                onClick = {
                    expanded = false
                    onStageSelected(null)
                },
            )
            RumourStage.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text("${option.level}. ${stageName(option)}") },
                    onClick = {
                        expanded = false
                        onStageSelected(option)
                    },
                )
            }
        }
    }
}
