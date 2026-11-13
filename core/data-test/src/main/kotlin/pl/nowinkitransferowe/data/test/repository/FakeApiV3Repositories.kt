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

package pl.nowinkitransferowe.data.test.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.model.clubEntities
import pl.nowinkitransferowe.core.data.model.playerEntities
import pl.nowinkitransferowe.core.data.model.tagEntities
import pl.nowinkitransferowe.core.data.model.topicEntities
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.database.model.PopulatedArticle
import pl.nowinkitransferowe.core.database.model.PopulatedTransfer
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.demo.DemoNtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkClub
import pl.nowinkitransferowe.core.network.model.NetworkPlayer
import javax.inject.Inject

// Fakes of the API v3 repositories for the app's Hilt tests. Articles, transfers and the
// dictionaries come from the demo data, mapped as the database would return them; the other
// records are empty until their screens arrive.

class FakeDictionaryRepository @Inject constructor(
    private val datasource: DemoNtNetworkDataSource,
) : DictionaryRepository {
    override fun observeTopics(): Flow<List<Topic>> = flow {
        emit(datasource.getTopics().map { it.asEntity().asExternalModel() })
    }

    override fun observeCountries(): Flow<List<Country>> = flow {
        emit(datasource.getCountries().map { it.asEntity().asExternalModel() })
    }

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakeArticleRepository @Inject constructor(
    private val datasource: DemoNtNetworkDataSource,
) : ArticleRepository {
    private val articles: Flow<List<Article>> = flow {
        val articles = datasource.getArticles(allIds(datasource, NetworkRecordType.ARTICLES))
        emit(
            articles
                .map { PopulatedArticle(it.asEntity(), it.topicEntities(), it.clubEntities(), it.playerEntities(), it.tagEntities()).asExternalModel() }
                .sortedByDescending(Article::publishedAt),
        )
    }

    override fun observeArticle(id: Int): Flow<Article?> = articles.map { list -> list.find { it.id == id } }

    override fun observeArticles(kinds: Set<ArticleKind>?, topic: String?, limit: Int): Flow<List<Article>> = articles.map { list ->
        list.filter { kinds == null || it.kind in kinds }.filter { topic == null || topic in it.topics }.take(limit)
    }

    override fun observeArticles(ids: Set<Int>): Flow<List<Article>> = articles.map { list -> list.filter { it.id in ids } }

    override fun observeArticleCount(): Flow<Int> = articles.map { it.size }

    override fun observeArticlesAboutClub(clubId: Int): Flow<List<Article>> = articles.map { list -> list.filter { clubId in it.clubIds } }

    override fun observeArticlesAboutPlayer(playerId: Int): Flow<List<Article>> = articles.map { list -> list.filter { playerId in it.playerIds } }

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakeTransferRepository @Inject constructor(
    private val datasource: DemoNtNetworkDataSource,
) : TransferRepository {
    private val transfers: Flow<List<Transfer>> = flow {
        val clubs = datasource.getClubs(allIds(datasource, NetworkRecordType.CLUBS)).associateBy(NetworkClub::id)
        val players = datasource.getPlayers(allIds(datasource, NetworkRecordType.PLAYERS)).associateBy(NetworkPlayer::id)
        val transfers = datasource.getTransfers(allIds(datasource, NetworkRecordType.TRANSFERS))
        val articleIds = allIds(datasource, NetworkRecordType.ARTICLES).toSet()
        emit(
            transfers
                .map { transfer ->
                    val from = transfer.fromClubId?.let(clubs::get)
                    val to = transfer.toClubId?.let(clubs::get)
                    PopulatedTransfer(
                        transfer = transfer.asEntity(articleIds),
                        currentPlayerName = players[transfer.playerId]?.name,
                        playerPhotoUrl = players[transfer.playerId]?.photoUrl,
                        currentFromClubName = from?.name,
                        fromClubCrestUrl = from?.crestUrl,
                        currentToClubName = to?.name,
                        toClubCrestUrl = to?.crestUrl,
                    ).asExternalModel()
                }
                .sortedWith(compareByDescending<Transfer> { it.date }.thenByDescending { it.id }),
        )
    }

    override fun observeTransfer(id: Int): Flow<Transfer?> = transfers.map { list -> list.find { it.id == id } }

    override fun observeTransfers(season: Season?, limit: Int): Flow<List<Transfer>> = transfers.map { list ->
        list.filter { season == null || it.season == season }.take(limit)
    }

    override fun observeTransfers(ids: Set<Int>): Flow<List<Transfer>> = transfers.map { list -> list.filter { it.id in ids } }

    override fun observeTransferCount(): Flow<Int> = transfers.map { it.size }

    override fun observeTransfersOfClub(clubId: Int): Flow<List<Transfer>> = transfers.map { list ->
        list.filter { transfer -> listOf(transfer.from, transfer.to).any { it is ClubRef.Club && it.id == clubId } }
    }

    override fun observeTransfersOfPlayer(playerId: Int): Flow<List<Transfer>> = transfers.map { list -> list.filter { it.player.id == playerId } }

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

/** Every record id of [type] in the demo data. */
private suspend fun allIds(datasource: DemoNtNetworkDataSource, type: NetworkRecordType): List<Int> = datasource.getChanges(type, after = 0, limit = Int.MAX_VALUE).changes
    .filterNot { it.deleted }
    .map { it.id }

class FakeRumourRepository @Inject constructor() : RumourRepository {
    override fun observeRumour(id: Int): Flow<Rumour?> = flowOf(null)

    override fun observeRumours(status: RumourStatus, updatedSince: Instant): Flow<List<Rumour>> = flowOf(emptyList())

    override fun observeRumoursOfArticle(articleId: Int): Flow<List<Rumour>> = flowOf(emptyList())

    override fun observeRumoursOfClub(clubId: Int): Flow<List<Rumour>> = flowOf(emptyList())

    override fun observeRumoursOfPlayer(playerId: Int): Flow<List<Rumour>> = flowOf(emptyList())

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakeClubRepository @Inject constructor() : ClubRepository {
    override fun observeClub(id: Int): Flow<Club?> = flowOf(null)

    override fun observeClubs(ids: Set<Int>): Flow<List<Club>> = flowOf(emptyList())

    override suspend fun getClubBySlug(slug: String): Club? = null

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakePlayerRepository @Inject constructor() : PlayerRepository {
    override fun observePlayer(id: Int): Flow<Player?> = flowOf(null)

    override fun observePlayers(ids: Set<Int>): Flow<List<Player>> = flowOf(emptyList())

    override suspend fun getPlayerBySlug(slug: String): Player? = null

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}
