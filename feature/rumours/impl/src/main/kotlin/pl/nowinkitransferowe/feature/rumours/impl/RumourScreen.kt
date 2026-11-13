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

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Clock
import kotlinx.datetime.todayIn
import pl.nowinkitransferowe.core.designsystem.component.NtCrest
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtOutlinedButton
import pl.nowinkitransferowe.core.designsystem.component.NtSectionHeader
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.RumourStep
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.ui.BookmarkButton
import pl.nowinkitransferowe.core.ui.ClubCrest
import pl.nowinkitransferowe.core.ui.CountryFlag
import pl.nowinkitransferowe.core.ui.RumourStageIndicator
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.dateFormatted
import pl.nowinkitransferowe.core.ui.stageName

@Composable
internal fun RumourRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    onClubClick: (Int) -> Unit = {},
    viewModel: RumourViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RumourScreen(
        uiState = uiState,
        showBackButton = showBackButton,
        onBackClick = onBackClick,
        onBookmarkChanged = viewModel::bookmarkRumour,
        onArticleClick = onArticleClick,
        onTransferClick = onTransferClick,
        onClubClick = onClubClick,
        modifier = modifier,
    )
}

/**
 * The card of a rumour: the player and clubs, the stage with its status, the history of stages
 * with the articles that moved it and a button to the latest article.
 */
@Composable
internal fun RumourScreen(
    uiState: RumourUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onBookmarkChanged: (Boolean) -> Unit,
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    onClubClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (uiState) {
            RumourUiState.Loading -> NtLoadingWheel(
                contentDesc = stringResource(R.string.feature_rumours_impl_loading),
                modifier = Modifier.padding(top = 32.dp),
            )

            RumourUiState.NotFound -> Column(Modifier.fillMaxWidth()) {
                TopRow(showBackButton, onBackClick)
                Text(
                    stringResource(R.string.feature_rumours_impl_not_found),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                )
            }

            is RumourUiState.Success -> {
                val rumour = uiState.rumour
                val context = LocalContext.current
                Column(
                    Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .testTag("rumour:${rumour.id}"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TopRow(showBackButton, onBackClick) {
                        BookmarkButton(uiState.isSaved, onClick = { onBookmarkChanged(!uiState.isSaved) })
                        uiState.latestArticle?.let { article ->
                            IconButton(onClick = { share(context, rumour, article) }) {
                                Icon(NtIcons.Share, contentDescription = stringResource(R.string.feature_rumours_impl_share))
                            }
                        }
                    }
                    PlayerHeader(rumour, uiState.player)
                    Route(rumour.from, rumour.to, onClubClick)
                    StageCard(rumour)
                    rumour.transferId?.let { transferId ->
                        NtOutlinedButton(
                            onClick = { onTransferClick(transferId) },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                        ) { Text(stringResource(R.string.feature_rumours_impl_see_transfer)) }
                    }
                    if (rumour.history.isNotEmpty()) {
                        NtSectionHeader(stringResource(R.string.feature_rumours_impl_history), Modifier.padding(horizontal = 16.dp))
                        Column(Modifier.padding(horizontal = 8.dp)) {
                            rumour.history.sortedByDescending(RumourStep::at).forEach { step ->
                                HistoryStep(step, step.articleId?.let(uiState.articles::get), onArticleClick)
                            }
                        }
                    }
                    // Room for the button to the article
                    Spacer(Modifier.height(88.dp))
                }
                uiState.latestArticle?.let { article ->
                    ExtendedFloatingActionButton(
                        onClick = { onArticleClick(article.id) },
                        icon = { Icon(NtIcons.Article, contentDescription = null) },
                        text = { Text(stringResource(R.string.feature_rumours_impl_read)) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .testTag("rumour:read"),
                    )
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "Rumour")
}

@Composable
private fun TopRow(showBackButton: Boolean, onBackClick: () -> Unit, actions: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(NtIcons.ArrowBack, contentDescription = stringResource(R.string.feature_rumours_impl_back))
            }
        }
        Text(
            stringResource(R.string.feature_rumours_impl_rumour),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
        )
        actions()
    }
}

@Composable
private fun PlayerHeader(rumour: Rumour, player: Player?) {
    Row(
        Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        NtCrest(imageUrl = rumour.player.photoUrl, name = rumour.player.name, size = 64.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                rumour.player.name.uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic),
            )
            val age = player?.ageOn(Clock.System.todayIn(Season.TIME_ZONE))
            val details = listOfNotNull(player?.position, age?.let { pluralStringResource(R.plurals.feature_rumours_impl_age, it, it) })
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (player?.nationality ?: rumour.player.nationality)?.let { CountryFlag(it, contentDescription = null) }
                if (details.isNotEmpty()) {
                    Text(details.joinToString(" · "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun Route(from: ClubRef?, to: ClubRef, onClubClick: (Int) -> Unit) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RouteClub(from, Modifier.weight(1f), onClubClick)
            Icon(NtIcons.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            RouteClub(to, Modifier.weight(1f), onClubClick)
        }
    }
}

@Composable
private fun RouteClub(club: ClubRef?, modifier: Modifier, onClubClick: (Int) -> Unit) {
    Column(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .then(if (club is ClubRef.Club) Modifier.clickable { onClubClick(club.id) } else Modifier)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (club != null) {
            ClubCrest(club, size = 56.dp)
            Text(club.name, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        } else {
            Text(stringResource(R.string.feature_rumours_impl_unknown_club), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The stage on the rumour colour, with the status, the fee and when it last moved. */
@Composable
private fun StageCard(rumour: Rumour) {
    val colors = LocalNtColors.current
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.rumourContainer),
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            RumourStageIndicator(rumour.stage, rumour.trend)
            Text(
                listOfNotNull(
                    statusName(rumour.status),
                    rumour.fee,
                    stringResource(R.string.feature_rumours_impl_updated, dateFormatted(rumour.updatedAt)),
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistoryStep(step: RumourStep, article: Article?, onArticleClick: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (article != null) Modifier.clickable { onArticleClick(article.id) } else Modifier)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(shape = CircleShape, color = LocalNtColors.current.rumourContainer, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(stageIcon(step.stage), contentDescription = null, tint = LocalNtColors.current.rumour, modifier = Modifier.size(20.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                "${stageName(step.stage)} · ${dateFormatted(step.at)}",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                article?.title ?: stringResource(R.string.feature_rumours_impl_no_article),
                style = MaterialTheme.typography.bodyMedium,
                color = if (article != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun stageIcon(stage: RumourStage): ImageVector = when (stage) {
    RumourStage.SPECULATION -> NtIcons.Rumours
    RumourStage.INTEREST -> NtIcons.Visibility
    RumourStage.TALKS -> NtIcons.Forum
    RumourStage.CLOSE -> NtIcons.TrendingUp
    RumourStage.AGREEMENT -> NtIcons.Handshake
}

@Composable
private fun statusName(status: RumourStatus): String? = when (status) {
    RumourStatus.ACTIVE -> stringResource(R.string.feature_rumours_impl_status_active)
    RumourStatus.CONFIRMED -> stringResource(R.string.feature_rumours_impl_status_confirmed)
    RumourStatus.FAILED -> stringResource(R.string.feature_rumours_impl_status_failed)
    RumourStatus.EXPIRED -> stringResource(R.string.feature_rumours_impl_status_expired)
    RumourStatus.UNKNOWN -> null
}

private fun share(context: Context, rumour: Rumour, article: Article) {
    val send = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, "${rumour.player.name} → ${rumour.to.name}\n${article.url}")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(send, null))
}
