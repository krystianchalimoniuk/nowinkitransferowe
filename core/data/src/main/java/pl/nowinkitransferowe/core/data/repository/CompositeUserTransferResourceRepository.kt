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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.model.mapToUserTransferResources
import javax.inject.Inject

class CompositeUserTransferResourceRepository @Inject constructor(
    val transferRepository: TransferRepository,
    val userDataRepository: UserDataRepository,
) : UserTransferResourceRepository {

    /**
     * Returns available news resources (joined with user data) matching the given query.
     */
    override fun observeAll(
        query: TransferResourceQuery,
    ): Flow<List<UserTransferResource>> =
        transferRepository.getTransferResources(query)
            .combine(userDataRepository.userData) { transferResources, userData ->
                transferResources.mapToUserTransferResources(userData)
            }

    override fun observeAllBookmarked(): Flow<List<UserTransferResource>> =
        userDataRepository.userData.map { it.bookmarkedTransferResources }.distinctUntilChanged()
            .flatMapLatest { bookmarkedTransferResources ->
                when {
                    bookmarkedTransferResources.isEmpty() -> flowOf(emptyList())
                    else -> observeAll(TransferResourceQuery(filterTransferIds = bookmarkedTransferResources))
                }
            }

    override fun observeAllPages(
        limit: Int,
        offset: Int,
    ): Flow<List<UserTransferResource>> =
        transferRepository.getTransferResourcesPages(limit, offset)
            .combine(userDataRepository.userData) { transferResources, userData ->
                transferResources.mapToUserTransferResources(userData)
            }

    override fun getCount(): Flow<Int> =
        transferRepository.getCount()
}
