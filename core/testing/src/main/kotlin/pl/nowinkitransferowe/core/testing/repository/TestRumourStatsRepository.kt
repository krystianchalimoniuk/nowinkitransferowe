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
import pl.nowinkitransferowe.core.data.repository.RumourStatsRepository
import pl.nowinkitransferowe.core.model.RumourStats

class TestRumourStatsRepository : RumourStatsRepository {

    private val statsFlow: MutableSharedFlow<RumourStats?> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** How many times the stats were refreshed. */
    var refreshCount = 0
        private set

    override fun observeRumourStats(): Flow<RumourStats?> = statsFlow

    override suspend fun refresh(): Boolean {
        refreshCount++
        return true
    }

    /** A test-only API to allow controlling the stats from tests. */
    fun sendStats(stats: RumourStats?) {
        statsFlow.tryEmit(stats)
    }
}
