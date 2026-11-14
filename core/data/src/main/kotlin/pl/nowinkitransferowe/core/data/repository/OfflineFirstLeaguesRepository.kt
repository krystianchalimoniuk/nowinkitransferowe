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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import pl.nowinkitransferowe.core.database.dao.StatsDao
import pl.nowinkitransferowe.core.database.model.FetchTimeEntity
import pl.nowinkitransferowe.core.database.model.LeagueClubEntity
import pl.nowinkitransferowe.core.database.model.LeagueEntity
import pl.nowinkitransferowe.core.database.model.LeagueSeasonEntity
import pl.nowinkitransferowe.core.database.model.PopulatedLeague
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.League
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.SeasonOption
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

internal class OfflineFirstLeaguesRepository @Inject constructor(
    private val statsDao: StatsDao,
    private val network: NtNetworkDataSource,
) : LeaguesRepository {

    override fun observeSeasons(): Flow<List<SeasonOption>> = statsDao.observeLeagueSeasons().map { seasons ->
        seasons.map { SeasonOption(Season(it.season), it.isCurrent) }
    }

    override fun observeLeagues(season: Season): Flow<List<League>> = statsDao.observeLeagues(season.startYear).map { it.map(PopulatedLeague::asExternalModel) }

    override suspend fun refresh(season: Season?): Boolean {
        val now = Clock.System.now()
        val key = "leagues/${season?.slug ?: "current"}"
        val fetchedAt = statsDao.getFetchTime(key)?.fetchedAt
        if (fetchedAt != null && now - fetchedAt < FRESH_FOR) return true
        return try {
            val response = network.getLeagues(season?.slug)
            val start = Season.parse(response.season)?.startYear ?: return false
            statsDao.replaceLeagues(
                seasons = response.seasons.mapNotNull { option ->
                    Season.parse(option.label)?.let { LeagueSeasonEntity(it.startYear, option.current) }
                },
                season = start,
                leagues = response.leagues.mapIndexed { position, league ->
                    LeagueEntity(start, league.topic, league.spent, league.earned, league.balance, league.transfers, position, league.topArrivals, league.topDepartures)
                },
                clubs = response.leagues.flatMap { league ->
                    league.clubs.mapIndexed { position, club ->
                        LeagueClubEntity(start, league.topic, club.clubId, club.spent, club.earned, club.balance, club.arrivals, club.departures, position)
                    }
                },
            )
            statsDao.upsertFetchTime(FetchTimeEntity(key, now))
            true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            false
        }
    }

    private companion object {
        val FRESH_FOR = 1.hours
    }
}
