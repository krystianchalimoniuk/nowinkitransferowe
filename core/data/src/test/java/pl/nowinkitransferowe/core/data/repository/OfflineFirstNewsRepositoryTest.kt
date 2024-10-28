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
import pl.nowinkitransferowe.core.data.testdoubles.TestNewsResourceDao
import pl.nowinkitransferowe.core.data.testdoubles.TestNtNetworkDataSource
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.database.model.asExternalModel
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.datastoreproto.UserPreferences
import pl.nowinkitransferowe.core.datastoretest.InMemoryDataStore
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.network.model.NetworkChangeList
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.testing.notifications.TestNotifier
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineFirstNewsRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: OfflineFirstNewsRepository

    private lateinit var ntPreferencesDataSource: NtPreferencesDataSource

    private lateinit var newsResourceDao: TestNewsResourceDao

    private lateinit var network: TestNtNetworkDataSource

    private lateinit var notifier: TestNotifier

    private lateinit var synchronizer: Synchronizer

    private lateinit var databaseUpdatingMonitor: TestDatabaseUpdatingMonitor

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        ntPreferencesDataSource = NtPreferencesDataSource(
            InMemoryDataStore(UserPreferences.getDefaultInstance()),
        )
        newsResourceDao = TestNewsResourceDao()
        network = TestNtNetworkDataSource()
        notifier = TestNotifier()
        synchronizer = TestSynchronizer(
            ntPreferencesDataSource,
        )
        databaseUpdatingMonitor = TestDatabaseUpdatingMonitor()

        subject = OfflineFirstNewsRepository(
            ntPreferencesDataSource = ntPreferencesDataSource,
            newsResourceDao = newsResourceDao,
            network = network,
            notifier = notifier,
            databaseUpdatingMonitor = databaseUpdatingMonitor,
        )
    }

    @Test
    fun offlineFirstNewsRepository_news_resources_stream_is_backed_by_news_resource_dao() =
        testScope.runTest {
            assertEquals(
                newsResourceDao.getNewsResources()
                    .first()
                    .map(NewsResourceEntity::asExternalModel),
                subject.getNewsResources()
                    .first(),
            )
        }

    @Test
    fun offlineFirstNewsRepository_sync_pulls_from_network() =
        testScope.runTest {
            ntPreferencesDataSource.setNewsNotificationsAllowed(true)

            // User has not onboarded
            subject.syncWith(synchronizer)
            val newsResourcesFromNetwork = network.getNewsResources()
                .map(NetworkNewsResource::asEntity)
                .map(NewsResourceEntity::asExternalModel)

            val newsResourcesFromDb = newsResourceDao.getNewsResources()
                .first()
                .map(NewsResourceEntity::asExternalModel)

            assertEquals(
                newsResourcesFromNetwork.map(NewsResource::id).sorted(),
                newsResourcesFromDb.map(NewsResource::id).sorted(),
            )

            // After sync version should be updated
            assertEquals(
                expected = network.latestChangeListVersion(CollectionType.NewsResources),
                actual = synchronizer.getChangeListVersions().newsResourceVersion,
            )

            // Notifier should not have been called
            assertTrue(notifier.addedNewsResources.isEmpty())
        }

    @Test
    fun offlineFirstNewsRepository_sync_deletes_items_marked_deleted_on_network() =
        testScope.runTest {
            val newsResourcesFromNetwork = network.getNewsResources()
                .map(NetworkNewsResource::asEntity)
                .map(NewsResourceEntity::asExternalModel)

            // Delete half of the items on the network
            val deletedItems = newsResourcesFromNetwork
                .map(NewsResource::id)
                .partition { it.chars().sum() % 2 == 0 }
                .first
                .toSet()

            deletedItems.forEach {
                network.editCollection(
                    collectionType = CollectionType.NewsResources,
                    id = it,
                    isDelete = true,
                )
            }

            subject.syncWith(synchronizer)

            val newsResourcesFromDb = newsResourceDao.getNewsResources()
                .first()
                .map(NewsResourceEntity::asExternalModel)

            // Assert that items marked deleted on the network have been deleted locally
            assertEquals(
                expected = (newsResourcesFromNetwork.map(NewsResource::id) - deletedItems).sorted(),
                actual = newsResourcesFromDb.map(NewsResource::id).sorted(),
            )

            // After sync version should be updated
            assertEquals(
                expected = network.latestChangeListVersion(CollectionType.NewsResources),
                actual = synchronizer.getChangeListVersions().newsResourceVersion,
            )

            // Notifier should not have been called
            assertTrue(notifier.addedNewsResources.isEmpty())
        }

    @Test
    fun offlineFirstNewsRepository_incremental_sync_pulls_from_network() =
        testScope.runTest {
            // Set news version to 7
            synchronizer.updateChangeListVersions {
                copy(newsResourceVersion = 3)
            }

            subject.syncWith(synchronizer)

            val changeList = network.changeListsAfter(
                CollectionType.NewsResources,
                version = 3,
            )
            val changeListIds = changeList
                .map(NetworkChangeList::id)
                .toSet()

            val newsResourcesFromNetwork = network.getNewsResources()
                .map(NetworkNewsResource::asEntity)
                .map(NewsResourceEntity::asExternalModel)
                .filter { it.id in changeListIds }

            val newsResourcesFromDb = newsResourceDao.getNewsResources()
                .first()
                .map(NewsResourceEntity::asExternalModel)

            assertEquals(
                expected = newsResourcesFromNetwork.map(NewsResource::id).sorted(),
                actual = newsResourcesFromDb.map(NewsResource::id).sorted(),
            )

            // After sync version should be updated
            assertEquals(
                expected = changeList.first().changeListVersion,
                actual = synchronizer.getChangeListVersions().newsResourceVersion,
            )

            // Notifier should not have been called
            assertTrue(notifier.addedNewsResources.isEmpty())
        }

    @Test
    fun offlineFirstNewsRepository_sync_marks_as_read_on_first_run() =
        testScope.runTest {
            subject.syncWith(synchronizer)
            assertEquals(
                network.getNewsResources().map { it.id.toString() }.toSet(),
                ntPreferencesDataSource.userData.first().viewedNewsResources,
            )
        }

    @Test
    fun offlineFirstNewsRepository_sync_does_not_mark_as_read_on_subsequent_run() =
        testScope.runTest {
            // Pretend that we already have up to change list 7
            synchronizer.updateChangeListVersions {
                copy(newsResourceVersion = 7)
            }

            subject.syncWith(synchronizer)

            assertEquals(
                emptySet(),
                ntPreferencesDataSource.userData.first().viewedNewsResources,
            )
        }

    @Test
    fun offlineFirstNewsRepository_sends_notifications_for_newly_synced_news() =
        testScope.runTest {
            ntPreferencesDataSource.setNewsNotificationsAllowed(true)
            // Pretend that we already have up to change list 7
            synchronizer.updateChangeListVersions {
                copy(newsResourceVersion = 7)
            }
            val networkNewsResources = network.getNewsResources()

            subject.syncWith(synchronizer)

            val followedNewsResourceIdsFromNetwork = networkNewsResources
                .map { it.id.toString() }
                .sorted()

            // Notifier should have been called with only news resources that have topics
            // that the user follows
            assertEquals(
                expected = followedNewsResourceIdsFromNetwork,
                actual = notifier.addedNewsResources.first().map(NewsResource::id).sorted(),
            )
        }

    @Test
    fun offlineFirstNewsRepository_does_not_send_notifications_for_existing_news_resources() =
        testScope.runTest {
            // Pretend that we already have up to change list 7
            synchronizer.updateChangeListVersions {
                copy(newsResourceVersion = 7)
            }

            val networkNewsResources = network.getNewsResources()
                .map(NetworkNewsResource::asEntity)

            // Prepopulate dao with news resources
            newsResourceDao.upsertNewsResources(networkNewsResources)

            subject.syncWith(synchronizer)

            // Notifier should not have been called bc all news resources existed previously
            assertTrue(notifier.addedNewsResources.isEmpty())
        }
}
