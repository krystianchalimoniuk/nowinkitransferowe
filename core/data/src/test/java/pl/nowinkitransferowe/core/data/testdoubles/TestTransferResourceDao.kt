package pl.nowinkitransferowe.core.data.testdoubles

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import pl.nowinkitransferowe.core.database.dao.TransferResourceDao
import pl.nowinkitransferowe.core.database.model.TransferResourceEntity

/**
 * Test double for [TransferResourceDao]
 */
class TestTransferResourceDao : TransferResourceDao {

    private val entitiesStateFlow = MutableStateFlow(emptyList<TransferResourceEntity>())

    override fun getTransferResources(
        useFilterTransferIds: Boolean,
        filterTransferIds: Set<String>,
    ): Flow<List<TransferResourceEntity>> =
        entitiesStateFlow
            .map { resources ->
                var result = resources
                if (useFilterTransferIds) {
                    result = result.filter { resource ->
                        resource.id in filterTransferIds
                    }
                }
                result
            }

    override fun getTransferResourcesPages(limit: Int, offset: Int): Flow<List<TransferResourceEntity>> =
        entitiesStateFlow
            .map { resources ->
                resources.take(limit)
            }

    override fun getTransferResourceIds(
        useFilterTransferIds: Boolean,
        filterTransferIds: Set<String>,
    ): Flow<List<String>> =
        entitiesStateFlow
            .map { resources ->
                var result = resources
                if (useFilterTransferIds) {
                    result = result.filter { resource ->
                        resource.id in filterTransferIds
                    }
                }
                result.map { it.id }
            }

    override suspend fun upsertTransferResources(transferResourceEntities: List<TransferResourceEntity>) {
        entitiesStateFlow.update { oldValues ->
            // New values come first so they overwrite old values
            (transferResourceEntities + oldValues)
                .distinctBy(TransferResourceEntity::id)
                .sortedWith(
                    compareBy(TransferResourceEntity::id).reversed(),
                )
        }
    }


    override suspend fun deleteTransferResources(ids: List<String>) {
        val idSet = ids.toSet()
        entitiesStateFlow.update { entities ->
            entities.filterNot { it.id in idSet }
        }
    }

    override fun getCount(): Flow<Int> =
        entitiesStateFlow.map {
            it.size
        }

}


