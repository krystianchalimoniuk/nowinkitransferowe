package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.data.Syncable
import pl.nowinkitransferowe.core.model.TransferResource


/**
 * Encapsulation class for query parameters for [TransferResource]
 */
data class TransferResourceQuery(
    val filterTransferIds: Set<String>? = null,
)

/**
 * Data layer implementation for [TransferResource]
 */
interface TransferRepository : Syncable {
    /**
     * Returns available news resources that match the specified [query].
     */
    fun getTransferResources(
        query: TransferResourceQuery = TransferResourceQuery(filterTransferIds = null),
    ): Flow<List<TransferResource>>

    /**
     * Returns available news resources that match the specified [limit] and [offset].
     */
    fun getTransferResourcesPages(
        limit: Int, offset: Int,
    ): Flow<List<TransferResource>>

    /**
     * Returns counts number.
     */
    fun getCount(): Flow<Int>
}