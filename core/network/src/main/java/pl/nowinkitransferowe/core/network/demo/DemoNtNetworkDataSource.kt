package pl.nowinkitransferowe.core.network.demo

import JvmUnitTestDemoAssetManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import pl.nowinkitransferowe.core.common.network.Dispatcher
import pl.nowinkitransferowe.core.common.network.NtDispatchers
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkChangeList
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource
import javax.inject.Inject

/**
 * [NtNetworkDataSource] implementation that provides static news resources to aid development
 */
class DemoNtNetworkDataSource @Inject constructor(
    @Dispatcher(NtDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
    private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager,
) : NtNetworkDataSource {
    override suspend fun getTransferResources(ids: String?): List<NetworkTransferResource> =
        withContext(ioDispatcher) {
            assets.open(TRANSFERS_ASSET).use(networkJson::decodeFromStream)
        }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getNewsResources(ids: String?): List<NetworkNewsResource> =
        withContext(ioDispatcher) {
            assets.open(NEWS_ASSET).use(networkJson::decodeFromStream)
        }

    override suspend fun getNewsResourceChangeList(after: Int?): List<NetworkChangeList> =
        getNewsResources().mapToChangeList(NetworkNewsResource::id)

    override suspend fun getTransferResourceChangeList(after: Int?): List<NetworkChangeList> =
        getTransferResources().mapToChangeList(NetworkTransferResource::id)

    companion object {
        private const val NEWS_ASSET = "news.json"
        private const val TRANSFERS_ASSET = "transfers.json"
    }
}

/**
 * Converts a list of [T] to change list of all the items in it where [idGetter] defines the
 * [NetworkChangeList.id]
 */
fun <T> List<T>.mapToChangeList(
    idGetter: (T) -> Int,
) = mapIndexed { index, item ->
    NetworkChangeList(
        id = idGetter(item).toString(),
        changeListVersion = idGetter(item),
        isDelete = false,
    )
}
