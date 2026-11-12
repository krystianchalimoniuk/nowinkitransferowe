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

package pl.nowinkitransferowe.core.data.testdoubles

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import pl.nowinkitransferowe.core.network.demo.DemoNtNetworkDataSource
import pl.nowinkitransferowe.core.network.demo.mapToChangeList
import pl.nowinkitransferowe.core.network.model.NetworkArticle
import pl.nowinkitransferowe.core.network.model.NetworkChange
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
import java.io.IOException

enum class CollectionType {
    TransferResources,
    NewsResources,
}

/**
 * Test double for [NtNetworkDataSource]
 */
class TestNtNetworkDataSource : NtNetworkDataSource {

    private val source = DemoNtNetworkDataSource(
        UnconfinedTestDispatcher(),
        Json { ignoreUnknownKeys = true },
    )

    private val allTransferResources = runBlocking { source.getTransferResources() }

    private val allNewsResources = runBlocking { source.getNewsResources() }

    private val changeLists: MutableMap<CollectionType, List<NetworkChangeList>> = mutableMapOf(
        CollectionType.TransferResources to allTransferResources
            .mapToChangeList(idGetter = NetworkTransferResource::id),
        CollectionType.NewsResources to allNewsResources
            .mapToChangeList(idGetter = NetworkNewsResource::id),
    )

    override suspend fun getTransferResources(ids: String?): List<NetworkTransferResource> = allTransferResources.matchIds(
        ids = ids?.split(","),
        idGetter = { networkTransferResource -> networkTransferResource.id.toString() },
    )

    override suspend fun getNewsResources(ids: String?): List<NetworkNewsResource> = allNewsResources.matchIds(
        ids = ids?.split(","),
        idGetter = { networkNewsResource -> networkNewsResource.id.toString() },
    )

    override suspend fun getTransferResourceChangeList(after: Int?): List<NetworkChangeList> = changeLists.getValue(CollectionType.TransferResources).after(after)

    override suspend fun getNewsResourceChangeList(after: Int?): List<NetworkChangeList> = changeLists.getValue(CollectionType.NewsResources).after(after)

    // API v3: records come from the demo data; the change lists can be edited like on the server.

    private val v3Changes: MutableMap<NetworkRecordType, List<NetworkChange>> = NetworkRecordType.entries.associateWithTo(mutableMapOf()) { type ->
        runBlocking { source.getChanges(type, after = 0, limit = Int.MAX_VALUE).changes }
    }

    /**
     * Largest page of changes the fake server returns, to test paging.
     */
    var maxChangesPerPage = Int.MAX_VALUE

    /**
     * Record type whose records cannot be fetched, to test an interrupted sync.
     */
    var failingRecordType: NetworkRecordType? = null

    /**
     * Ids asked for in each records request, by type.
     */
    val requestedIds: MutableMap<NetworkRecordType, MutableList<List<Int>>> = mutableMapOf()

    override suspend fun getChanges(type: NetworkRecordType, after: Int, limit: Int): NetworkChanges {
        val all = v3Changes.getValue(type)
        val newer = all.filter { it.version > after }.sortedBy(NetworkChange::version)
        val pageSize = minOf(limit, maxChangesPerPage)
        return NetworkChanges(
            changes = newer.take(pageSize),
            latestVersion = all.maxOfOrNull(NetworkChange::version) ?: 0,
            hasMore = newer.size > pageSize,
        )
    }

    fun latestVersion(type: NetworkRecordType): Int = v3Changes.getValue(type).maxOf(NetworkChange::version)

    /**
     * Records a change of [id] with a new version, mimicking the server's change list.
     */
    fun editRecord(type: NetworkRecordType, id: Int, deleted: Boolean = false) {
        val changes = v3Changes.getValue(type)
        v3Changes[type] = changes.filterNot { it.id == id } + NetworkChange(id = id, version = latestVersion(type) + 1, deleted = deleted)
    }

    private fun recordsRequest(type: NetworkRecordType, ids: List<Int>) {
        if (type == failingRecordType) throw IOException("${type.path} are not available")
        requestedIds.getOrPut(type, ::mutableListOf) += ids
    }

    override suspend fun getArticles(ids: List<Int>): List<NetworkArticle> = recordsRequest(NetworkRecordType.ARTICLES, ids).let { source.getArticles(ids) }

    override suspend fun getTransfers(ids: List<Int>): List<NetworkTransfer> = recordsRequest(NetworkRecordType.TRANSFERS, ids).let { source.getTransfers(ids) }

    override suspend fun getClubs(ids: List<Int>): List<NetworkClub> = recordsRequest(NetworkRecordType.CLUBS, ids).let { source.getClubs(ids) }

    override suspend fun getPlayers(ids: List<Int>): List<NetworkPlayer> = recordsRequest(NetworkRecordType.PLAYERS, ids).let { source.getPlayers(ids) }

    override suspend fun getRumours(ids: List<Int>): List<NetworkRumour> = recordsRequest(NetworkRecordType.RUMOURS, ids).let { source.getRumours(ids) }

    override suspend fun getRumourStats(): NetworkRumourStats = source.getRumourStats()

    override suspend fun getClubSeasons(clubId: Int): NetworkClubSeasons = source.getClubSeasons(clubId)

    override suspend fun getLeagues(season: String?): NetworkLeagues = source.getLeagues(season)

    /**
     * Number of /topics and /countries requests.
     */
    var dictionaryRequests = 0

    override suspend fun getTopics(): List<NetworkTopic> = source.getTopics().also { dictionaryRequests++ }

    override suspend fun getCountries(): List<NetworkCountry> = source.getCountries().also { dictionaryRequests++ }

    fun latestChangeListVersion(collectionType: CollectionType) = changeLists.getValue(collectionType).first().changeListVersion

    fun changeListsAfter(collectionType: CollectionType, version: Int) = changeLists.getValue(collectionType).after(version)

    /**
     * Edits the change list for the backing [collectionType] for the given [id] mimicking
     * the server's change list registry
     */
    fun editCollection(collectionType: CollectionType, id: String, isDelete: Boolean) {
        val changeList = changeLists.getValue(collectionType)
        val latestVersion = changeList.firstOrNull()?.changeListVersion ?: 0
        val change = NetworkChangeList(
            id = id,
            isDelete = isDelete,
            changeListVersion = latestVersion,
        )
        changeLists[collectionType] = changeList.filterNot { it.id == id } + change
    }
}

fun List<NetworkChangeList>.after(version: Int?): List<NetworkChangeList> = when (version) {
    null -> this
    else -> filter { it.changeListVersion > version }
}

/**
 * Return items from [this] whose id defined by [idGetter] is in [ids] if [ids] is not null
 */
private fun <T> List<T>.matchIds(
    ids: List<String>?,
    idGetter: (T) -> String,
) = when (ids) {
    null -> this
    else -> ids.toSet().let { idSet -> filter { idGetter(it) in idSet } }
}

/**
 * Maps items to a change list where the change list version is denoted by the index of each item.
 * [after] simulates which models have changed by excluding items before it
 */
private fun <T> List<T>.mapToChangeList(
    idGetter: (T) -> String,
) = mapIndexed { index, item ->
    NetworkChangeList(
        id = idGetter(item),
        changeListVersion = idGetter(item).toInt(),
        isDelete = false,
    )
}
