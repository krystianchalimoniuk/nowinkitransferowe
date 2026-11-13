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
import pl.nowinkitransferowe.core.database.model.HotClubEntity
import pl.nowinkitransferowe.core.database.model.RumourStatsEntity
import pl.nowinkitransferowe.core.database.model.StageAccuracyEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.model.RumourStats
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

internal class OfflineFirstRumourStatsRepository @Inject constructor(
    private val statsDao: StatsDao,
    private val network: NtNetworkDataSource,
) : RumourStatsRepository {

    override fun observeRumourStats(): Flow<RumourStats?> = statsDao.observeLatestRumourStats().map { it?.asExternalModel() }

    override suspend fun refresh(): Boolean {
        val now = Clock.System.now()
        val fetchedAt = statsDao.getFetchTime(FETCH_KEY)?.fetchedAt
        if (fetchedAt != null && now - fetchedAt < FRESH_FOR) return true
        return try {
            val stats = network.getRumourStats()
            val season = (Season.parse(stats.season) ?: Season.of(now)).startYear
            statsDao.replaceRumourStats(
                stats = RumourStatsEntity(
                    season = season,
                    active = stats.active,
                    confirmed = stats.confirmed,
                    failed = stats.failed,
                    showAccuracy = stats.showAccuracy,
                    daysToConfirm = stats.daysToConfirm,
                ),
                byStage = stats.byStage.map { StageAccuracyEntity(season, it.stage, it.confirmed, it.closed) },
                hotClubs = stats.hotClubs.mapIndexed { position, club -> HotClubEntity(season, club.clubId, club.rumours, position) },
            )
            statsDao.upsertFetchTime(FetchTimeEntity(FETCH_KEY, now))
            true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            false
        }
    }

    private companion object {
        const val FETCH_KEY = "rumour_stats"
        val FRESH_FOR = 1.hours
    }
}
