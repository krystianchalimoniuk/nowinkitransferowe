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

package pl.nowinkitransferowe.core.network

import pl.nowinkitransferowe.core.network.model.NetworkArticle
import pl.nowinkitransferowe.core.network.model.NetworkChangeList
import pl.nowinkitransferowe.core.network.model.NetworkChanges
import pl.nowinkitransferowe.core.network.model.NetworkClub
import pl.nowinkitransferowe.core.network.model.NetworkClubSeasons
import pl.nowinkitransferowe.core.network.model.NetworkCountry
import pl.nowinkitransferowe.core.network.model.NetworkLeagues
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.network.model.NetworkPlayer
import pl.nowinkitransferowe.core.network.model.NetworkRumour
import pl.nowinkitransferowe.core.network.model.NetworkRumourStats
import pl.nowinkitransferowe.core.network.model.NetworkTopic
import pl.nowinkitransferowe.core.network.model.NetworkTransfer
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource

interface NtNetworkDataSource {
    suspend fun getTransferResources(ids: String? = null): List<NetworkTransferResource>

    suspend fun getNewsResources(ids: String? = null): List<NetworkNewsResource>

    suspend fun getNewsResourceChangeList(after: Int? = null): List<NetworkChangeList>

    suspend fun getTransferResourceChangeList(after: Int? = null): List<NetworkChangeList>

    // API v3. The v2 calls above stay until the repositories move to v3.

    /**
     * Changes of [type] with a version greater than [after], at most [limit] of them.
     */
    suspend fun getChanges(
        type: NetworkRecordType,
        after: Int,
        limit: Int = MAX_CHANGES_PER_PAGE,
    ): NetworkChanges

    /**
     * Records with the given ids, at most [MAX_IDS_PER_REQUEST]; ids deleted meanwhile are skipped.
     */
    suspend fun getArticles(ids: List<Int>): List<NetworkArticle>

    suspend fun getTransfers(ids: List<Int>): List<NetworkTransfer>

    suspend fun getClubs(ids: List<Int>): List<NetworkClub>

    suspend fun getPlayers(ids: List<Int>): List<NetworkPlayer>

    suspend fun getRumours(ids: List<Int>): List<NetworkRumour>

    suspend fun getRumourStats(): NetworkRumourStats

    /**
     * Spending, income and transfers of the club with [clubId] by season, newest first.
     */
    suspend fun getClubSeasons(clubId: Int): NetworkClubSeasons

    /**
     * Leagues of the season with the given slug (for example `26-27`), or of the current season.
     */
    suspend fun getLeagues(season: String? = null): NetworkLeagues

    suspend fun getTopics(): List<NetworkTopic>

    suspend fun getCountries(): List<NetworkCountry>
}

/**
 * Record types that have an API v3 change list; [path] is the type in the URL.
 */
enum class NetworkRecordType(val path: String) {
    ARTICLES("articles"),
    TRANSFERS("transfers"),
    CLUBS("clubs"),
    PLAYERS("players"),
    RUMOURS("rumours"),
}

/** The most ids API v3 accepts in one records request. */
const val MAX_IDS_PER_REQUEST = 100

/** The most changes API v3 returns in one page. */
const val MAX_CHANGES_PER_PAGE = 1000
