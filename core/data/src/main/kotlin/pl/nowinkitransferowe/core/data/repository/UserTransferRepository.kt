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
import pl.nowinkitransferowe.core.model.UserTransfer

/**
 * Transfers joined with the user's data.
 */
interface UserTransferRepository {
    /** The newest [limit] transfers. */
    fun observeTransfers(limit: Int = Int.MAX_VALUE): Flow<List<UserTransfer>>

    fun observeTransfer(id: Int): Flow<UserTransfer?>

    /** Every transfer of the player with [playerId], newest first. */
    fun observeTransfersOfPlayer(playerId: Int): Flow<List<UserTransfer>>

    fun observeBookmarked(): Flow<List<UserTransfer>>

    fun observeTransferCount(): Flow<Int>
}
