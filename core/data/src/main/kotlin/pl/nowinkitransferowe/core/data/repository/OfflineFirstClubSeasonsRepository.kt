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
import pl.nowinkitransferowe.core.data.model.clubStub
import pl.nowinkitransferowe.core.database.dao.ClubDao
import pl.nowinkitransferowe.core.database.dao.StatsDao
import pl.nowinkitransferowe.core.database.model.ClubSeasonEntity
import pl.nowinkitransferowe.core.database.model.FetchTimeEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.ClubSeason
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

internal class OfflineFirstClubSeasonsRepository @Inject constructor(
    private val statsDao: StatsDao,
    private val clubDao: ClubDao,
    private val network: NtNetworkDataSource,
) : ClubSeasonsRepository {

    override fun observeClubSeasons(clubId: Int): Flow<List<ClubSeason>> = statsDao.observeClubSeasons(clubId).map { it.map(ClubSeasonEntity::asExternalModel) }

    override suspend fun refresh(clubId: Int): Boolean {
        val now = Clock.System.now()
        val key = "club_seasons/$clubId"
        val fetchedAt = statsDao.getFetchTime(key)?.fetchedAt
        if (fetchedAt != null && now - fetchedAt < FRESH_FOR) return true
        return try {
            val seasons = network.getClubSeasons(clubId).seasons.mapNotNull { season ->
                val start = Season.parse(season.season)?.startYear ?: return@mapNotNull null
                ClubSeasonEntity(clubId, start, season.spent, season.earned, season.balance, season.arrivals, season.departures)
            }
            // The seasons refer to the club; a club not synced yet gets a stub, as for transfers
            clubDao.insertOrIgnoreClubs(listOf(clubStub(clubId, null)))
            statsDao.replaceClubSeasons(clubId, seasons)
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
