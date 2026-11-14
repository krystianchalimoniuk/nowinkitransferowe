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
import pl.nowinkitransferowe.core.data.Syncable
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.Transfer

/**
 * API v3 transfers, saved offline. Club and player names are the current ones.
 */
interface TransferRepository : Syncable {
    fun observeTransfer(id: Int): Flow<Transfer?>

    /**
     * Newest transfers first, optionally only from [season] and to or from a club of the league
     * [topic].
     */
    fun observeTransfers(season: Season? = null, topic: String? = null, limit: Int = Int.MAX_VALUE): Flow<List<Transfer>>

    fun observeTransfers(ids: Set<Int>): Flow<List<Transfer>>

    fun observeTransferCount(): Flow<Int>

    /** Ids of the transfers added after the one with [id]. */
    fun observeTransferIdsAfter(id: Int): Flow<List<Int>>

    /** The id of the newest transfer, or null when there are none. */
    suspend fun getNewestTransferId(): Int?

    fun observeTransfersOfClub(clubId: Int): Flow<List<Transfer>>

    fun observeTransfersOfPlayer(playerId: Int): Flow<List<Transfer>>
}
