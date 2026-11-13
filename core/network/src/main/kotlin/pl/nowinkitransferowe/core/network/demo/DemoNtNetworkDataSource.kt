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

package pl.nowinkitransferowe.core.network.demo

import JvmUnitTestDemoAssetManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import pl.nowinkitransferowe.core.common.network.Dispatcher
import pl.nowinkitransferowe.core.common.network.NtDispatchers
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkArticle
import pl.nowinkitransferowe.core.network.model.NetworkChange
import pl.nowinkitransferowe.core.network.model.NetworkChanges
import pl.nowinkitransferowe.core.network.model.NetworkClub
import pl.nowinkitransferowe.core.network.model.NetworkClubSeasons
import pl.nowinkitransferowe.core.network.model.NetworkCountry
import pl.nowinkitransferowe.core.network.model.NetworkLeagues
import pl.nowinkitransferowe.core.network.model.NetworkPlayer
import pl.nowinkitransferowe.core.network.model.NetworkRumour
import pl.nowinkitransferowe.core.network.model.NetworkRumourStats
import pl.nowinkitransferowe.core.network.model.NetworkTopic
import pl.nowinkitransferowe.core.network.model.NetworkTransfer
import javax.inject.Inject

/**
 * [NtNetworkDataSource] implementation that provides static news resources to aid development
 */
class DemoNtNetworkDataSource @Inject constructor(
    @Dispatcher(NtDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
    private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager,
) : NtNetworkDataSource {
    // API v3: the assets in v3/ are a small set of records recorded from the website. A record's
    // id is also its change list version.

    override suspend fun getChanges(type: NetworkRecordType, after: Int, limit: Int): NetworkChanges {
        val ids = when (type) {
            NetworkRecordType.ARTICLES -> getArticles().map(NetworkArticle::id)
            NetworkRecordType.TRANSFERS -> getTransfers().map(NetworkTransfer::id)
            NetworkRecordType.CLUBS -> getClubs().map(NetworkClub::id)
            NetworkRecordType.PLAYERS -> getPlayers().map(NetworkPlayer::id)
            NetworkRecordType.RUMOURS -> getRumours().map(NetworkRumour::id)
        }.sorted()
        val newer = ids.filter { it > after }
        return NetworkChanges(
            changes = newer.take(limit).map { NetworkChange(id = it, version = it, deleted = false) },
            latestVersion = ids.lastOrNull() ?: 0,
            hasMore = newer.size > limit,
        )
    }

    override suspend fun getArticles(ids: List<Int>): List<NetworkArticle> = getArticles().withIds(ids, NetworkArticle::id)

    override suspend fun getTransfers(ids: List<Int>): List<NetworkTransfer> = getTransfers().withIds(ids, NetworkTransfer::id)

    override suspend fun getClubs(ids: List<Int>): List<NetworkClub> = getClubs().withIds(ids, NetworkClub::id)

    override suspend fun getPlayers(ids: List<Int>): List<NetworkPlayer> = getPlayers().withIds(ids, NetworkPlayer::id)

    override suspend fun getRumours(ids: List<Int>): List<NetworkRumour> = getRumours().withIds(ids, NetworkRumour::id)

    override suspend fun getRumourStats(): NetworkRumourStats = getDemoObjectFromJson(RUMOUR_STATS_ASSET)

    override suspend fun getClubSeasons(clubId: Int): NetworkClubSeasons = getDemoDataFromJson<NetworkClubSeasons>(CLUB_SEASONS_ASSET)
        .find { it.clubId == clubId }
        ?: NetworkClubSeasons(clubId = clubId)

    override suspend fun getLeagues(season: String?): NetworkLeagues = getDemoObjectFromJson(LEAGUES_ASSET)

    override suspend fun getTopics(): List<NetworkTopic> = getDemoDataFromJson(TOPICS_ASSET)

    override suspend fun getCountries(): List<NetworkCountry> = getDemoDataFromJson(COUNTRIES_ASSET)

    private suspend fun getArticles(): List<NetworkArticle> = getDemoDataFromJson(ARTICLES_ASSET)

    private suspend fun getTransfers(): List<NetworkTransfer> = getDemoDataFromJson(TRANSFERS_ASSET)

    private suspend fun getClubs(): List<NetworkClub> = getDemoDataFromJson(CLUBS_ASSET)

    private suspend fun getPlayers(): List<NetworkPlayer> = getDemoDataFromJson(PLAYERS_ASSET)

    private suspend fun getRumours(): List<NetworkRumour> = getDemoDataFromJson(RUMOURS_ASSET)

    /**
     * Get Demo data form a [fileName] Json file.
     */
    private suspend inline fun <reified T> getDemoDataFromJson(fileName: String): List<T> = getDemoObjectFromJson(fileName)

    @OptIn(ExperimentalSerializationApi::class)
    private suspend inline fun <reified T> getDemoObjectFromJson(fileName: String): T = withContext(ioDispatcher) {
        assets.open(fileName).use { inputStream ->
            networkJson.decodeFromStream(inputStream)
        }
    }

    companion object {
        private const val ARTICLES_ASSET = "v3/articles.json"
        private const val TRANSFERS_ASSET = "v3/transfers.json"
        private const val CLUBS_ASSET = "v3/clubs.json"
        private const val PLAYERS_ASSET = "v3/players.json"
        private const val RUMOURS_ASSET = "v3/rumours.json"
        private const val RUMOUR_STATS_ASSET = "v3/rumour_stats.json"

        // Computed from the demo transfers by the server rules, so the totals are smaller than on the website.
        private const val CLUB_SEASONS_ASSET = "v3/club_seasons.json"
        private const val LEAGUES_ASSET = "v3/leagues.json"
        private const val TOPICS_ASSET = "v3/topics.json"
        private const val COUNTRIES_ASSET = "v3/countries.json"
    }
}

/**
 * The records whose id is in [ids], like the API, which skips ids it does not know.
 */
private fun <T> List<T>.withIds(ids: List<Int>, idGetter: (T) -> Int): List<T> = filter { idGetter(it) in ids }
