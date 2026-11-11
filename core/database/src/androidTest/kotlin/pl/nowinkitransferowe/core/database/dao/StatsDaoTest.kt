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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import pl.nowinkitransferowe.core.database.model.ClubSeasonEntity
import pl.nowinkitransferowe.core.database.model.FetchTimeEntity
import pl.nowinkitransferowe.core.database.model.HotClubEntity
import pl.nowinkitransferowe.core.database.model.LeagueClubEntity
import pl.nowinkitransferowe.core.database.model.LeagueEntity
import pl.nowinkitransferowe.core.database.model.LeagueSeasonEntity
import pl.nowinkitransferowe.core.database.model.RumourStatsEntity
import pl.nowinkitransferowe.core.database.model.StageAccuracyEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.Season
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class StatsDaoTest : DatabaseTest() {

    @Test
    fun leagues_of_a_season_are_replaced_without_touching_other_seasons() = runTest {
        statsDao.replaceLeagues(listOf(LeagueSeasonEntity(2025, true)), 2025, listOf(league(2025, "premier-league")), listOf(leagueClub(2025, 86)))
        statsDao.replaceLeagues(
            listOf(LeagueSeasonEntity(2026, true), LeagueSeasonEntity(2025, false)),
            2026,
            listOf(league(2026, "premier-league", spent = 100.0)),
            listOf(leagueClub(2026, 86), leagueClub(2026, 79, position = 1)),
        )
        statsDao.replaceLeagues(
            listOf(LeagueSeasonEntity(2026, true), LeagueSeasonEntity(2025, false)),
            2026,
            listOf(league(2026, "premier-league", spent = 120.0)),
            listOf(leagueClub(2026, 79)),
        )

        val current = statsDao.observeLeagues(2026).first().single().asExternalModel()
        assertEquals(120.0, current.spent)
        assertEquals(listOf(79), current.clubs.map { it.clubId })
        assertEquals(listOf(86), statsDao.observeLeagues(2025).first().single().asExternalModel().clubs.map { it.clubId })
        assertEquals(listOf(2026, 2025), statsDao.observeLeagueSeasons().first().map { it.season })
    }

    @Test
    fun club_seasons_are_replaced_and_go_away_with_the_club() = runTest {
        clubDao.insertOrIgnoreClubs(listOf(clubStub(86, "Liverpool FC")))
        statsDao.replaceClubSeasons(86, listOf(clubSeason(2025), clubSeason(2026)))

        assertEquals(listOf(Season(2026), Season(2025)), statsDao.observeClubSeasons(86).first().map { it.asExternalModel().season })
        assertEquals(listOf(20692, 20691), statsDao.observeClubSeasons(86).first().first().arrivals)

        clubDao.deleteClubs(listOf(86))
        assertEquals(emptyList(), statsDao.observeClubSeasons(86).first())
    }

    @Test
    fun the_newest_rumour_stats_come_with_accuracy_and_hot_clubs() = runTest {
        statsDao.replaceRumourStats(RumourStatsEntity(2025, 0, 30, 10, true, 12.0), emptyList(), emptyList())
        statsDao.replaceRumourStats(
            RumourStatsEntity(2026, 14, 23, 9, true, 11.5),
            listOf(StageAccuracyEntity(2026, 4, 7, 10), StageAccuracyEntity(2026, 5, 9, 10)),
            listOf(HotClubEntity(2026, 3410, 6, 0), HotClubEntity(2026, 86, 2, 1)),
        )

        val stats = statsDao.observeLatestRumourStats().first()!!.asExternalModel()
        assertEquals(Season(2026), stats.season)
        assertEquals(listOf(5, 4), stats.byStage.map { it.stage.level })
        assertEquals(listOf(3410, 86), stats.hotClubs.map { it.clubId })
    }

    @Test
    fun fetch_times_are_kept_per_key() = runTest {
        assertNull(statsDao.getFetchTime("leagues/26-27"))

        statsDao.upsertFetchTime(FetchTimeEntity("leagues/26-27", Instant.parse("2026-11-11T10:00:00Z")))
        statsDao.upsertFetchTime(FetchTimeEntity("leagues/26-27", Instant.parse("2026-11-11T10:05:00Z")))

        assertEquals(Instant.parse("2026-11-11T10:05:00Z"), statsDao.getFetchTime("leagues/26-27")!!.fetchedAt)
    }

    private fun league(season: Int, topic: String, spent: Double = 10.0) = LeagueEntity(
        season = season,
        topic = topic,
        spent = spent,
        earned = 5.0,
        balance = 5.0 - spent,
        transfers = 3,
        position = 0,
        topArrivals = listOf(20567, 20412),
        topDepartures = listOf(20590),
    )

    private fun leagueClub(season: Int, clubId: Int, position: Int = 0) = LeagueClubEntity(season, "premier-league", clubId, 1.0, 2.0, 1.0, 1, 1, position)

    private fun clubSeason(season: Int) = ClubSeasonEntity(86, season, 275.0, 0.0, -275.0, listOf(20692, 20691), emptyList())
}
