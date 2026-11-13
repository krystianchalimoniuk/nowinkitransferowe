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
import pl.nowinkitransferowe.core.data.repository.ClubSeasonsRepository
import pl.nowinkitransferowe.core.model.ClubSeason

class TestClubSeasonsRepository : ClubSeasonsRepository {

    private val seasonsFlow: MutableSharedFlow<Map<Int, List<ClubSeason>>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** Clubs whose seasons were refreshed, in order. */
    val refreshedClubs = mutableListOf<Int>()

    override fun observeClubSeasons(clubId: Int): Flow<List<ClubSeason>> = seasonsFlow.map { it[clubId].orEmpty() }

    override suspend fun refresh(clubId: Int): Boolean {
        refreshedClubs += clubId
        return true
    }

    /** A test-only API to allow controlling the seasons of the clubs, by club id. */
    fun sendSeasons(seasons: Map<Int, List<ClubSeason>>) {
        seasonsFlow.tryEmit(seasons)
    }
}
