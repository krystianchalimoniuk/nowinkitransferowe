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

package pl.nowinkitransferowe.feature.topic.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.Clock
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.feature.articledetails.api.navigation.navigateToArticle
import pl.nowinkitransferowe.feature.club.api.navigation.navigateToClub
import pl.nowinkitransferowe.feature.leagues.api.navigation.navigateToLeague
import pl.nowinkitransferowe.feature.player.api.navigation.navigateToPlayer
import pl.nowinkitransferowe.feature.topic.api.navigation.TopicNavKey
import pl.nowinkitransferowe.feature.topic.impl.TopicRoute
import pl.nowinkitransferowe.feature.topic.impl.TopicViewModel

fun EntryProviderScope<NavKey>.topicEntry(navigator: Navigator) {
    entry<TopicNavKey> { key ->
        TopicRoute(
            onBackClick = { navigator.goBack() },
            onArticleClick = navigator::navigateToArticle,
            onLeagueClick = { slug -> navigator.navigateToLeague(slug, Season.of(Clock.System.now()).startYear) },
            onClubClick = navigator::navigateToClub,
            onPlayerClick = navigator::navigateToPlayer,
            viewModel = hiltViewModel<TopicViewModel, TopicViewModel.Factory>(key = "${key.topic}/${key.tag}") { factory ->
                factory.create(key.topic, key.tag)
            },
        )
    }
}
