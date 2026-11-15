/*
 * Copyright 2024 The Android Open Source Project
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

package pl.nowinkitransferowe.data.test.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.SearchResult
import javax.inject.Inject

/**
 * Searches the demo data by name and title, ignoring case, instead of the full-text indexes, so
 * the demo app shows every section.
 */
internal class FakeSearchContentsRepository @Inject constructor(
    private val articles: FakeArticleRepository,
    private val transfers: FakeTransferRepository,
    private val clubs: FakeClubRepository,
    private val players: FakePlayerRepository,
    private val rumours: FakeRumourRepository,
) : SearchContentsRepository {

    override fun searchContents(searchQuery: String): Flow<SearchResult> {
        val words = searchQuery.lowercase().split(Regex("\\s+")).filter(String::isNotBlank)
        fun matches(vararg texts: String?) = words.isNotEmpty() && words.all { word -> texts.any { it?.lowercase()?.contains(word) == true } }
        return combine(
            articles.observeArticles(kinds = null, topic = null, limit = Int.MAX_VALUE),
            transfers.observeTransfers(),
            clubs.clubs,
            players.players,
            rumours.observeRumours(RumourStatus.ACTIVE),
        ) { articleList, transferList, clubList, playerList, rumourList ->
            SearchResult(
                articles = articleList.filter { matches(it.title, it.lead) },
                transfers = transferList.filter { matches(it.player.name, it.from.name, it.to.name) },
                clubs = clubList.filter { it.slug != null && matches(it.name) },
                players = playerList.filter { it.slug != null && matches(it.name) },
                rumours = rumourList.filter { matches(it.player.name, it.from?.name, it.to.name) },
            )
        }
    }

    override fun getSearchContentsCount(): Flow<Int> = flowOf(1)
}
