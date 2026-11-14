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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.model.clubEntities
import pl.nowinkitransferowe.core.data.model.playerEntities
import pl.nowinkitransferowe.core.data.model.stageEntities
import pl.nowinkitransferowe.core.data.model.tagEntities
import pl.nowinkitransferowe.core.data.model.topicEntities
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.ClubSeasonsRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.RumourStatsRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.database.model.PopulatedArticle
import pl.nowinkitransferowe.core.database.model.PopulatedRumour
import pl.nowinkitransferowe.core.database.model.PopulatedTransfer
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.ClubSeason
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.HotClub
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStats
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.StageAccuracy
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.demo.DemoNtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkClub
import pl.nowinkitransferowe.core.network.model.NetworkPlayer
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

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

    override fun observeArticleIdsAfter(id: Int): Flow<List<Int>> = articles.map { list -> list.map { it.id }.filter { it > id } }

    override suspend fun getNewestArticleId(): Int? = articles.first().maxOfOrNull { it.id }

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

    private val clubTopics: Flow<Map<Int, String?>> = flow {
        emit(datasource.getClubs(allIds(datasource, NetworkRecordType.CLUBS)).associate { it.id to it.topic })
    }

    override fun observeTransfers(season: Season?, topic: String?, limit: Int): Flow<List<Transfer>> = combine(transfers, clubTopics) { list, topics ->
        list
            .filter { season == null || it.season == season }
            .filter { transfer -> topic == null || listOf(transfer.from, transfer.to).any { it is ClubRef.Club && topics[it.id] == topic } }
            .take(limit)
    }

    override fun observeTransfers(ids: Set<Int>): Flow<List<Transfer>> = transfers.map { list -> list.filter { it.id in ids } }

    override fun observeTransferCount(): Flow<Int> = transfers.map { it.size }

    override fun observeTransferIdsAfter(id: Int): Flow<List<Int>> = transfers.map { list -> list.map { it.id }.filter { it > id } }

    override suspend fun getNewestTransferId(): Int? = transfers.first().maxOfOrNull { it.id }

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

class FakeRumourRepository @Inject constructor(
    private val datasource: DemoNtNetworkDataSource,
) : RumourRepository {
    private val clubs: Flow<Map<Int, NetworkClub>> = flow {
        emit(datasource.getClubs(allIds(datasource, NetworkRecordType.CLUBS)).associateBy(NetworkClub::id))
    }

    private val rumours: Flow<List<Rumour>> = combine(
        clubs,
        flow { emit(datasource.getPlayers(allIds(datasource, NetworkRecordType.PLAYERS)).associateBy(NetworkPlayer::id)) },
        flow { emit(datasource.getRumours(allIds(datasource, NetworkRecordType.RUMOURS))) },
    ) { clubs, players, rumours ->
        val transferIds = allIds(datasource, NetworkRecordType.TRANSFERS).toSet()
        val articleIds = allIds(datasource, NetworkRecordType.ARTICLES).toSet()
        rumours
            .mapNotNull { rumour ->
                val entity = rumour.asEntity(transferIds, articleIds) ?: return@mapNotNull null
                val from = rumour.fromClubId?.let(clubs::get)
                val to = rumour.toClubId?.let(clubs::get)
                PopulatedRumour(
                    rumour = entity,
                    currentPlayerName = players[rumour.playerId]?.name,
                    playerPhotoUrl = players[rumour.playerId]?.photoUrl,
                    playerNationality = players[rumour.playerId]?.nationality,
                    currentFromClubName = from?.name,
                    fromClubCrestUrl = from?.crestUrl,
                    currentToClubName = to?.name,
                    toClubCrestUrl = to?.crestUrl,
                    stages = rumour.stageEntities(articleIds),
                ).asExternalModel()
            }
            .sortedByDescending(Rumour::updatedAt)
            .movedToToday()
    }

    /**
     * The demo rumours keep their order and spacing but the newest one moves to yesterday, so they
     * stay on the board (the last 45 days) however old the demo data gets.
     */
    private fun List<Rumour>.movedToToday(): List<Rumour> {
        val newest = firstOrNull()?.updatedAt ?: return this
        val shift = Clock.System.now() - 1.days - newest
        return map { rumour ->
            rumour.copy(
                updatedAt = rumour.updatedAt + shift,
                history = rumour.history.map { it.copy(at = it.at + shift) },
            )
        }
    }

    override fun observeRumour(id: Int): Flow<Rumour?> = rumours.map { list -> list.find { it.id == id } }

    override fun observeRumours(
        status: RumourStatus,
        updatedSince: Instant,
        topic: String?,
        stage: RumourStage?,
    ): Flow<List<Rumour>> = combine(rumours, clubs) { list, clubs ->
        list
            .filter { it.status == status && it.updatedAt >= updatedSince }
            .filter { stage == null || it.stage == stage }
            .filter { rumour -> topic == null || listOfNotNull(rumour.from, rumour.to).any { it is ClubRef.Club && clubs[it.id]?.topic == topic } }
    }

    override fun observeRumours(ids: Set<Int>): Flow<List<Rumour>> = rumours.map { list -> list.filter { it.id in ids } }

    override fun observeRumoursOfArticle(articleId: Int): Flow<List<Rumour>> = rumours.map { list ->
        list.filter { rumour -> rumour.latestArticleId == articleId || rumour.history.any { it.articleId == articleId } }
    }

    override fun observeRumoursOfClub(clubId: Int): Flow<List<Rumour>> = rumours.map { list ->
        list.filter { rumour -> listOfNotNull(rumour.from, rumour.to).any { it is ClubRef.Club && it.id == clubId } }
    }

    override fun observeRumoursOfPlayer(playerId: Int): Flow<List<Rumour>> = rumours.map { list -> list.filter { it.player.id == playerId } }

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakeClubRepository @Inject constructor(
    private val datasource: DemoNtNetworkDataSource,
) : ClubRepository {
    private val clubs: Flow<List<Club>> = flow {
        emit(datasource.getClubs(allIds(datasource, NetworkRecordType.CLUBS)).map { it.asEntity().asExternalModel() })
    }

    override fun observeClub(id: Int): Flow<Club?> = clubs.map { list -> list.find { it.id == id } }

    override fun observeClubs(ids: Set<Int>): Flow<List<Club>> = clubs.map { list -> list.filter { it.id in ids }.sortedBy(Club::name) }

    override suspend fun getClubBySlug(slug: String): Club? = datasource.getClubs(allIds(datasource, NetworkRecordType.CLUBS))
        .find { it.slug == slug }
        ?.asEntity()
        ?.asExternalModel()

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

class FakePlayerRepository @Inject constructor() : PlayerRepository {
    override fun observePlayer(id: Int): Flow<Player?> = flowOf(null)

    override fun observePlayers(ids: Set<Int>): Flow<List<Player>> = flowOf(emptyList())

    override suspend fun getPlayerBySlug(slug: String): Player? = null

    override suspend fun syncWith(synchronizer: Synchronizer) = true
}

/** The demo stats of the season, as the server would count them. */
class FakeRumourStatsRepository @Inject constructor(
    private val datasource: DemoNtNetworkDataSource,
) : RumourStatsRepository {
    override fun observeRumourStats(): Flow<RumourStats?> = flow {
        val stats = datasource.getRumourStats()
        val season = Season.parse(stats.season) ?: return@flow emit(null)
        emit(
            RumourStats(
                season = season,
                active = stats.active,
                confirmed = stats.confirmed,
                failed = stats.failed,
                showAccuracy = stats.showAccuracy,
                byStage = stats.byStage.map { StageAccuracy(RumourStage.fromLevel(it.stage), it.confirmed, it.closed) },
                hotClubs = stats.hotClubs.map { HotClub(it.clubId, it.rumours) },
                daysToConfirm = stats.daysToConfirm,
            ),
        )
    }

    override suspend fun refresh() = true
}

/** The demo seasons of the clubs, as the server would count them. */
class FakeClubSeasonsRepository @Inject constructor(
    private val datasource: DemoNtNetworkDataSource,
) : ClubSeasonsRepository {
    override fun observeClubSeasons(clubId: Int): Flow<List<ClubSeason>> = flow {
        emit(
            datasource.getClubSeasons(clubId).seasons.mapNotNull { season ->
                ClubSeason(
                    season = Season.parse(season.season) ?: return@mapNotNull null,
                    spent = season.spent,
                    earned = season.earned,
                    balance = season.balance,
                    arrivals = season.arrivals,
                    departures = season.departures,
                )
            },
        )
    }

    override suspend fun refresh(clubId: Int) = true
}
