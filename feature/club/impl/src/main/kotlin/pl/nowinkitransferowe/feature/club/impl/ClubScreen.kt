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

package pl.nowinkitransferowe.feature.club.impl

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtCrest
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtSectionHeader
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.ClubSeason
import pl.nowinkitransferowe.core.ui.CountryFlag
import pl.nowinkitransferowe.core.ui.RumourListItem
import pl.nowinkitransferowe.core.ui.SpendingBars
import pl.nowinkitransferowe.core.ui.SpendingRow
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TransferListItem
import pl.nowinkitransferowe.core.ui.millionsLabel

@Composable
internal fun ClubRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTransferClick: (Int) -> Unit,
    onRumourClick: (Int) -> Unit,
    viewModel: ClubViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ClubScreen(
        uiState = uiState,
        showBackButton = showBackButton,
        onBackClick = onBackClick,
        onTabSelected = viewModel::selectTab,
        onTransferClick = onTransferClick,
        onRumourClick = onRumourClick,
        modifier = modifier,
    )
}

/**
 * A club as on the website: its facts from Wikidata, then tabs with its spending by season, the
 * players who came and left and the rumours about it.
 */
@Composable
internal fun ClubScreen(
    uiState: ClubUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTabSelected: (ClubTab) -> Unit,
    onTransferClick: (Int) -> Unit,
    onRumourClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (uiState) {
            ClubUiState.Loading -> NtLoadingWheel(
                contentDesc = stringResource(R.string.feature_club_impl_loading),
                modifier = Modifier.padding(top = 32.dp),
            )

            ClubUiState.NotFound -> Column(Modifier.fillMaxWidth()) {
                TopRow(showBackButton, onBackClick, club = null)
                Text(
                    stringResource(R.string.feature_club_impl_not_found),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                )
            }

            is ClubUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .testTag("club:${uiState.club.id}"),
            ) {
                item(key = "top") { TopRow(showBackButton, onBackClick, uiState.club) }
                item(key = "header") { Header(uiState) }
                item(key = "tabs") { Tabs(uiState, onTabSelected) }
                when (uiState.tab) {
                    ClubTab.FINANCES -> finances(uiState.seasons)
                    ClubTab.ARRIVALS -> transfers(uiState.arrivals, R.string.feature_club_impl_no_arrivals, onTransferClick)
                    ClubTab.DEPARTURES -> transfers(uiState.departures, R.string.feature_club_impl_no_departures, onTransferClick)
                    ClubTab.RUMOURS -> {
                        if (uiState.rumours.isEmpty()) item(key = "empty") { Empty(R.string.feature_club_impl_no_rumours) }
                        items(uiState.rumours, key = { "rumour:${it.id}" }) { rumour ->
                            RumourListItem(rumour, onClick = { onRumourClick(rumour.id) }, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "Club")
}

@Composable
private fun TopRow(showBackButton: Boolean, onBackClick: () -> Unit, club: Club?) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(NtIcons.ArrowBack, contentDescription = stringResource(R.string.feature_club_impl_back))
            }
        }
        Box(Modifier.weight(1f))
        club?.url?.let { url ->
            IconButton(onClick = { share(context, club.name, url) }) {
                Icon(NtIcons.Share, contentDescription = stringResource(R.string.feature_club_impl_share))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header(state: ClubUiState.Success) {
    val club = state.club
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            NtCrest(imageUrl = club.crestUrl, name = club.name, size = 64.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(club.name.uppercase(), style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic))
                Text(
                    pluralStringResource(R.plurals.feature_club_impl_transfer_count, state.transferCount, state.transferCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.country?.let { country ->
                Fact(text = country.name) { CountryFlag(country.code, contentDescription = null) }
            }
            state.league?.let { Fact(it.name, NtIcons.EmojiEvents) }
            club.stadium?.let { Fact(it, NtIcons.Stadium) }
            club.founded?.let { Fact(it.toString(), NtIcons.History) }
        }
    }
}

@Composable
private fun Fact(text: String, icon: ImageVector) {
    Fact(text) { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
}

/** A fact about the club with an icon, like the chips on the website. */
@Composable
private fun Fact(text: String, leading: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            leading()
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Tabs(state: ClubUiState.Success, onSelected: (ClubTab) -> Unit) {
    PrimaryScrollableTabRow(selectedTabIndex = state.tab.ordinal, edgePadding = 16.dp, modifier = Modifier.padding(top = 12.dp)) {
        ClubTab.entries.forEach { tab ->
            Tab(
                selected = tab == state.tab,
                onClick = { onSelected(tab) },
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = {
                    Text(
                        stringResource(
                            when (tab) {
                                ClubTab.FINANCES -> R.string.feature_club_impl_tab_finances
                                ClubTab.ARRIVALS -> R.string.feature_club_impl_tab_arrivals
                                ClubTab.DEPARTURES -> R.string.feature_club_impl_tab_departures
                                ClubTab.RUMOURS -> R.string.feature_club_impl_tab_rumours
                            },
                        ),
                    )
                },
            )
        }
    }
}

private fun LazyListScope.finances(seasons: List<ClubSeason>) {
    val latest = seasons.firstOrNull()
    if (latest == null) {
        item(key = "empty") { Empty(R.string.feature_club_impl_no_finances) }
        return
    }
    item(key = "tiles") {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val colors = LocalNtColors.current
            Tile(stringResource(R.string.feature_club_impl_spent, latest.season.label), millionsLabel(latest.spent), Modifier.weight(1f))
            Tile(stringResource(R.string.feature_club_impl_earned), millionsLabel(latest.earned), Modifier.weight(1f))
            Tile(
                stringResource(R.string.feature_club_impl_balance),
                (if (latest.balance > 0) "+" else "") + millionsLabel(latest.balance),
                Modifier.weight(1f),
                valueColor = if (latest.balance >= 0) colors.success else MaterialTheme.colorScheme.primary,
            )
        }
    }
    item(key = "chart") {
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NtSectionHeader(stringResource(R.string.feature_club_impl_spending_by_season))
            SpendingBars(seasons.map { SpendingRow(it.season.label, moneyIn = it.earned, moneyOut = it.spent) })
        }
    }
}

@Composable
private fun Tile(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.Unspecified) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1)
        }
    }
}

private fun LazyListScope.transfers(
    transfers: List<pl.nowinkitransferowe.core.model.UserTransfer>,
    emptyText: Int,
    onTransferClick: (Int) -> Unit,
) {
    if (transfers.isEmpty()) item(key = "empty") { Empty(emptyText) }
    items(transfers, key = { "transfer:${it.id}" }) { transfer ->
        TransferListItem(transfer, onClick = { onTransferClick(transfer.id) }, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun Empty(text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    )
}

private fun share(context: Context, name: String, url: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, "$name\n$url")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(send, null))
}
