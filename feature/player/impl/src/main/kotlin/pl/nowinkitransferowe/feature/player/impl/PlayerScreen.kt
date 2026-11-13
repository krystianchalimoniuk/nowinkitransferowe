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

package pl.nowinkitransferowe.feature.player.impl

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtPlayerPhoto
import pl.nowinkitransferowe.core.designsystem.component.NtSectionHeader
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.cardContainer
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.ui.ArticleListItem
import pl.nowinkitransferowe.core.ui.CountryFlag
import pl.nowinkitransferowe.core.ui.FeeLabel
import pl.nowinkitransferowe.core.ui.RumourListItem
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TransferRoute
import pl.nowinkitransferowe.core.ui.millionsLabel
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun PlayerRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTransferClick: (Int) -> Unit,
    onClubClick: (Int) -> Unit,
    onRumourClick: (Int) -> Unit,
    onArticleClick: (Int) -> Unit,
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlayerScreen(
        uiState = uiState,
        showBackButton = showBackButton,
        onBackClick = onBackClick,
        onTransferClick = onTransferClick,
        onClubClick = onClubClick,
        onRumourClick = onRumourClick,
        onArticleClick = onArticleClick,
        onArticleSavedChanged = viewModel::updateArticleSaved,
        modifier = modifier,
    )
}

/**
 * A player as on the website: facts from Wikidata, the fees of his transfers over time, his
 * transfer history and the active rumours about him.
 */
@Composable
internal fun PlayerScreen(
    uiState: PlayerUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTransferClick: (Int) -> Unit,
    onClubClick: (Int) -> Unit,
    onRumourClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onArticleClick: (Int) -> Unit = {},
    onArticleSavedChanged: (Int, Boolean) -> Unit = { _, _ -> },
    today: LocalDate = Clock.System.todayIn(Season.TIME_ZONE),
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (uiState) {
            PlayerUiState.Loading -> NtLoadingWheel(
                contentDesc = stringResource(R.string.feature_player_impl_loading),
                modifier = Modifier.padding(top = 32.dp),
            )

            PlayerUiState.NotFound -> Column(Modifier.fillMaxWidth()) {
                TopRow(showBackButton, onBackClick, player = null)
                Text(
                    stringResource(R.string.feature_player_impl_not_found),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                )
            }

            is PlayerUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .testTag("player:${uiState.player.id}"),
            ) {
                item(key = "top") { TopRow(showBackButton, onBackClick, uiState.player) }
                item(key = "header") { Header(uiState) }
                item(key = "facts") { Facts(uiState, today, onClubClick) }
                if (uiState.transfers.isNotEmpty()) {
                    item(key = "summary") { Summary(uiState) }
                }
                if (uiState.showChart) {
                    item(key = "fees") { FeeCard(uiState.chartTransfers) }
                }
                if (uiState.transfers.isNotEmpty()) {
                    item(key = "history") {
                        NtSectionHeader(stringResource(R.string.feature_player_impl_history), Modifier.padding(horizontal = 16.dp).padding(top = 8.dp))
                    }
                    items(uiState.transfers, key = { "transfer:${it.id}" }) { transfer ->
                        HistoryRow(transfer, onClick = { onTransferClick(transfer.id) })
                    }
                }
                if (uiState.rumours.isNotEmpty()) {
                    item(key = "rumours") {
                        NtSectionHeader(stringResource(R.string.feature_player_impl_rumours), Modifier.padding(horizontal = 16.dp).padding(top = 8.dp))
                    }
                    items(uiState.rumours, key = { "rumour:${it.id}" }) { rumour ->
                        RumourListItem(rumour, onClick = { onRumourClick(rumour.id) }, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }
                if (uiState.articles.isNotEmpty()) {
                    item(key = "articles") {
                        NtSectionHeader(stringResource(R.string.feature_player_impl_articles), Modifier.padding(horizontal = 16.dp).padding(top = 8.dp))
                    }
                    items(uiState.articles, key = { "article:${it.id}" }) { article ->
                        ArticleListItem(
                            article = article,
                            onClick = { onArticleClick(article.id) },
                            onToggleBookmark = { onArticleSavedChanged(article.id, !article.isSaved) },
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "Player")
}

@Composable
private fun TopRow(showBackButton: Boolean, onBackClick: () -> Unit, player: Player?) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(NtIcons.ArrowBack, contentDescription = stringResource(R.string.feature_player_impl_back))
            }
        }
        Box(Modifier.weight(1f))
        player?.url?.let { url ->
            IconButton(onClick = { share(context, player.name, url) }) {
                Icon(NtIcons.Share, contentDescription = stringResource(R.string.feature_player_impl_share))
            }
        }
    }
}

@Composable
private fun Header(state: PlayerUiState.Success) {
    val player = state.player
    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        NtPlayerPhoto(photoUrl = player.photoUrl, name = player.name, size = 64.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(player.name.uppercase(), style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic))
            val details = state.countries.map { it.name } + listOfNotNull(player.position)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.countries.forEach { CountryFlag(it.code, contentDescription = null) }
                if (details.isNotEmpty()) {
                    Text(details.joinToString(" · "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** Facts as list items with icons: birth, height and the current club, which opens its screen. */
@Composable
private fun Facts(state: PlayerUiState.Success, today: LocalDate, onClubClick: (Int) -> Unit) {
    val player = state.player
    Column(Modifier.padding(horizontal = 8.dp)) {
        player.birthDate?.let { born ->
            val age = player.ageOn(today)
            val text = buildString {
                append(stringResource(R.string.feature_player_impl_born, born.toJavaLocalDate().format(BIRTH_DATE)))
                player.birthPlace?.let { append(", ").append(it) }
                age?.let { append(" (").append(pluralStringResource(R.plurals.feature_player_impl_age, it, it)).append(")") }
            }
            FactItem(NtIcons.Cake, text)
        }
        player.heightCm?.let { FactItem(NtIcons.Height, stringResource(R.string.feature_player_impl_height, it)) }
        (state.currentClub as? ClubRef.Club)?.let { club ->
            val since = state.transfers.first().date.toJavaLocalDate().format(SHORT_MONTH_YEAR)
            FactItem(
                icon = NtIcons.Shield,
                text = stringResource(R.string.feature_player_impl_current_club_since, club.name, since),
                label = stringResource(R.string.feature_player_impl_current_club),
                onClick = { onClubClick(club.id) },
                modifier = Modifier.testTag("player:current-club"),
            )
        }
    }
}

@Composable
private fun FactItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f)) {
            label?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
        if (onClick != null) Icon(NtIcons.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * How many transfers, their fees together and the highest one, like on the website. The tiles
 * share one height; the highest fee is left out when it is the only fee, as it equals the total.
 */
@Composable
private fun Summary(state: PlayerUiState.Success) {
    val paid = state.transfers.count { it.fee.countedMillions > 0 }
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(IntrinsicSize.Min)
            .testTag("player:summary"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryTile(stringResource(R.string.feature_player_impl_transfers), state.transfers.size.toString(), Modifier.weight(1f))
        SummaryTile(stringResource(R.string.feature_player_impl_total), millionsLabel(state.totalMillions), Modifier.weight(1f))
        if (paid > 1) {
            SummaryTile(stringResource(R.string.feature_player_impl_highest), millionsLabel(state.highestMillions), Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.cardContainer, modifier = modifier.fillMaxHeight()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = MaterialTheme.typography.titleMedium.fontSize),
            )
        }
    }
}

/** The fees of the player's transfers over time, in a card like on the website. */
@Composable
private fun FeeCard(transfers: List<UserTransfer>) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.cardContainer),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .testTag("player:fees"),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.feature_player_impl_fees), style = MaterialTheme.typography.titleMedium)
            FeeChart(transfers)
            Text(
                stringResource(R.string.feature_player_impl_fees_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A transfer in the history: the season in big type, the month, the clubs and the fee. */
@Composable
private fun HistoryRow(transfer: UserTransfer, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("player-transfer:${transfer.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            (transfer.date.year % 100).toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(40.dp),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                transfer.date.toJavaLocalDate().format(MONTH_YEAR),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TransferRoute(transfer.from, transfer.to)
        }
        FeeLabel(transfer.fee)
    }
}

private fun share(context: Context, name: String, url: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, "$name\n$url")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(send, null))
}

private val POLISH = Locale.forLanguageTag("pl")
private val BIRTH_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", POLISH)
private val MONTH_YEAR = DateTimeFormatter.ofPattern("LLLL yyyy", POLISH)
private val SHORT_MONTH_YEAR = DateTimeFormatter.ofPattern("LLL yyyy", POLISH)
