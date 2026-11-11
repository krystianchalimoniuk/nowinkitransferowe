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
import pl.nowinkitransferowe.core.model.Player

/**
 * API v3 players, saved offline. A player referred to by another record before their own sync
 * is a stub ([Player.isStub]) with only a name.
 */
interface PlayerRepository : Syncable {
    fun observePlayer(id: Int): Flow<Player?>

    /**
     * Synced players with the given ids, by name. Stubs are left out.
     */
    fun observePlayers(ids: Set<Int>): Flow<List<Player>>

    suspend fun getPlayerBySlug(slug: String): Player?
}
