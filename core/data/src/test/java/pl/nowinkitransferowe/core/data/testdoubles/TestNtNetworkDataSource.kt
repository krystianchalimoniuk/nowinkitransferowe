package pl.nowinkitransferowe.core.data.testdoubles

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import pl.nowinkitransferowe.core.network.demo.DemoNtNetworkDataSource
import pl.nowinkitransferowe.core.network.demo.mapToChangeList
import pl.nowinkitransferowe.core.network.model.NetworkChangeList
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource

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

    override suspend fun getTransferResources(ids: String?): List<NetworkTransferResource> =
        allTransferResources.matchIds(
            ids = ids?.split(","),
            idGetter = { networkTransferResource -> networkTransferResource.id.toString() },
        )

    override suspend fun getNewsResources(ids: String?): List<NetworkNewsResource> =
        allNewsResources.matchIds(
            ids = ids?.split(","),
            idGetter = { networkNewsResource -> networkNewsResource.id.toString() },
        )

    override suspend fun getTransferResourceChangeList(after: Int?): List<NetworkChangeList> =
        changeLists.getValue(CollectionType.TransferResources).after(after)

    override suspend fun getNewsResourceChangeList(after: Int?): List<NetworkChangeList> =
        changeLists.getValue(CollectionType.NewsResources).after(after)

    fun latestChangeListVersion(collectionType: CollectionType) =
        changeLists.getValue(collectionType).first().changeListVersion

    fun changeListsAfter(collectionType: CollectionType, version: Int) =
        changeLists.getValue(collectionType).after(version)

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
