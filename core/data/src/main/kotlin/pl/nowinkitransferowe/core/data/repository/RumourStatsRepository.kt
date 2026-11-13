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
import pl.nowinkitransferowe.core.model.RumourStats

/**
 * Season numbers of the rumours. They have no change list: [refresh] fetches them when the
 * stored ones are older than an hour, and the last response stays for offline use.
 */
interface RumourStatsRepository {
    /** Stats of the newest stored season, or null before the first fetch. */
    fun observeRumourStats(): Flow<RumourStats?>

    /** Fetches the stats unless they are fresh; returns false when the fetch failed. */
    suspend fun refresh(): Boolean
}
