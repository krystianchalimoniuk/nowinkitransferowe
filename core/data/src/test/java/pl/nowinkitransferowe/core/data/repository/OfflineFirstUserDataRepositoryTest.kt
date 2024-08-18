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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.nowinkitransferowe.core.analytics.NoOpAnalyticsHelper
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.datastoretest.testUserPreferencesDataStore
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserData
import kotlin.test.assertEquals

class OfflineFirstUserDataRepositoryTest {
    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: OfflineFirstUserDataRepository

    private lateinit var ntPreferencesDataSource: NtPreferencesDataSource

    private val analyticsHelper = NoOpAnalyticsHelper()

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        ntPreferencesDataSource = NtPreferencesDataSource(
            tmpFolder.testUserPreferencesDataStore(TestScope(UnconfinedTestDispatcher())),
        )

        subject = OfflineFirstUserDataRepository(
            ntPreferencesDataSource = ntPreferencesDataSource,
            analyticsHelper,
        )
    }

    @Test
    fun offlineFirstUserDataRepository_default_user_data_is_correct() =
        testScope.runTest {
            assertEquals(
                UserData(
                    bookmarkedNewsResources = emptySet(),
                    viewedNewsResources = emptySet(),
                    bookmarkedTransferResources = emptySet(),
                    viewedTransferResources = emptySet(),
                    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                    useDynamicColor = false,
                    isNewsNotificationsAllowed = false,
                    isTransfersNotificationsAllowed = false,
                    isGeneralNotificationAllowed = false,
                ),
                subject.userData.first(),
            )
        }

    @Test
    fun offlineFirstUserDataRepository_bookmark_news_resource_logic_delegates_to_nt_preferences() =
        testScope.runTest {
            subject.setNewsResourceBookmarked(newsResourceId = "0", bookmarked = true)

            assertEquals(
                setOf("0"),
                subject.userData
                    .map { it.bookmarkedNewsResources }
                    .first(),
            )

            subject.setNewsResourceBookmarked(newsResourceId = "1", bookmarked = true)

            assertEquals(
                setOf("0", "1"),
                subject.userData
                    .map { it.bookmarkedNewsResources }
                    .first(),
            )

            assertEquals(
                ntPreferencesDataSource.userData
                    .map { it.bookmarkedNewsResources }
                    .first(),
                subject.userData
                    .map { it.bookmarkedNewsResources }
                    .first(),
            )
        }

    @Test
    fun offlineFirstUserDataRepository_update_viewed_news_resources_delegates_to_nt_preferences() =
        runTest {
            subject.setNewsResourceViewed(newsResourceId = "0", viewed = true)

            assertEquals(
                setOf("0"),
                subject.userData
                    .map { it.viewedNewsResources }
                    .first(),
            )

            subject.setNewsResourceViewed(newsResourceId = "1", viewed = true)

            assertEquals(
                setOf("0", "1"),
                subject.userData
                    .map { it.viewedNewsResources }
                    .first(),
            )

            assertEquals(
                ntPreferencesDataSource.userData
                    .map { it.viewedNewsResources }
                    .first(),
                subject.userData
                    .map { it.viewedNewsResources }
                    .first(),
            )
        }

    @Test
    fun offlineFirstUserDataRepository_bookmark_transfer_resource_logic_delegates_to_nt_preferences() =
        testScope.runTest {
            subject.setTransferResourceBookmarked(transferResourceId = "0", bookmarked = true)

            assertEquals(
                setOf("0"),
                subject.userData
                    .map { it.bookmarkedTransferResources }
                    .first(),
            )

            subject.setTransferResourceBookmarked(transferResourceId = "1", bookmarked = true)

            assertEquals(
                setOf("0", "1"),
                subject.userData
                    .map { it.bookmarkedTransferResources }
                    .first(),
            )

            assertEquals(
                ntPreferencesDataSource.userData
                    .map { it.bookmarkedTransferResources }
                    .first(),
                subject.userData
                    .map { it.bookmarkedTransferResources }
                    .first(),
            )
        }

    @Test
    fun offlineFirstUserDataRepository_update_viewed_transfer_resources_delegates_to_nt_preferences() =
        runTest {
            subject.setTransferResourceViewed(transferResourceId = "0", viewed = true)

            assertEquals(
                setOf("0"),
                subject.userData
                    .map { it.viewedTransferResources }
                    .first(),
            )

            subject.setTransferResourceViewed(transferResourceId = "1", viewed = true)

            assertEquals(
                setOf("0", "1"),
                subject.userData
                    .map { it.viewedTransferResources }
                    .first(),
            )

            assertEquals(
                ntPreferencesDataSource.userData
                    .map { it.viewedTransferResources }
                    .first(),
                subject.userData
                    .map { it.viewedTransferResources }
                    .first(),
            )
        }

    @Test
    fun offlineFirstUserDataRepository_set_dynamic_color_delegates_to_nt_preferences() =
        testScope.runTest {
            subject.setDynamicColorPreference(true)

            assertEquals(
                true,
                subject.userData
                    .map { it.useDynamicColor }
                    .first(),
            )
            assertEquals(
                true,
                ntPreferencesDataSource
                    .userData
                    .map { it.useDynamicColor }
                    .first(),
            )
        }

    @Test
    fun offlineFirstUserDataRepository_set_dark_theme_config_delegates_to_nt_preferences() =
        testScope.runTest {
            subject.setDarkThemeConfig(DarkThemeConfig.DARK)

            assertEquals(
                DarkThemeConfig.DARK,
                subject.userData
                    .map { it.darkThemeConfig }
                    .first(),
            )
            assertEquals(
                DarkThemeConfig.DARK,
                ntPreferencesDataSource
                    .userData
                    .map { it.darkThemeConfig }
                    .first(),
            )
        }

    @Test
    fun offlineFirstUserDataRepository_set_news_notifications_allowed_delegates_to_nt_preferences() =
        testScope.runTest {
            subject.setNewsNotificationsAllowed(true)

            assertEquals(
                true,
                subject.userData
                    .map { it.isNewsNotificationsAllowed }
                    .first(),
            )
            assertEquals(
                true,
                ntPreferencesDataSource
                    .userData
                    .map { it.isNewsNotificationsAllowed }
                    .first(),
            )
        }

    @Test
    fun offlineFirstUserDataRepository_set_transfer_notifications_allowed_delegates_to_nt_preferences() =
        testScope.runTest {
            subject.setTransfersNotificationsAllowed(true)

            assertEquals(
                true,
                subject.userData
                    .map { it.isTransfersNotificationsAllowed }
                    .first(),
            )
            assertEquals(
                true,
                ntPreferencesDataSource
                    .userData
                    .map { it.isTransfersNotificationsAllowed }
                    .first(),
            )
        }

    @Test
    fun offlineFirstUserDataRepository_set_general_notifications_allowed_delegates_to_nt_preferences() =
        testScope.runTest {
            subject.setGeneralNotificationsAllowed(true)

            assertEquals(
                true,
                subject.userData
                    .map { it.isGeneralNotificationAllowed }
                    .first(),
            )
            assertEquals(
                true,
                ntPreferencesDataSource
                    .userData
                    .map { it.isGeneralNotificationAllowed }
                    .first(),
            )
        }
}
