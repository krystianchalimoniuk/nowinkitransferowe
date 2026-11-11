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
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.database.model.PopulatedRumour
import pl.nowinkitransferowe.core.database.model.RumourEntity
import pl.nowinkitransferowe.core.database.model.RumourStageEntity

@Dao
interface RumourDao {
    /**
     * Saves [rumours] and replaces their stage history. The player, clubs, transfer and articles they
     * refer to must exist (player and clubs at least as stubs).
     */
    @Transaction
    suspend fun upsertRumours(rumours: List<RumourEntity>, stages: List<RumourStageEntity>) {
        upsertRumourEntities(rumours)
        deleteStages(rumours.map(RumourEntity::id))
        insertStages(stages)
    }

    @Upsert
    suspend fun upsertRumourEntities(rumours: List<RumourEntity>)

    @Insert
    suspend fun insertStages(stages: List<RumourStageEntity>)

    @Query("DELETE FROM rumour_stages WHERE rumour_id IN (:rumourIds)")
    suspend fun deleteStages(rumourIds: List<Int>)

    @Query("DELETE FROM rumours WHERE id IN (:ids)")
    suspend fun deleteRumours(ids: List<Int>)

    @Transaction
    @Query(
        """
        SELECT r.*,
            p.name AS current_player_name, p.photo_url AS player_photo_url,
            fc.name AS current_from_club_name, fc.crest_url AS from_club_crest_url,
            tc.name AS current_to_club_name, tc.crest_url AS to_club_crest_url
        FROM rumours r
        LEFT JOIN players p ON p.id = r.player_id
        LEFT JOIN clubs fc ON fc.id = r.from_club_id
        LEFT JOIN clubs tc ON tc.id = r.to_club_id
        WHERE r.id = :id
        """,
    )
    fun observeRumour(id: Int): Flow<PopulatedRumour?>

    /**
     * Rumours with [status] updated at or after [updatedSince], most recently updated first. Active
     * rumours of the last 45 days are the board shown on the website.
     */
    @Transaction
    @Query(
        """
        SELECT r.*,
            p.name AS current_player_name, p.photo_url AS player_photo_url,
            fc.name AS current_from_club_name, fc.crest_url AS from_club_crest_url,
            tc.name AS current_to_club_name, tc.crest_url AS to_club_crest_url
        FROM rumours r
        LEFT JOIN players p ON p.id = r.player_id
        LEFT JOIN clubs fc ON fc.id = r.from_club_id
        LEFT JOIN clubs tc ON tc.id = r.to_club_id
        WHERE r.status = :status AND r.updated_at >= :updatedSince
        ORDER BY r.updated_at DESC
        """,
    )
    fun observeRumours(status: String, updatedSince: Instant): Flow<List<PopulatedRumour>>

    @Transaction
    @Query(
        """
        SELECT r.*,
            p.name AS current_player_name, p.photo_url AS player_photo_url,
            fc.name AS current_from_club_name, fc.crest_url AS from_club_crest_url,
            tc.name AS current_to_club_name, tc.crest_url AS to_club_crest_url
        FROM rumours r
        LEFT JOIN players p ON p.id = r.player_id
        LEFT JOIN clubs fc ON fc.id = r.from_club_id
        LEFT JOIN clubs tc ON tc.id = r.to_club_id
        WHERE r.from_club_id = :clubId OR r.to_club_id = :clubId
        ORDER BY r.updated_at DESC
        """,
    )
    fun observeRumoursOfClub(clubId: Int): Flow<List<PopulatedRumour>>

    @Transaction
    @Query(
        """
        SELECT r.*,
            p.name AS current_player_name, p.photo_url AS player_photo_url,
            fc.name AS current_from_club_name, fc.crest_url AS from_club_crest_url,
            tc.name AS current_to_club_name, tc.crest_url AS to_club_crest_url
        FROM rumours r
        LEFT JOIN players p ON p.id = r.player_id
        LEFT JOIN clubs fc ON fc.id = r.from_club_id
        LEFT JOIN clubs tc ON tc.id = r.to_club_id
        WHERE r.player_id = :playerId
        ORDER BY r.updated_at DESC
        """,
    )
    fun observeRumoursOfPlayer(playerId: Int): Flow<List<PopulatedRumour>>
}
