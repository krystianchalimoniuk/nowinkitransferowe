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

package pl.nowinkitransferowe.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.database.model.ClubEntity

/**
 * Clubs. Full records go through [upsertClubs]; stubs through [insertOrIgnoreClubs], so a stub never
 * overwrites a synced club. Never use REPLACE here: it deletes the row first, and the foreign keys
 * would then drop or clear everything that refers to the club.
 */
@Dao
interface ClubDao {
    @Upsert
    suspend fun upsertClubs(clubs: List<ClubEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreClubs(clubs: List<ClubEntity>)

    @Query("DELETE FROM clubs WHERE id IN (:ids)")
    suspend fun deleteClubs(ids: List<Int>)

    @Query("SELECT * FROM clubs WHERE id = :id")
    fun observeClub(id: Int): Flow<ClubEntity?>

    @Query("SELECT * FROM clubs WHERE slug = :slug")
    suspend fun getClubBySlug(slug: String): ClubEntity?

    /** Synced clubs with the given ids; stubs are left out. */
    @Query("SELECT * FROM clubs WHERE id IN (:ids) AND slug IS NOT NULL ORDER BY name")
    fun observeClubs(ids: Set<Int>): Flow<List<ClubEntity>>

    @Query("SELECT id FROM clubs WHERE id IN (:ids) AND slug IS NOT NULL")
    suspend fun getSyncedIds(ids: Set<Int>): List<Int>
}
