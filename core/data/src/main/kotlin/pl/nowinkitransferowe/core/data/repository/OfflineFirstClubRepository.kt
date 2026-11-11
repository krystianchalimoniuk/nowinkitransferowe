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
import pl.nowinkitransferowe.core.database.dao.ClubDao
import pl.nowinkitransferowe.core.database.model.ClubEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import javax.inject.Inject

internal class OfflineFirstClubRepository @Inject constructor(
    private val clubDao: ClubDao,
    private val network: NtNetworkDataSource,
) : ClubRepository {

    override fun observeClub(id: Int): Flow<Club?> = clubDao.observeClub(id).map { it?.asExternalModel() }

    override fun observeClubs(ids: Set<Int>): Flow<List<Club>> = clubDao.observeClubs(ids).map { it.map(ClubEntity::asExternalModel) }

    override suspend fun getClubBySlug(slug: String): Club? = clubDao.getClubBySlug(slug)?.asExternalModel()

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = synchronizer.syncChanges(
        versionReader = ChangeListVersions::clubVersion,
        changesFetcher = { after -> network.getChanges(NetworkRecordType.CLUBS, after) },
        versionUpdater = { copy(clubVersion = it) },
        modelDeleter = clubDao::deleteClubs,
        modelUpdater = { ids -> clubDao.upsertClubs(network.getClubs(ids).map { it.asEntity() }) },
    )
}
