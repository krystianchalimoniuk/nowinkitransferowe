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
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.model.clubStub
import pl.nowinkitransferowe.core.data.model.playerStub
import pl.nowinkitransferowe.core.data.syncChanges
import pl.nowinkitransferowe.core.database.dao.ArticleDao
import pl.nowinkitransferowe.core.database.dao.ClubDao
import pl.nowinkitransferowe.core.database.dao.PlayerDao
import pl.nowinkitransferowe.core.database.dao.TransferDao
import pl.nowinkitransferowe.core.database.model.PopulatedTransfer
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import javax.inject.Inject

internal class OfflineFirstTransferRepository @Inject constructor(
    private val transferDao: TransferDao,
    private val articleDao: ArticleDao,
    private val clubDao: ClubDao,
    private val playerDao: PlayerDao,
    private val network: NtNetworkDataSource,
) : TransferRepository {

    override fun observeTransfer(id: Int): Flow<Transfer?> = transferDao.observeTransfer(id).map { it?.asExternalModel() }

    override fun observeTransfers(season: Season?, limit: Int): Flow<List<Transfer>> = transferDao.observeTransfers(season?.startYear, limit).map { it.map(PopulatedTransfer::asExternalModel) }

    override fun observeTransfers(ids: Set<Int>): Flow<List<Transfer>> = transferDao.observeTransfersByIds(ids).map { it.map(PopulatedTransfer::asExternalModel) }

    override fun observeTransfersOfClub(clubId: Int): Flow<List<Transfer>> = transferDao.observeTransfersOfClub(clubId).map { it.map(PopulatedTransfer::asExternalModel) }

    override fun observeTransfersOfPlayer(playerId: Int): Flow<List<Transfer>> = transferDao.observeTransfersOfPlayer(playerId).map { it.map(PopulatedTransfer::asExternalModel) }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = synchronizer.syncChanges(
        versionReader = ChangeListVersions::transferVersion,
        changesFetcher = { after -> network.getChanges(NetworkRecordType.TRANSFERS, after) },
        versionUpdater = { copy(transferVersion = it) },
        modelDeleter = transferDao::deleteTransfers,
        modelUpdater = { ids ->
            val transfers = network.getTransfers(ids)
            playerDao.insertOrIgnorePlayers(transfers.map { playerStub(it.playerId, it.playerName) })
            clubDao.insertOrIgnoreClubs(
                transfers.flatMap { transfer ->
                    listOfNotNull(
                        transfer.fromClubId?.let { clubStub(it, transfer.fromClubName) },
                        transfer.toClubId?.let { clubStub(it, transfer.toClubName) },
                    )
                },
            )
            val articleIds = articleDao.getExistingIds(transfers.mapNotNullTo(mutableSetOf()) { it.articleId }).toSet()
            transferDao.upsertTransfers(transfers.map { it.asEntity(articleIds) })
        },
    )
}
