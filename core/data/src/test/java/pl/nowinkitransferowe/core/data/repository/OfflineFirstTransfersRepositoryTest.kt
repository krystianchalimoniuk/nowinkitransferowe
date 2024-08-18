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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.model.asEntity
import pl.nowinkitransferowe.core.data.testdoubles.CollectionType
import pl.nowinkitransferowe.core.data.testdoubles.TestDatabaseUpdatingMonitor
import pl.nowinkitransferowe.core.data.testdoubles.TestNtNetworkDataSource
import pl.nowinkitransferowe.core.data.testdoubles.TestTransferResourceDao
import pl.nowinkitransferowe.core.database.model.TransferResourceEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.datastoretest.testUserPreferencesDataStore
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.network.model.NetworkChangeList
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource
import pl.nowinkitransferowe.core.testing.notifications.TestNotifier
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineFirstTransfersRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: OfflineFirstTransferRepository

    private lateinit var ntPreferencesDataSource: NtPreferencesDataSource

    private lateinit var transferResourceDao: TestTransferResourceDao

    private lateinit var network: TestNtNetworkDataSource

    private lateinit var notifier: TestNotifier

    private lateinit var synchronizer: Synchronizer

    private lateinit var databaseUpdatingMonitor: TestDatabaseUpdatingMonitor

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        ntPreferencesDataSource = NtPreferencesDataSource(
            tmpFolder.testUserPreferencesDataStore(TestScope(UnconfinedTestDispatcher())),
        )
        transferResourceDao = TestTransferResourceDao()
        network = TestNtNetworkDataSource()
        notifier = TestNotifier()
        synchronizer = TestSynchronizer(
            ntPreferencesDataSource,
        )
        databaseUpdatingMonitor = TestDatabaseUpdatingMonitor()

        subject = OfflineFirstTransferRepository(
            ntPreferencesDataSource = ntPreferencesDataSource,
            transferResourceDao = transferResourceDao,
            network = network,
            notifier = notifier,
            databaseUpdatingMonitor = databaseUpdatingMonitor,
        )
    }

    @Test
    fun offlineFirstTransferRepository_transfer_resources_stream_is_backed_by_transfer_resource_dao() =
        testScope.runTest {
            assertEquals(
                transferResourceDao.getTransferResources()
                    .first()
                    .map(TransferResourceEntity::asExternalModel),
                subject.getTransferResources()
                    .first(),
            )
        }

    @Test
    fun offlineFirstTransfersRepository_sync_pulls_from_network() =
        testScope.runTest {
            ntPreferencesDataSource.setTransfersNotificationsAllowed(true)

            // User has not onboarded
            subject.syncWith(synchronizer)
            val transferResourcesFromNetwork = network.getTransferResources()
                .map(NetworkTransferResource::asEntity)
                .map(TransferResourceEntity::asExternalModel)

            val transferResourcesFromDb = transferResourceDao.getTransferResources()
                .first()
                .map(TransferResourceEntity::asExternalModel)

            assertEquals(
                transferResourcesFromNetwork.map(TransferResource::id).sorted(),
                transferResourcesFromDb.map(TransferResource::id).sorted(),
            )

            // After sync version should be updated
            assertEquals(
                expected = network.latestChangeListVersion(CollectionType.TransferResources),
                actual = synchronizer.getChangeListVersions().transferResourceVersion,
            )

            // Notifier should not have been called
            assertTrue(notifier.addedTransferResources.isEmpty())
        }

    @Test
    fun offlineFirstTransferRepository_sync_deletes_items_marked_deleted_on_network() =
        testScope.runTest {
            val transferResourcesFromNetwork = network.getTransferResources()
                .map(NetworkTransferResource::asEntity)
                .map(TransferResourceEntity::asExternalModel)

            // Delete half of the items on the network
            val deletedItems = transferResourcesFromNetwork
                .map(TransferResource::id)
                .partition { it.chars().sum() % 2 == 0 }
                .first
                .toSet()

            deletedItems.forEach {
                network.editCollection(
                    collectionType = CollectionType.TransferResources,
                    id = it,
                    isDelete = true,
                )
            }

            subject.syncWith(synchronizer)

            val transferResourcesFromDb = transferResourceDao.getTransferResources()
                .first()
                .map(TransferResourceEntity::asExternalModel)

            // Assert that items marked deleted on the network have been deleted locally
            assertEquals(
                expected = (transferResourcesFromNetwork.map(TransferResource::id) - deletedItems).sorted(),
                actual = transferResourcesFromDb.map(TransferResource::id).sorted(),
            )

            // After sync version should be updated
            assertEquals(
                expected = network.latestChangeListVersion(CollectionType.TransferResources),
                actual = synchronizer.getChangeListVersions().transferResourceVersion,
            )

            // Notifier should not have been called
            assertTrue(notifier.addedTransferResources.isEmpty())
        }

    @Test
    fun offlineFirstTransferRepository_incremental_sync_pulls_from_network() =
        testScope.runTest {
            // Set transfer version to 7
            synchronizer.updateChangeListVersions {
                copy(transferResourceVersion = 3)
            }

            subject.syncWith(synchronizer)

            val changeList = network.changeListsAfter(
                CollectionType.TransferResources,
                version = 3,
            )
            val changeListIds = changeList
                .map(NetworkChangeList::id)
                .toSet()

            val transferResourcesFromNetwork = network.getTransferResources()
                .map(NetworkTransferResource::asEntity)
                .map(TransferResourceEntity::asExternalModel)
                .filter { it.id in changeListIds }

            val transferResourcesFromDb = transferResourceDao.getTransferResources()
                .first()
                .map(TransferResourceEntity::asExternalModel)

            assertEquals(
                expected = transferResourcesFromNetwork.map(TransferResource::id).sorted(),
                actual = transferResourcesFromDb.map(TransferResource::id).sorted(),
            )

            // After sync version should be updated
            assertEquals(
                expected = changeList.first().changeListVersion,
                actual = synchronizer.getChangeListVersions().transferResourceVersion,
            )

            // Notifier should not have been called
            assertTrue(notifier.addedTransferResources.isEmpty())
        }

    @Test
    fun offlineFirstTransferRepository_sync_marks_as_read_on_first_run() =
        testScope.runTest {
            subject.syncWith(synchronizer)
            assertEquals(
                network.getTransferResources().map { it.id.toString() }.toSet(),
                ntPreferencesDataSource.userData.first().viewedTransferResources,
            )
        }

    @Test
    fun offlineFirstTransferRepository_sync_does_not_mark_as_read_on_subsequent_run() =
        testScope.runTest {
            // Pretend that we already have up to change list 7
            synchronizer.updateChangeListVersions {
                copy(transferResourceVersion = 7)
            }

            subject.syncWith(synchronizer)

            assertEquals(
                emptySet(),
                ntPreferencesDataSource.userData.first().viewedTransferResources,
            )
        }

    @Test
    fun offlineFirstTransferRepository_sends_notifications_for_newly_synced_transfer() =
        testScope.runTest {
            ntPreferencesDataSource.setTransfersNotificationsAllowed(true)
            // Pretend that we already have up to change list 7
            synchronizer.updateChangeListVersions {
                copy(transferResourceVersion = 7)
            }
            val networkTransferResources = network.getTransferResources()

            subject.syncWith(synchronizer)

            val followedTransferResourceIdsFromNetwork = networkTransferResources
                .map { it.id.toString() }
                .sorted()

            // Notifier should have been called with only transfer resources that have topics
            // that the user follows
            assertEquals(
                expected = followedTransferResourceIdsFromNetwork,
                actual = notifier.addedTransferResources.first().map(TransferResource::id).sorted(),
            )
        }

    @Test
    fun offlineFirstTransferRepository_does_not_send_notifications_for_existing_transfer_resources() =
        testScope.runTest {
            // Pretend that we already have up to change list 7
            synchronizer.updateChangeListVersions {
                copy(transferResourceVersion = 7)
            }

            val networkTransferResources = network.getTransferResources()
                .map(NetworkTransferResource::asEntity)

            // Prepopulate dao with transfer resources
            transferResourceDao.upsertTransferResources(networkTransferResources)

            subject.syncWith(synchronizer)

            // Notifier should not have been called bc all transfer resources existed previously
            assertTrue(notifier.addedTransferResources.isEmpty())
        }
}
