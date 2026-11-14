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
import pl.nowinkitransferowe.core.model.League
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.SeasonOption

/**
 * Spending and income of the leagues by season, counted by the server. They have no change list:
 * [refresh] fetches a season when the stored one is older than an hour, and the last response
 * stays for offline use.
 */
interface LeaguesRepository {
    /** The seasons that can be shown, newest first, as the last response listed them. */
    fun observeSeasons(): Flow<List<SeasonOption>>

    /** The leagues of [season] in the server's order. */
    fun observeLeagues(season: Season): Flow<List<League>>

    /** Fetches [season] (the current one when null) unless it is fresh; false when it failed. */
    suspend fun refresh(season: Season? = null): Boolean
}
