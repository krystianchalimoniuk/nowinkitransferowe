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

    override fun getTransferResources(query: TransferResourceQuery): Flow<List<TransferResource>> =
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

    override fun getTransferResource(id: String): Flow<TransferResource> =
        transferResourcesFlow.map { transferResources ->
            transferResources.first { it.id == id }
        }

    override fun getTransferResourceByName(name: String): Flow<List<TransferResource>> =
        transferResourcesFlow.map { transferResources ->
            transferResources.filter { it.name == name }
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
