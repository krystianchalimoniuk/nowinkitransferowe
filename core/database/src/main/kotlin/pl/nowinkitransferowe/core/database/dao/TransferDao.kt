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
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.database.model.PopulatedTransfer
import pl.nowinkitransferowe.core.database.model.TransferEntity

/**
 * Transfers, read with the current names, crests and photos of their clubs and players.
 */
@Dao
interface TransferDao {
    /** The clubs, player and article the transfers refer to must exist (clubs and players at least as stubs). */
    @Upsert
    suspend fun upsertTransfers(transfers: List<TransferEntity>)

    @Query("DELETE FROM transfers WHERE id IN (:ids)")
    suspend fun deleteTransfers(ids: List<Int>)

    @Query("SELECT COUNT(*) FROM transfers")
    fun observeTransferCount(): Flow<Int>

    @Query("SELECT id FROM transfers WHERE id IN (:ids)")
    suspend fun getExistingIds(ids: Set<Int>): List<Int>

    @Query(
        """
        SELECT t.*,
            p.name AS current_player_name, p.photo_url AS player_photo_url,
            fc.name AS current_from_club_name, fc.crest_url AS from_club_crest_url,
            tc.name AS current_to_club_name, tc.crest_url AS to_club_crest_url
        FROM transfers t
        LEFT JOIN players p ON p.id = t.player_id
        LEFT JOIN clubs fc ON fc.id = t.from_club_id
        LEFT JOIN clubs tc ON tc.id = t.to_club_id
        WHERE t.id = :id
        """,
    )
    fun observeTransfer(id: Int): Flow<PopulatedTransfer?>

    /** Newest transfers, optionally of one season (its start year). */
    @Query(
        """
        SELECT t.*,
            p.name AS current_player_name, p.photo_url AS player_photo_url,
            fc.name AS current_from_club_name, fc.crest_url AS from_club_crest_url,
            tc.name AS current_to_club_name, tc.crest_url AS to_club_crest_url
        FROM transfers t
        LEFT JOIN players p ON p.id = t.player_id
        LEFT JOIN clubs fc ON fc.id = t.from_club_id
        LEFT JOIN clubs tc ON tc.id = t.to_club_id
        WHERE (:season IS NULL OR t.season = :season)
        ORDER BY t.date DESC, t.id DESC
        LIMIT :limit
        """,
    )
    fun observeTransfers(season: Int? = null, limit: Int = Int.MAX_VALUE): Flow<List<PopulatedTransfer>>

    @Query(
        """
        SELECT t.*,
            p.name AS current_player_name, p.photo_url AS player_photo_url,
            fc.name AS current_from_club_name, fc.crest_url AS from_club_crest_url,
            tc.name AS current_to_club_name, tc.crest_url AS to_club_crest_url
        FROM transfers t
        LEFT JOIN players p ON p.id = t.player_id
        LEFT JOIN clubs fc ON fc.id = t.from_club_id
        LEFT JOIN clubs tc ON tc.id = t.to_club_id
        WHERE t.id IN (:ids)
        ORDER BY t.date DESC, t.id DESC
        """,
    )
    fun observeTransfersByIds(ids: Set<Int>): Flow<List<PopulatedTransfer>>

    @Query(
        """
        SELECT t.*,
            p.name AS current_player_name, p.photo_url AS player_photo_url,
            fc.name AS current_from_club_name, fc.crest_url AS from_club_crest_url,
            tc.name AS current_to_club_name, tc.crest_url AS to_club_crest_url
        FROM transfers t
        LEFT JOIN players p ON p.id = t.player_id
        LEFT JOIN clubs fc ON fc.id = t.from_club_id
        LEFT JOIN clubs tc ON tc.id = t.to_club_id
        WHERE t.from_club_id = :clubId OR t.to_club_id = :clubId
        ORDER BY t.date DESC, t.id DESC
        """,
    )
    fun observeTransfersOfClub(clubId: Int): Flow<List<PopulatedTransfer>>

    @Query(
        """
        SELECT t.*,
            p.name AS current_player_name, p.photo_url AS player_photo_url,
            fc.name AS current_from_club_name, fc.crest_url AS from_club_crest_url,
            tc.name AS current_to_club_name, tc.crest_url AS to_club_crest_url
        FROM transfers t
        LEFT JOIN players p ON p.id = t.player_id
        LEFT JOIN clubs fc ON fc.id = t.from_club_id
        LEFT JOIN clubs tc ON tc.id = t.to_club_id
        WHERE t.player_id = :playerId
        ORDER BY t.date DESC, t.id DESC
        """,
    )
    fun observeTransfersOfPlayer(playerId: Int): Flow<List<PopulatedTransfer>>
}
