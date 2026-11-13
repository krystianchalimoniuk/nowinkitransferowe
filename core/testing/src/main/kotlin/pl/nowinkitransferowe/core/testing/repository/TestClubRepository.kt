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
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.model.Club

class TestClubRepository : ClubRepository {

    private val clubs = MutableStateFlow(emptyList<Club>())

    override fun observeClub(id: Int): Flow<Club?> = clubs.map { list -> list.find { it.id == id } }

    override fun observeClubs(ids: Set<Int>): Flow<List<Club>> = clubs.map { list -> list.filter { it.id in ids && !it.isStub }.sortedBy { it.name } }

    override suspend fun getClubBySlug(slug: String): Club? = clubs.value.find { it.slug == slug }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /** A test-only API to allow controlling the clubs from tests. */
    fun sendClubs(clubs: List<Club>) {
        this.clubs.value = clubs
    }
}
