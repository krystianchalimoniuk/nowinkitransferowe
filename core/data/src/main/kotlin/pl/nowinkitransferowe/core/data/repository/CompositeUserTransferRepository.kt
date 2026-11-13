/*
 * Copyright 2026 The Android Open Source Project
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
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.model.mapToUserTransfers
import javax.inject.Inject

/**
 * Implements a [UserTransferRepository] by combining a [TransferRepository] with the
 * [UserDataRepository].
 */
class CompositeUserTransferRepository @Inject constructor(
    private val transferRepository: TransferRepository,
    private val userDataRepository: UserDataRepository,
) : UserTransferRepository {

    override fun observeTransfers(limit: Int): Flow<List<UserTransfer>> = transferRepository.observeTransfers(limit = limit).withUserData()

    override fun observeTransfer(id: Int): Flow<UserTransfer?> = transferRepository.observeTransfer(id)
        .map(::listOfNotNull)
        .withUserData()
        .map { it.firstOrNull() }

    override fun observeTransfersOfPlayer(playerId: Int): Flow<List<UserTransfer>> = transferRepository.observeTransfersOfPlayer(playerId).withUserData()

    override fun observeBookmarked(): Flow<List<UserTransfer>> = userDataRepository.userData
        .map { userData -> userData.bookmarkedTransferResources.mapNotNullTo(mutableSetOf(), String::toIntOrNull) }
        .distinctUntilChanged()
        .flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList()) else transferRepository.observeTransfers(ids).withUserData()
        }

    override fun observeTransferCount(): Flow<Int> = transferRepository.observeTransferCount()

    private fun Flow<List<Transfer>>.withUserData(): Flow<List<UserTransfer>> = combine(this, userDataRepository.userData) { transfers, userData ->
        transfers.mapToUserTransfers(userData)
    }
}
