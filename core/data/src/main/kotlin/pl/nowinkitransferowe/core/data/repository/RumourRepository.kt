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
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.data.Syncable
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStatus

/**
 * API v3 rumours with their stage history, saved offline.
 */
interface RumourRepository : Syncable {
    fun observeRumour(id: Int): Flow<Rumour?>

    /**
     * Rumours with [status] updated at or after [updatedSince], most recently updated first,
     * optionally only to or from a club of the league [topic] and only at [stage].
     */
    fun observeRumours(
        status: RumourStatus,
        updatedSince: Instant = Instant.DISTANT_PAST,
        topic: String? = null,
        stage: RumourStage? = null,
    ): Flow<List<Rumour>>

    /** The rumours with [ids], for example the saved ones, most recently updated first. */
    fun observeRumours(ids: Set<Int>): Flow<List<Rumour>>

    /** Rumours the article with [articleId] moved to a stage. */
    fun observeRumoursOfArticle(articleId: Int): Flow<List<Rumour>>

    fun observeRumoursOfClub(clubId: Int): Flow<List<Rumour>>

    fun observeRumoursOfPlayer(playerId: Int): Flow<List<Rumour>>
}
