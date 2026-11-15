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

package pl.nowinkitransferowe.feature.followed.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtCrest
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtPlayerPhoto
import pl.nowinkitransferowe.core.designsystem.component.NtSectionTitle
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.cardContainer
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.ui.FollowButton
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent

@Composable
internal fun FollowedRoute(
    onBackClick: () -> Unit,
    onClubClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FollowedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FollowedScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onClubClick = onClubClick,
        onPlayerClick = onPlayerClick,
        onClubFollowedChange = viewModel::setClubFollowed,
        onPlayerFollowedChange = viewModel::setPlayerFollowed,
        settingsActions = FollowedSettingsActions(
            onTransfersChange = viewModel::setTransfersNotified,
            onRumoursChange = viewModel::setRumoursNotified,
            onArticlesChange = viewModel::setArticlesNotified,
            onLeaguesChange = viewModel::setLeaguesNotified,
            onLeagueFollowedChange = viewModel::setLeagueFollowed,
            onQuietHoursChange = viewModel::setQuietHours,
        ),
        modifier = modifier,
    )
}

/** What the switches and league chips of the followed screen change. */
internal data class FollowedSettingsActions(
    val onTransfersChange: (Boolean) -> Unit = {},
    val onRumoursChange: (Boolean) -> Unit = {},
    val onArticlesChange: (Boolean) -> Unit = {},
    val onLeaguesChange: (Boolean) -> Unit = {},
    val onLeagueFollowedChange: (String, Boolean) -> Unit = { _, _ -> },
    val onQuietHoursChange: (Boolean) -> Unit = {},
)

/**
 * The followed clubs and players in groups like the settings, with what the app notifies about
 * them, the leagues whose important news it notifies about and the quiet hours. Tapping a club or
 * player opens it, its star stops following it.
 */
@Composable
internal fun FollowedScreen(
    uiState: FollowedUiState,
    onBackClick: () -> Unit,
    onClubClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit,
    onClubFollowedChange: (Int, Boolean) -> Unit,
    onPlayerFollowedChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    settingsActions: FollowedSettingsActions = FollowedSettingsActions(),
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .testTag("followed"),
        ) {
            item(key = "top") {
                Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(NtIcons.ArrowBack, contentDescription = stringResource(R.string.feature_followed_impl_back))
                    }
                    NtSectionTitle(stringResource(R.string.feature_followed_impl_title), Modifier.padding(start = 4.dp))
                }
            }
            when (uiState) {
                FollowedUiState.Loading -> item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        NtLoadingWheel(contentDesc = stringResource(R.string.feature_followed_impl_loading))
                    }
                }

                is FollowedUiState.Success -> {
                    notifyAbout(uiState.notifications, settingsActions)
                    if (uiState.isEmpty()) item(key = "empty") { Empty() }
                    group(
                        key = "clubs",
                        title = R.string.feature_followed_impl_clubs,
                        items = uiState.clubs,
                        itemKey = { "club:${it.club.id}" },
                    ) { followed, shape ->
                        FollowedItem(
                            name = followed.club.name,
                            summary = followed.league ?: followed.club.city,
                            shape = shape,
                            onClick = { onClubClick(followed.club.id) },
                            onUnfollow = { onClubFollowedChange(followed.club.id, false) },
                            modifier = Modifier.testTag("followed:club:${followed.club.id}"),
                        ) { NtCrest(imageUrl = followed.club.crestUrl, name = followed.club.name, size = 40.dp) }
                    }
                    group(
                        key = "players",
                        title = R.string.feature_followed_impl_players,
                        items = uiState.players,
                        itemKey = { "player:${it.id}" },
                    ) { player, shape ->
                        FollowedItem(
                            name = player.name,
                            summary = player.position,
                            shape = shape,
                            onClick = { onPlayerClick(player.id) },
                            onUnfollow = { onPlayerFollowedChange(player.id, false) },
                            modifier = Modifier.testTag("followed:player:${player.id}"),
                        ) { NtPlayerPhoto(photoUrl = player.photoUrl, name = player.name, size = 40.dp) }
                    }
                    leagues(uiState.leagues, uiState.notifications, settingsActions)
                    item(key = "quiet") {
                        GroupTitle(R.string.feature_followed_impl_quiet_hours_group)
                        SwitchRow(
                            icon = NtIcons.Bedtime,
                            title = stringResource(R.string.feature_followed_impl_quiet_hours),
                            summary = stringResource(R.string.feature_followed_impl_quiet_hours_summary),
                            checked = uiState.notifications.quietHours,
                            onCheckedChange = settingsActions.onQuietHoursChange,
                            shape = RoundedCornerShape(OUTER_CORNER),
                        )
                    }
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "Followed")
}

/** Which news of followed clubs and players notify. */
private fun LazyListScope.notifyAbout(settings: FollowedNotifications, actions: FollowedSettingsActions) {
    item(key = "notify") {
        GroupTitle(R.string.feature_followed_impl_notify_about)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SwitchRow(
                icon = NtIcons.Transfers,
                title = stringResource(R.string.feature_followed_impl_notify_transfers),
                summary = stringResource(R.string.feature_followed_impl_notify_transfers_summary),
                checked = settings.transfers,
                onCheckedChange = actions.onTransfersChange,
                shape = shapeAt(0, 3),
            )
            SwitchRow(
                icon = NtIcons.Rumours,
                title = stringResource(R.string.feature_followed_impl_notify_rumours),
                summary = stringResource(R.string.feature_followed_impl_notify_rumours_summary),
                checked = settings.rumours,
                onCheckedChange = actions.onRumoursChange,
                shape = shapeAt(1, 3),
            )
            SwitchRow(
                icon = NtIcons.Article,
                title = stringResource(R.string.feature_followed_impl_notify_articles),
                summary = stringResource(R.string.feature_followed_impl_notify_articles_summary),
                checked = settings.articles,
                onCheckedChange = actions.onArticlesChange,
                shape = shapeAt(2, 3),
            )
        }
    }
}

/** The leagues whose important news notify, picked with chips. */
@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.leagues(leagues: List<Topic>, settings: FollowedNotifications, actions: FollowedSettingsActions) {
    if (leagues.isEmpty()) return
    item(key = "leagues") {
        GroupTitle(R.string.feature_followed_impl_leagues)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SwitchRow(
                icon = NtIcons.Leagues,
                title = stringResource(R.string.feature_followed_impl_notify_leagues),
                summary = stringResource(R.string.feature_followed_impl_notify_leagues_summary),
                checked = settings.leagues,
                onCheckedChange = actions.onLeaguesChange,
                shape = shapeAt(0, 2),
            )
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(shapeAt(1, 2))
                    .background(MaterialTheme.colorScheme.cardContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                leagues.forEach { league ->
                    val selected = league.slug in settings.followedLeagues
                    FilterChip(
                        selected = selected,
                        onClick = { actions.onLeagueFollowedChange(league.slug, !selected) },
                        label = { Text(league.name) },
                        enabled = settings.leagues,
                        leadingIcon = if (selected) {
                            { Icon(NtIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else {
                            null
                        },
                        modifier = Modifier.testTag("followed:league:${league.slug}"),
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupTitle(title: Int) {
    Text(
        stringResource(title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape,
) {
    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.cardContainer)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(title),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** The shape of item [index] of [count] in a group: round outer corners, small inner ones. */
private fun shapeAt(index: Int, count: Int): RoundedCornerShape {
    val top = if (index == 0) OUTER_CORNER else INNER_CORNER
    val bottom = if (index == count - 1) OUTER_CORNER else INNER_CORNER
    return RoundedCornerShape(top, top, bottom, bottom)
}

/** A titled group whose items share the rounded corners of the settings groups. */
private fun <T> LazyListScope.group(
    key: String,
    title: Int,
    items: List<T>,
    itemKey: (T) -> Any,
    content: @Composable (T, RoundedCornerShape) -> Unit,
) {
    if (items.isEmpty()) return
    item(key = key) { GroupTitle(title) }
    itemsIndexed(items, key = { _, item -> itemKey(item) }) { index, item ->
        content(item, shapeAt(index, items.size))
        if (index != items.lastIndex) Spacer(Modifier.size(2.dp))
    }
}

@Composable
private fun FollowedItem(
    name: String,
    summary: String?,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    onUnfollow: () -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.cardContainer)
            .clickable(onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        leading()
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            summary?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        FollowButton(followed = true, onFollowedChange = { if (!it) onUnfollow() }, name = name)
    }
}

@Composable
private fun Empty() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(NtIcons.StarBorder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Text(stringResource(R.string.feature_followed_impl_empty), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            stringResource(R.string.feature_followed_impl_empty_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private val OUTER_CORNER = 20.dp
private val INNER_CORNER = 4.dp
