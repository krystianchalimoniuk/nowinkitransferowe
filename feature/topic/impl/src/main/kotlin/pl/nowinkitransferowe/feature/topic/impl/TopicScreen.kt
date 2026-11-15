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

package pl.nowinkitransferowe.feature.topic.impl

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtOutlinedButton
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.cardContainer
import pl.nowinkitransferowe.core.ui.ArticleListItem
import pl.nowinkitransferowe.core.ui.FollowButton
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent

@Composable
internal fun TopicRoute(
    onBackClick: () -> Unit,
    onArticleClick: (Int) -> Unit,
    onLeagueClick: (String) -> Unit,
    onClubClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit,
    viewModel: TopicViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TopicScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTabSelected = viewModel::selectTab,
        onFollowedChange = viewModel::setFollowed,
        onLoadMore = viewModel::loadMore,
        onArticleClick = onArticleClick,
        onArticleSavedChange = viewModel::setArticleSaved,
        onLeagueClick = onLeagueClick,
        onClubClick = onClubClick,
        onPlayerClick = onPlayerClick,
        modifier = modifier,
    )
}

/**
 * The articles of a topic or tag, like /temat and /tag on the website: its name with a star, a
 * link to the league, club or player it is about, and the articles in the tabs of the home screen.
 */
@Composable
internal fun TopicScreen(
    uiState: TopicUiState?,
    onBackClick: () -> Unit,
    onTabSelected: (TopicTab) -> Unit,
    onFollowedChange: (Boolean) -> Unit,
    onLoadMore: () -> Unit,
    onArticleClick: (Int) -> Unit,
    onArticleSavedChange: (Int, Boolean) -> Unit,
    onLeagueClick: (String) -> Unit,
    onClubClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.widthIn(max = 720.dp).fillMaxSize().testTag("topic"),
        ) {
            item(key = "top") {
                Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(NtIcons.ArrowBack, contentDescription = stringResource(R.string.feature_topic_impl_back))
                    }
                    Box(Modifier.weight(1f))
                    if (uiState != null && uiState.isFollowable) {
                        FollowButton(uiState.isFollowed, onFollowedChange, uiState.title, Modifier.testTag("topic:follow"))
                    }
                }
            }
            if (uiState == null) {
                item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        NtLoadingWheel(contentDesc = stringResource(R.string.feature_topic_impl_loading))
                    }
                }
                return@LazyColumn
            }
            item(key = "header") {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        uiState.title.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(uiState.subject.kindLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            when (val subject = uiState.subject) {
                is TopicSubject.League -> item(key = "link") {
                    LinkCard(NtIcons.Leagues, stringResource(R.string.feature_topic_impl_league_stats)) { onLeagueClick(subject.slug) }
                }
                is TopicSubject.OfClub -> item(key = "link") {
                    LinkCard(NtIcons.Shield, stringResource(R.string.feature_topic_impl_club_profile, subject.club.name)) { onClubClick(subject.club.id) }
                }
                is TopicSubject.OfPlayer -> item(key = "link") {
                    LinkCard(NtIcons.Person, stringResource(R.string.feature_topic_impl_player_profile, subject.player.name)) { onPlayerClick(subject.player.id) }
                }
                TopicSubject.OtherTopic, TopicSubject.Tag -> Unit
            }
            item(key = "tabs") {
                PrimaryTabRow(selectedTabIndex = uiState.tab.ordinal, modifier = Modifier.padding(top = 8.dp)) {
                    TopicTab.entries.forEach { tab ->
                        Tab(
                            selected = tab == uiState.tab,
                            onClick = { onTabSelected(tab) },
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
            val articles = uiState.articles.orEmpty()
            if (articles.isEmpty()) {
                item(key = "empty") {
                    Text(
                        stringResource(R.string.feature_topic_impl_no_articles),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                }
            }
            items(articles, key = { it.id }) { article ->
                ArticleListItem(
                    article = article,
                    onClick = { onArticleClick(article.id) },
                    onToggleBookmark = { onArticleSavedChange(article.id, !article.isSaved) },
                    modifier = Modifier.padding(horizontal = 12.dp).testTag("topic:article:${article.id}"),
                )
            }
            if (uiState.hasMore) {
                item(key = "more") {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        NtOutlinedButton(onClick = onLoadMore, modifier = Modifier.testTag("topic:more")) {
                            Text(stringResource(R.string.feature_topic_impl_more))
                        }
                    }
                }
            }
        }
    }
    TrackScreenViewEvent(screenName = "Topic")
}

/** A card that opens the league, club or player of the topic. */
@Composable
private fun LinkCard(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.cardContainer,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth().testTag("topic:link"),
    ) {
        Row(
            Modifier.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Icon(NtIcons.ChevronRight, contentDescription = null)
        }
    }
}

private val TopicSubject.kindLabel: Int
    get() = when (this) {
        is TopicSubject.League -> R.string.feature_topic_impl_kind_league
        TopicSubject.OtherTopic -> R.string.feature_topic_impl_kind_topic
        is TopicSubject.OfClub -> R.string.feature_topic_impl_kind_club
        is TopicSubject.OfPlayer -> R.string.feature_topic_impl_kind_player
        TopicSubject.Tag -> R.string.feature_topic_impl_kind_tag
    }

private val TopicTab.label: Int
    get() = when (this) {
        TopicTab.ALL -> R.string.feature_topic_impl_tab_all
        TopicTab.OFFICIAL -> R.string.feature_topic_impl_tab_official
        TopicTab.RUMOURS -> R.string.feature_topic_impl_tab_rumours
    }
