/*
 * Copyright 2024 The Android Open Source Project
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.changeListSync
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.util.DatabaseUpdatingMonitor
import pl.nowinkitransferowe.core.database.dao.TransferResourceDao
import pl.nowinkitransferowe.core.database.model.TransferResourceEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.network.NtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource
import pl.nowinkitransferowe.core.notifications.Notifier
import javax.inject.Inject

private const val SYNC_BATCH_SIZE = 1000

internal class OfflineFirstTransferRepository @Inject constructor(
    private val ntPreferencesDataSource: NtPreferencesDataSource,
    private val transferResourceDao: TransferResourceDao,
    private val network: NtNetworkDataSource,
    private val notifier: Notifier,
    private val databaseUpdatingMonitor: DatabaseUpdatingMonitor,
) : TransferRepository {

    override fun getTransferResources(query: TransferResourceQuery): Flow<List<TransferResource>> =
        transferResourceDao.getTransferResources(
            useFilterTransferIds = query.filterTransferIds != null,
            filterTransferIds = query.filterTransferIds ?: emptySet(),
        )
            .map { it.map(TransferResourceEntity::asExternalModel) }

    override fun getTransferResourcesPages(limit: Int, offset: Int): Flow<List<TransferResource>> =
        transferResourceDao.getTransferResourcesPages(limit, offset)
            .map { it.map(TransferResourceEntity::asExternalModel) }

    override fun getTransferResource(id: String): Flow<TransferResource> =
        transferResourceDao.getTransferResource(id).map { it.asExternalModel() }

    override fun getTransferResourceByName(name: String): Flow<List<TransferResource>> =
        transferResourceDao.getTransferResourcesByName(name)
            .map { it.map(TransferResourceEntity::asExternalModel) }


    override fun getCount(): Flow<Int> = transferResourceDao.getCount()

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        var isFirstSync = false
        return synchronizer.changeListSync(
            versionReader = ChangeListVersions::transferResourceVersion,
            changeListFetcher = { currentVersion ->
                isFirstSync = currentVersion <= 0
                network.getTransferResourceChangeList(after = currentVersion)
            },
            versionUpdater = { latestVersion ->
                copy(transferResourceVersion = latestVersion)
            },
            modelDeleter = { toDelete ->
                transferResourceDao.deleteTransferResources(toDelete)
                if (toDelete.isNotEmpty()) databaseUpdatingMonitor.notifyTransfersDataChanged()
            },
            modelUpdater = { changedIds ->

                val isTransferNotificationAllowed =
                    ntPreferencesDataSource.userData.first().isTransfersNotificationsAllowed

                val existingTransferResourceIdsThatHaveChanged = when {
                    isFirstSync -> emptySet()
                    else -> transferResourceDao.getTransferResourceIds(
                        useFilterTransferIds = true,
                        filterTransferIds = changedIds.toSet(),
                    )
                        .first()
                        .toSet()
                }

                if (isFirstSync) {
                    // When we first retrieve news, mark everything viewed, so that we aren't
                    // overwhelmed with all historical news.
                    ntPreferencesDataSource.setTransferResourcesViewed(changedIds, true)
                }

                // Obtain the news resources which have changed from the network and upsert them locally
                changedIds.chunked(SYNC_BATCH_SIZE).forEach { chunkedIds ->
                    val networkTransferResources =
                        network.getTransferResources(ids = chunkedIds.joinToString(","))

                    transferResourceDao.upsertTransferResources(
                        transferResourceEntities = networkTransferResources.map(
                            NetworkTransferResource::asEntity,
                        ),
                    )
                }
                if (changedIds.isNotEmpty()) databaseUpdatingMonitor.notifyTransfersDataChanged()

                if (!isFirstSync) {
                    val addedTransferResources = transferResourceDao.getTransferResources(
                        useFilterTransferIds = true,
                        filterTransferIds = changedIds.toSet() - existingTransferResourceIdsThatHaveChanged,
                    )
                        .first()
                        .map(TransferResourceEntity::asExternalModel)

                    if (addedTransferResources.isNotEmpty() && isTransferNotificationAllowed) {
                        notifier.postTransferNotifications(
                            transferResource = addedTransferResources,
                        )
                    }
                }
            },
        )
    }
}
