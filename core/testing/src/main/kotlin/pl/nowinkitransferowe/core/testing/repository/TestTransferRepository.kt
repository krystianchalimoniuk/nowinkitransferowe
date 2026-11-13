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

package pl.nowinkitransferowe.core.testing.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.Transfer

class TestTransferRepository : TransferRepository {

    private val transfersFlow: MutableSharedFlow<List<Transfer>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun observeTransfer(id: Int): Flow<Transfer?> = transfersFlow.map { transfers -> transfers.find { it.id == id } }

    override fun observeTransfers(season: Season?, limit: Int): Flow<List<Transfer>> = transfersFlow.map { transfers ->
        transfers.filter { season == null || it.season == season }.take(limit)
    }

    override fun observeTransfers(ids: Set<Int>): Flow<List<Transfer>> = transfersFlow.map { transfers -> transfers.filter { it.id in ids } }

    override fun observeTransferCount(): Flow<Int> = transfersFlow.map { it.size }

    override fun observeTransfersOfClub(clubId: Int): Flow<List<Transfer>> = transfersFlow.map { transfers ->
        transfers.filter { transfer -> listOf(transfer.from, transfer.to).any { it is ClubRef.Club && it.id == clubId } }
    }

    override fun observeTransfersOfPlayer(playerId: Int): Flow<List<Transfer>> = transfersFlow.map { transfers -> transfers.filter { it.player.id == playerId } }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /**
     * A test-only API to allow controlling the list of transfers from tests.
     */
    fun sendTransfers(transfers: List<Transfer>) {
        transfersFlow.tryEmit(transfers)
    }
}
