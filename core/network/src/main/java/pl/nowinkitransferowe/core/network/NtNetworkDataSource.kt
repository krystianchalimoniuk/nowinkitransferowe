package pl.nowinkitransferowe.core.network

import pl.nowinkitransferowe.core.network.model.NetworkChangeList
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource

interface NtNetworkDataSource {
    suspend fun getTransferResources(ids: String? = null): List<NetworkTransferResource>

    suspend fun getNewsResources(ids: String? = null): List<NetworkNewsResource>

    suspend fun getNewsResourceChangeList(after: Int? = null): List<NetworkChangeList>

    suspend fun getTransferResourceChangeList(after: Int? = null): List<NetworkChangeList>
}