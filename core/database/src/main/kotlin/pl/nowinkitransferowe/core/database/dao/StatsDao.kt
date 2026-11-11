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
import pl.nowinkitransferowe.core.database.model.ClubSeasonEntity
import pl.nowinkitransferowe.core.database.model.FetchTimeEntity
import pl.nowinkitransferowe.core.database.model.HotClubEntity
import pl.nowinkitransferowe.core.database.model.LeagueClubEntity
import pl.nowinkitransferowe.core.database.model.LeagueEntity
import pl.nowinkitransferowe.core.database.model.LeagueSeasonEntity
import pl.nowinkitransferowe.core.database.model.PopulatedLeague
import pl.nowinkitransferowe.core.database.model.PopulatedRumourStats
import pl.nowinkitransferowe.core.database.model.RumourStatsEntity
import pl.nowinkitransferowe.core.database.model.StageAccuracyEntity

/**
 * Numbers the server counts: the last response of each kind replaces the stored one.
 */
@Dao
interface StatsDao {
    /** Replaces the seasons that can be shown and the leagues of [season] (its start year). */
    @Transaction
    suspend fun replaceLeagues(
        seasons: List<LeagueSeasonEntity>,
        season: Int,
        leagues: List<LeagueEntity>,
        clubs: List<LeagueClubEntity>,
    ) {
        deleteLeagueSeasons()
        insertLeagueSeasons(seasons)
        deleteLeagues(season)
        insertLeagues(leagues)
        insertLeagueClubs(clubs)
    }

    @Insert
    suspend fun insertLeagueSeasons(seasons: List<LeagueSeasonEntity>)

    @Query("DELETE FROM league_seasons")
    suspend fun deleteLeagueSeasons()

    @Insert
    suspend fun insertLeagues(leagues: List<LeagueEntity>)

    @Insert
    suspend fun insertLeagueClubs(clubs: List<LeagueClubEntity>)

    /** Also deletes their clubs (foreign key cascade). */
    @Query("DELETE FROM leagues WHERE season = :season")
    suspend fun deleteLeagues(season: Int)

    @Query("SELECT * FROM league_seasons ORDER BY season DESC")
    fun observeLeagueSeasons(): Flow<List<LeagueSeasonEntity>>

    @Transaction
    @Query("SELECT * FROM leagues WHERE season = :season ORDER BY position")
    fun observeLeagues(season: Int): Flow<List<PopulatedLeague>>

    /** Replaces the seasons of [clubId]. The club must exist, at least as a stub. */
    @Transaction
    suspend fun replaceClubSeasons(clubId: Int, seasons: List<ClubSeasonEntity>) {
        deleteClubSeasons(clubId)
        insertClubSeasons(seasons)
    }

    @Insert
    suspend fun insertClubSeasons(seasons: List<ClubSeasonEntity>)

    @Query("DELETE FROM club_seasons WHERE club_id = :clubId")
    suspend fun deleteClubSeasons(clubId: Int)

    @Query("SELECT * FROM club_seasons WHERE club_id = :clubId ORDER BY season DESC")
    fun observeClubSeasons(clubId: Int): Flow<List<ClubSeasonEntity>>

    @Transaction
    suspend fun replaceRumourStats(
        stats: RumourStatsEntity,
        byStage: List<StageAccuracyEntity>,
        hotClubs: List<HotClubEntity>,
    ) {
        deleteRumourStats(stats.season)
        insertRumourStats(stats)
        insertStageAccuracy(byStage)
        insertHotClubs(hotClubs)
    }

    @Insert
    suspend fun insertRumourStats(stats: RumourStatsEntity)

    @Insert
    suspend fun insertStageAccuracy(byStage: List<StageAccuracyEntity>)

    @Insert
    suspend fun insertHotClubs(hotClubs: List<HotClubEntity>)

    /** Also deletes its accuracy and hot clubs (foreign key cascade). */
    @Query("DELETE FROM rumour_stats WHERE season = :season")
    suspend fun deleteRumourStats(season: Int)

    /** Stats of the newest stored season. */
    @Transaction
    @Query("SELECT * FROM rumour_stats ORDER BY season DESC LIMIT 1")
    fun observeLatestRumourStats(): Flow<PopulatedRumourStats?>

    @Upsert
    suspend fun upsertFetchTime(fetchTime: FetchTimeEntity)

    @Query("SELECT * FROM fetch_times WHERE `key` = :key")
    suspend fun getFetchTime(key: String): FetchTimeEntity?
}
