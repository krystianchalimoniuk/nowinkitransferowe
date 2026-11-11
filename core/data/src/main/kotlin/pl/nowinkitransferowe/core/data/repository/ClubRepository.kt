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
import pl.nowinkitransferowe.core.model.Club

/**
 * API v3 clubs, saved offline. A club referred to by another record before its own sync is a
 * stub ([Club.isStub]) with only a name.
 */
interface ClubRepository : Syncable {
    fun observeClub(id: Int): Flow<Club?>

    /**
     * Synced clubs with the given ids, by name. Stubs are left out.
     */
    fun observeClubs(ids: Set<Int>): Flow<List<Club>>

    suspend fun getClubBySlug(slug: String): Club?
}
