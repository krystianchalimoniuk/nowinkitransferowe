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
import pl.nowinkitransferowe.core.data.syncChanges
import pl.nowinkitransferowe.core.database.dao.PlayerDao
import pl.nowinkitransferowe.core.database.model.PlayerEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import javax.inject.Inject

internal class OfflineFirstPlayerRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val network: NtNetworkDataSource,
) : PlayerRepository {

    override fun observePlayer(id: Int): Flow<Player?> = playerDao.observePlayer(id).map { it?.asExternalModel() }

    override fun observePlayers(ids: Set<Int>): Flow<List<Player>> = playerDao.observePlayers(ids).map { it.map(PlayerEntity::asExternalModel) }

    override suspend fun getPlayerBySlug(slug: String): Player? = playerDao.getPlayerBySlug(slug)?.asExternalModel()

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = synchronizer.syncChanges(
        versionReader = ChangeListVersions::playerVersion,
        changesFetcher = { after -> network.getChanges(NetworkRecordType.PLAYERS, after) },
        versionUpdater = { copy(playerVersion = it) },
        modelDeleter = playerDao::deletePlayers,
        modelUpdater = { ids -> playerDao.upsertPlayers(network.getPlayers(ids).map { it.asEntity() }) },
    )
}
