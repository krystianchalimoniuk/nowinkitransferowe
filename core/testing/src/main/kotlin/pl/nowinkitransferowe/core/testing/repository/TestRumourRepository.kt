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
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStatus

class TestRumourRepository : RumourRepository {

    private val rumoursFlow: MutableSharedFlow<List<Rumour>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun observeRumour(id: Int): Flow<Rumour?> = rumoursFlow.map { rumours -> rumours.find { it.id == id } }

    override fun observeRumours(status: RumourStatus, updatedSince: Instant): Flow<List<Rumour>> = rumoursFlow.map { rumours ->
        rumours.filter { it.status == status && it.updatedAt >= updatedSince }.sortedByDescending(Rumour::updatedAt)
    }

    override fun observeRumoursOfClub(clubId: Int): Flow<List<Rumour>> = rumoursFlow.map { rumours ->
        rumours.filter { rumour -> listOfNotNull(rumour.from, rumour.to).any { it is ClubRef.Club && it.id == clubId } }
    }

    override fun observeRumoursOfPlayer(playerId: Int): Flow<List<Rumour>> = rumoursFlow.map { rumours -> rumours.filter { it.player.id == playerId } }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /** A test-only API to allow controlling the list of rumours from tests. */
    fun sendRumours(rumours: List<Rumour>) {
        rumoursFlow.tryEmit(rumours)
    }
}
