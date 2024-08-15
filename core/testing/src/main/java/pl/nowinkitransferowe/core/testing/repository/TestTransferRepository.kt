package pl.nowinkitransferowe.core.testing.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.data.repository.TransferResourceQuery
import pl.nowinkitransferowe.core.model.TransferResource

class TestTransferRepository : TransferRepository {

    /**
     * The backing hot flow for the list of topics ids for testing.
     */
    private val transferResourcesFlow: MutableSharedFlow<List<TransferResource>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun getTransferResources(query: TransferResourceQuery): Flow<List<TransferResource>>  =
        transferResourcesFlow.map { transferResources ->
            var result = transferResources
            query.filterTransferIds?.let { filterTransferIds ->
                result = transferResources.filter { it.id in filterTransferIds }
            }
            result
        }

    override fun getTransferResourcesPages(limit: Int, offset: Int): Flow<List<TransferResource>> =
    transferResourcesFlow.map { transferResources ->
        transferResources.take(limit)
    }

    override fun getCount(): Flow<Int> =
        transferResourcesFlow.map { transferResources ->
            transferResources.count()
        }


    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /**
     * A test-only API to allow controlling the list of transfer resources from tests.
     */
    fun sendTransferResources(transferResources: List<TransferResource>) {
        transferResourcesFlow.tryEmit(transferResources)
    }
}