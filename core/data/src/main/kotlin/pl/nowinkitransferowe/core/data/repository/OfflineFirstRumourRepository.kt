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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.model.clubStub
import pl.nowinkitransferowe.core.data.model.playerStub
import pl.nowinkitransferowe.core.data.model.stageEntities
import pl.nowinkitransferowe.core.data.syncChanges
import pl.nowinkitransferowe.core.database.dao.ArticleDao
import pl.nowinkitransferowe.core.database.dao.ClubDao
import pl.nowinkitransferowe.core.database.dao.PlayerDao
import pl.nowinkitransferowe.core.database.dao.RumourDao
import pl.nowinkitransferowe.core.database.dao.TransferDao
import pl.nowinkitransferowe.core.database.model.PopulatedRumour
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import javax.inject.Inject

internal class OfflineFirstRumourRepository @Inject constructor(
    private val rumourDao: RumourDao,
    private val transferDao: TransferDao,
    private val articleDao: ArticleDao,
    private val clubDao: ClubDao,
    private val playerDao: PlayerDao,
    private val network: NtNetworkDataSource,
) : RumourRepository {

    override fun observeRumour(id: Int): Flow<Rumour?> = rumourDao.observeRumour(id).map { it?.asExternalModel() }

    override fun observeRumours(
        status: RumourStatus,
        updatedSince: Instant,
        topic: String?,
        stage: RumourStage?,
    ): Flow<List<Rumour>> = rumourDao.observeRumours(status.name, updatedSince, topic, stage?.level).map { it.map(PopulatedRumour::asExternalModel) }

    override fun observeRumours(ids: Set<Int>): Flow<List<Rumour>> = rumourDao.observeRumours(ids).map { it.map(PopulatedRumour::asExternalModel) }

    override fun observeRumoursOfArticle(articleId: Int): Flow<List<Rumour>> = rumourDao.observeRumoursOfArticle(articleId).map { it.map(PopulatedRumour::asExternalModel) }

    override fun observeRumoursOfClub(clubId: Int): Flow<List<Rumour>> = rumourDao.observeRumoursOfClub(clubId).map { it.map(PopulatedRumour::asExternalModel) }

    override fun observeRumoursOfPlayer(playerId: Int): Flow<List<Rumour>> = rumourDao.observeRumoursOfPlayer(playerId).map { it.map(PopulatedRumour::asExternalModel) }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = synchronizer.syncChanges(
        versionReader = ChangeListVersions::rumourVersion,
        changesFetcher = { after -> network.getChanges(NetworkRecordType.RUMOURS, after) },
        versionUpdater = { copy(rumourVersion = it) },
        modelDeleter = rumourDao::deleteRumours,
        modelUpdater = { ids ->
            val rumours = network.getRumours(ids)
            playerDao.insertOrIgnorePlayers(rumours.map { playerStub(it.playerId, it.playerName) })
            clubDao.insertOrIgnoreClubs(
                rumours.flatMap { rumour ->
                    listOfNotNull(
                        rumour.fromClubId?.let { clubStub(it, rumour.fromClubName) },
                        rumour.toClubId?.let { clubStub(it, rumour.toClubName) },
                    )
                },
            )
            val transferIds = transferDao.getExistingIds(rumours.mapNotNullTo(mutableSetOf()) { it.transferId }).toSet()
            val articleIds = articleDao.getExistingIds(
                rumours.flatMapTo(mutableSetOf()) { rumour ->
                    listOfNotNull(rumour.latestArticleId) + rumour.history.mapNotNull { it.articleId }
                },
            ).toSet()
            val saved = rumours.mapNotNull { rumour -> rumour.asEntity(transferIds, articleIds)?.let { rumour to it } }
            rumourDao.upsertRumours(
                rumours = saved.map { it.second },
                stages = saved.flatMap { it.first.stageEntities(articleIds) },
            )
        },
    )
}
