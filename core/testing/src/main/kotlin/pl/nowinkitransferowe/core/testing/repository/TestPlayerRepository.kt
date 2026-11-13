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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.model.Player

class TestPlayerRepository : PlayerRepository {

    private val players = MutableStateFlow(emptyList<Player>())

    override fun observePlayer(id: Int): Flow<Player?> = players.map { list -> list.find { it.id == id } }

    override fun observePlayers(ids: Set<Int>): Flow<List<Player>> = players.map { list -> list.filter { it.id in ids && !it.isStub }.sortedBy { it.name } }

    override suspend fun getPlayerBySlug(slug: String): Player? = players.value.find { it.slug == slug }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /** A test-only API to allow controlling the players from tests. */
    fun sendPlayers(players: List<Player>) {
        this.players.value = players
    }
}
