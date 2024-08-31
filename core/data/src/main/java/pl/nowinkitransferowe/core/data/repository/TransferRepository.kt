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

package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.data.Syncable
import pl.nowinkitransferowe.core.model.TransferResource

/**
 * Encapsulation class for query parameters for [TransferResource]
 */
data class TransferResourceQuery(
    val filterTransferIds: Set<String>? = null,
    val footballerName: String? = null
)

/**
 * Data layer implementation for [TransferResource]
 */
interface TransferRepository : Syncable {
    /**
     * Returns available news resources that match the specified [query].
     */
    fun getTransferResources(
        query: TransferResourceQuery = TransferResourceQuery(filterTransferIds = null, footballerName = null),
    ): Flow<List<TransferResource>>

    /**
     * Returns available news resources that match the specified [limit] and [offset].
     */
    fun getTransferResourcesPages(
        limit: Int,
        offset: Int,
    ): Flow<List<TransferResource>>

    /**
     * Returns available transfer resource that match the specified [id].
     */
    fun getTransferResource(id: String): Flow<TransferResource>

    /**
     * Returns available transfer resource that match the specified [name].
     */
    fun getTransferResourceByName(name: String): Flow<List<TransferResource>>

    /**
     * Returns counts number.
     */
    fun getCount(): Flow<Int>
}
