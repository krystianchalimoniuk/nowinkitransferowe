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
import pl.nowinkitransferowe.core.model.UserTransferResource

interface UserTransferResourceRepository {
    /**
     * Returns available transfers resources as a stream.
     */
    fun observeAll(
        query: TransferResourceQuery = TransferResourceQuery(
            filterTransferIds = null,
        ),
    ): Flow<List<UserTransferResource>>

    /**
     * Returns the user's bookmarked transfer resources as a stream.
     */
    fun observeAllBookmarked(): Flow<List<UserTransferResource>>

    /**
     * Returns available news resources that match the specified [limit] and [offset].
     */
    fun observeAllPages(
        limit: Int,
        offset: Int,
    ): Flow<List<UserTransferResource>>

    /**
     * Returns counts number.
     */
    fun getCount(): Flow<Int>
}
