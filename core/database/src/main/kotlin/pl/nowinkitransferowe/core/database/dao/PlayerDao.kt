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
import pl.nowinkitransferowe.core.database.model.PlayerEntity

/**
 * Players, with the same rules as [ClubDao]: upsert for synced records, insert-or-ignore for stubs,
 * never REPLACE.
 */
@Dao
interface PlayerDao {
    @Upsert
    suspend fun upsertPlayers(players: List<PlayerEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnorePlayers(players: List<PlayerEntity>)

    @Query("DELETE FROM players WHERE id IN (:ids)")
    suspend fun deletePlayers(ids: List<Int>)

    @Query("SELECT * FROM players WHERE id = :id")
    fun observePlayer(id: Int): Flow<PlayerEntity?>

    @Query("SELECT * FROM players WHERE slug = :slug")
    suspend fun getPlayerBySlug(slug: String): PlayerEntity?

    /** Synced players with the given ids; stubs are left out. */
    @Query("SELECT * FROM players WHERE id IN (:ids) AND slug IS NOT NULL ORDER BY name")
    fun observePlayers(ids: Set<Int>): Flow<List<PlayerEntity>>
}
