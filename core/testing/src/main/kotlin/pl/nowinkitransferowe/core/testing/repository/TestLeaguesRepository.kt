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
import pl.nowinkitransferowe.core.data.repository.LeaguesRepository
import pl.nowinkitransferowe.core.model.League
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.SeasonOption

class TestLeaguesRepository : LeaguesRepository {

    private val seasonsFlow = MutableSharedFlow<List<SeasonOption>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val leaguesFlow = MutableSharedFlow<Map<Season, List<League>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** Seasons refreshed, in order; null for the current one. */
    val refreshedSeasons = mutableListOf<Season?>()

    override fun observeSeasons(): Flow<List<SeasonOption>> = seasonsFlow

    override fun observeLeagues(season: Season): Flow<List<League>> = leaguesFlow.map { it[season].orEmpty() }

    override suspend fun refresh(season: Season?): Boolean {
        refreshedSeasons += season
        return true
    }

    /** A test-only API to control the seasons and the leagues of each season. */
    fun send(seasons: List<SeasonOption>, leagues: Map<Season, List<League>>) {
        seasonsFlow.tryEmit(seasons)
        leaguesFlow.tryEmit(leagues)
    }
}
