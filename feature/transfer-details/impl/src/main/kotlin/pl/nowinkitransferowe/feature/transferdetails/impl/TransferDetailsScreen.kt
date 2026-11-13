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

package pl.nowinkitransferowe.feature.transferdetails.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtOutlinedButton
import pl.nowinkitransferowe.core.designsystem.component.NtPlayerPhoto
import pl.nowinkitransferowe.core.designsystem.component.NtSectionHeader
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.cardContainer
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.ui.BookmarkButton
import pl.nowinkitransferowe.core.ui.ClubCrest
import pl.nowinkitransferowe.core.ui.CountryFlag
import pl.nowinkitransferowe.core.ui.FeeLabel
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TransferListItem
import pl.nowinkitransferowe.core.ui.dateFormatted

@Composable
internal fun TransferDetailsRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    onClubClick: (Int) -> Unit = {},
    onPlayerClick: (Int) -> Unit = {},
    viewModel: TransferDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrackScreenViewEvent(screenName = "Transfer: ${viewModel.transferId}")
    TransferDetailsScreen(
        uiState = uiState,
        showBackButton = showBackButton,
        onBackClick = onBackClick,
        onBookmarkChanged = viewModel::bookmarkTransfer,
        onArticleClick = onArticleClick,
        onTransferClick = onTransferClick,
        onClubClick = onClubClick,
        onPlayerClick = onPlayerClick,
        modifier = modifier,
    )
}

/**
 * A transfer as in the mockups: the player, the route with large crests, the fee, date and
 * season, links to the article and the player profile, and the player's other transfers.
 */
@Composable
internal fun TransferDetailsScreen(
    uiState: TransferDetailsUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onBookmarkChanged: (Int, Boolean) -> Unit,
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    onClubClick: (Int) -> Unit = {},
    onPlayerClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        TransferDetailsUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NtLoadingWheel(contentDesc = stringResource(R.string.feature_transfer_details_impl_loading))
        }

        is TransferDetailsUiState.Success -> {
            val transfer = uiState.transfer
            Column(
                modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .testTag("transfer:${transfer.id}"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(Modifier.widthIn(max = 720.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (showBackButton) {
                            IconButton(onClick = onBackClick) {
                                Icon(NtIcons.ArrowBack, contentDescription = stringResource(pl.nowinkitransferowe.core.ui.R.string.core_ui_back))
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        BookmarkButton(transfer.isSaved, onClick = { onBookmarkChanged(transfer.id, !transfer.isSaved) })
                    }
                    PlayerHeader(transfer, uiState.player)
                    RouteCard(transfer.from, transfer.to, transfer, onClubClick)
                    Facts(transfer)
                    Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        transfer.articleId?.let { articleId ->
                            FilledTonalButton(onClick = { onArticleClick(articleId) }, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.feature_transfer_details_impl_article))
                            }
                        }
                        NtOutlinedButton(
                            onClick = { onPlayerClick(transfer.player.id) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.feature_transfer_details_impl_player_profile))
                        }
                    }
                    if (uiState.history.size > 1) {
                        NtSectionHeader(stringResource(R.string.feature_transfer_details_impl_history), Modifier.padding(horizontal = 16.dp))
                        Column(Modifier.padding(horizontal = 4.dp)) {
                            uiState.history.forEach { other ->
                                TransferListItem(
                                    transfer = other,
                                    onClick = { if (other.id != transfer.id) onTransferClick(other.id) },
                                    selected = other.id == transfer.id,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
                }
            }
        }
    }
}

@Composable
private fun PlayerHeader(transfer: UserTransfer, player: Player?) {
    Row(
        Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        NtPlayerPhoto(photoUrl = transfer.player.photoUrl, name = transfer.player.name, size = 64.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                transfer.player.name.uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic),
            )
            val details = listOfNotNull(player?.position, player?.birthDate?.let { stringResource(R.string.feature_transfer_details_impl_born, dateFormatted(it)) })
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (player?.nationality ?: transfer.player.nationality)?.let { CountryFlag(it, contentDescription = null) }
                if (details.isNotEmpty()) {
                    Text(details.joinToString(" · "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RouteCard(from: ClubRef, to: ClubRef, transfer: UserTransfer, onClubClick: (Int) -> Unit) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.cardContainer),
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RouteClub(from, Modifier.weight(1f), onClubClick)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(NtIcons.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                FeeLabel(transfer.fee)
            }
            RouteClub(to, Modifier.weight(1f), onClubClick)
        }
    }
}

@Composable
private fun RouteClub(club: ClubRef, modifier: Modifier, onClubClick: (Int) -> Unit) {
    Column(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .then(if (club is ClubRef.Club) Modifier.clickable { onClubClick(club.id) } else Modifier)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ClubCrest(club, size = 56.dp)
        Text(
            club.name,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Facts(transfer: UserTransfer) {
    Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Fact(stringResource(R.string.feature_transfer_details_impl_fee), transfer.fee.label, Modifier.weight(1f))
        Fact(stringResource(R.string.feature_transfer_details_impl_list_label_date), dateFormatted(transfer.date), Modifier.weight(1f))
        Fact(stringResource(R.string.feature_transfer_details_impl_list_label_season), transfer.season.label, Modifier.weight(1f))
    }
}

@Composable
private fun Fact(label: String, value: String, modifier: Modifier) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.cardContainer),
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
