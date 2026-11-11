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

package pl.nowinkitransferowe.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.ClubSeason
import pl.nowinkitransferowe.core.model.HotClub
import pl.nowinkitransferowe.core.model.League
import pl.nowinkitransferowe.core.model.LeagueClub
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStats
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.StageAccuracy

// Numbers the server counts (leagues, club seasons, rumour stats) have no change list. The last
// response is kept here so the screens work offline; seasons are stored as their start year.

/** A season the leagues can be shown for, as the last response listed them. */
@Entity(tableName = "league_seasons")
data class LeagueSeasonEntity(
    @PrimaryKey
    val season: Int,
    @ColumnInfo(name = "is_current")
    val isCurrent: Boolean,
)

@Entity(tableName = "leagues", primaryKeys = ["season", "topic"])
data class LeagueEntity(
    val season: Int,
    val topic: String,
    val spent: Double,
    val earned: Double,
    val balance: Double,
    val transfers: Int,
    val position: Int,
    @ColumnInfo(name = "top_arrivals")
    val topArrivals: List<Int>,
    @ColumnInfo(name = "top_departures")
    val topDepartures: List<Int>,
)

@Entity(
    tableName = "league_clubs",
    primaryKeys = ["season", "topic", "club_id"],
    foreignKeys = [
        ForeignKey(
            entity = LeagueEntity::class,
            parentColumns = ["season", "topic"],
            childColumns = ["season", "topic"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LeagueClubEntity(
    val season: Int,
    val topic: String,
    @ColumnInfo(name = "club_id")
    val clubId: Int,
    val spent: Double,
    val earned: Double,
    val balance: Double,
    val arrivals: Int,
    val departures: Int,
    val position: Int,
)

data class PopulatedLeague(
    @Embedded
    val league: LeagueEntity,
    @Relation(parentColumn = "topic", entityColumn = "topic")
    val clubs: List<LeagueClubEntity>,
)

fun PopulatedLeague.asExternalModel() = League(
    topic = league.topic,
    spent = league.spent,
    earned = league.earned,
    balance = league.balance,
    transfers = league.transfers,
    clubs = clubs.filter { it.season == league.season }.sortedBy(LeagueClubEntity::position).map {
        LeagueClub(
            clubId = it.clubId,
            spent = it.spent,
            earned = it.earned,
            balance = it.balance,
            arrivals = it.arrivals,
            departures = it.departures,
        )
    },
    topArrivals = league.topArrivals,
    topDepartures = league.topDepartures,
)

@Entity(
    tableName = "club_seasons",
    primaryKeys = ["club_id", "season"],
    foreignKeys = [
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["club_id"], onDelete = ForeignKey.CASCADE),
    ],
)
data class ClubSeasonEntity(
    @ColumnInfo(name = "club_id")
    val clubId: Int,
    val season: Int,
    val spent: Double,
    val earned: Double,
    val balance: Double,
    val arrivals: List<Int>,
    val departures: List<Int>,
)

fun ClubSeasonEntity.asExternalModel() = ClubSeason(
    season = Season(season),
    spent = spent,
    earned = earned,
    balance = balance,
    arrivals = arrivals,
    departures = departures,
)

@Entity(tableName = "rumour_stats")
data class RumourStatsEntity(
    @PrimaryKey
    val season: Int,
    val active: Int,
    val confirmed: Int,
    val failed: Int,
    @ColumnInfo(name = "show_accuracy")
    val showAccuracy: Boolean,
    @ColumnInfo(name = "days_to_confirm")
    val daysToConfirm: Double?,
)

@Entity(
    tableName = "rumour_stage_accuracy",
    primaryKeys = ["season", "stage"],
    foreignKeys = [
        ForeignKey(entity = RumourStatsEntity::class, parentColumns = ["season"], childColumns = ["season"], onDelete = ForeignKey.CASCADE),
    ],
)
data class StageAccuracyEntity(
    val season: Int,
    val stage: Int,
    val confirmed: Int,
    val closed: Int,
)

@Entity(
    tableName = "rumour_hot_clubs",
    primaryKeys = ["season", "club_id"],
    foreignKeys = [
        ForeignKey(entity = RumourStatsEntity::class, parentColumns = ["season"], childColumns = ["season"], onDelete = ForeignKey.CASCADE),
    ],
)
data class HotClubEntity(
    val season: Int,
    @ColumnInfo(name = "club_id")
    val clubId: Int,
    val rumours: Int,
    val position: Int,
)

data class PopulatedRumourStats(
    @Embedded
    val stats: RumourStatsEntity,
    @Relation(parentColumn = "season", entityColumn = "season")
    val byStage: List<StageAccuracyEntity>,
    @Relation(parentColumn = "season", entityColumn = "season")
    val hotClubs: List<HotClubEntity>,
)

fun PopulatedRumourStats.asExternalModel() = RumourStats(
    season = Season(stats.season),
    active = stats.active,
    confirmed = stats.confirmed,
    failed = stats.failed,
    showAccuracy = stats.showAccuracy,
    byStage = byStage.sortedByDescending(StageAccuracyEntity::stage).map {
        StageAccuracy(stage = RumourStage.fromLevel(it.stage), confirmed = it.confirmed, closed = it.closed)
    },
    hotClubs = hotClubs.sortedBy(HotClubEntity::position).map { HotClub(clubId = it.clubId, rumours = it.rumours) },
    daysToConfirm = stats.daysToConfirm,
)

/**
 * When data without a change list (leagues of a season, a club's seasons, rumour stats) was last
 * fetched, keyed for example "leagues/26-27" or "club_seasons/86".
 */
@Entity(tableName = "fetch_times", indices = [Index(value = ["fetched_at"])])
data class FetchTimeEntity(
    @PrimaryKey
    val key: String,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Instant,
)
