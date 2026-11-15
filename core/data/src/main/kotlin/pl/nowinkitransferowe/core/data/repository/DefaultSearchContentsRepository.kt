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

package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.database.dao.SearchDao
import pl.nowinkitransferowe.core.model.SearchResult
import javax.inject.Inject

internal class DefaultSearchContentsRepository @Inject constructor(
    private val searchDao: SearchDao,
    private val articleRepository: ArticleRepository,
    private val transferRepository: TransferRepository,
    private val clubRepository: ClubRepository,
    private val playerRepository: PlayerRepository,
    private val rumourRepository: RumourRepository,
) : SearchContentsRepository {

    override fun searchContents(searchQuery: String): Flow<SearchResult> {
        val query = searchQuery.asFtsQuery()
        val articles = searchDao.searchArticleIds(query)
            .map { it.toSet() }
            .distinctUntilChanged()
            .flatMapLatest(articleRepository::observeArticles)
        val transfers = searchDao.searchTransferIds(query)
            .map { it.toSet() }
            .distinctUntilChanged()
            .flatMapLatest(transferRepository::observeTransfers)
        val clubs = searchDao.searchClubIds(query)
            .map { it.toSet() }
            .distinctUntilChanged()
            .flatMapLatest(clubRepository::observeClubs)
        val players = searchDao.searchPlayerIds(query)
            .map { it.toSet() }
            .distinctUntilChanged()
            .flatMapLatest(playerRepository::observePlayers)
        val rumours = searchDao.searchRumourIds(query)
            .map { it.toSet() }
            .distinctUntilChanged()
            .flatMapLatest(rumourRepository::observeRumours)
        return combine(articles, transfers, clubs, players, rumours) { articleList, transferList, clubList, playerList, rumourList ->
            SearchResult(articles = articleList, transfers = transferList, clubs = clubList, players = playerList, rumours = rumourList)
        }
    }

    override fun getSearchContentsCount(): Flow<Int> = combine(
        articleRepository.observeArticleCount(),
        transferRepository.observeTransferCount(),
    ) { articleCount, transferCount -> articleCount + transferCount }
}

/**
 * Every word of the query as a prefix, for example `lewandowski barc` → `lewandowski* barc*`. FTS
 * syntax characters are dropped, so whatever the user types is a valid query.
 */
internal fun String.asFtsQuery(): String = split(Regex("\\s+"))
    .map { word -> word.filter(Char::isLetterOrDigit) }
    .filter(String::isNotEmpty)
    .joinToString(" ") { "$it*" }
