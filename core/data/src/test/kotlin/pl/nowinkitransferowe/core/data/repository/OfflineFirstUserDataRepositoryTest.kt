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
import pl.nowinkitransferowe.core.analytics.AnalyticsEvent
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.datastoreproto.UserPreferences
import pl.nowinkitransferowe.core.datastoretest.InMemoryDataStore
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserData
import pl.nowinkitransferowe.core.testing.util.TestAnalyticsHelper
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineFirstUserDataRepositoryTest {
    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: OfflineFirstUserDataRepository

    private lateinit var ntPreferencesDataSource: NtPreferencesDataSource

    private val analyticsHelper = TestAnalyticsHelper()

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        ntPreferencesDataSource = NtPreferencesDataSource(
            InMemoryDataStore(UserPreferences.getDefaultInstance()),
        )

        subject = OfflineFirstUserDataRepository(
            ntPreferencesDataSource = ntPreferencesDataSource,
            analyticsHelper,
        )
    }

    @Test
    fun offlineFirstUserDataRepository_default_user_data_is_correct() = testScope.runTest {
        assertEquals(
            UserData(
                bookmarkedArticles = emptySet(),
                viewedArticles = emptySet(),
                bookmarkedTransfers = emptySet(),
                viewedTransfers = emptySet(),
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
    fun offlineFirstUserDataRepository_bookmark_article_logic_delegates_to_nt_preferences() = testScope.runTest {
        subject.setArticleBookmarked(articleId = 0, bookmarked = true)

        assertEquals(
            setOf(0),
            subject.userData
                .map { it.bookmarkedArticles }
                .first(),
        )

        subject.setArticleBookmarked(articleId = 1, bookmarked = true)

        assertEquals(
            setOf(0, 1),
            subject.userData
                .map { it.bookmarkedArticles }
                .first(),
        )

        assertEquals(
            ntPreferencesDataSource.userData
                .map { it.bookmarkedArticles }
                .first(),
            subject.userData
                .map { it.bookmarkedArticles }
                .first(),
        )
    }

    @Test
    fun offlineFirstUserDataRepository_update_viewed_articles_delegates_to_nt_preferences() = runTest {
        subject.setArticleViewed(articleId = 0, viewed = true)

        assertEquals(
            setOf(0),
            subject.userData
                .map { it.viewedArticles }
                .first(),
        )

        subject.setArticleViewed(articleId = 1, viewed = true)

        assertEquals(
            setOf(0, 1),
            subject.userData
                .map { it.viewedArticles }
                .first(),
        )

        assertEquals(
            ntPreferencesDataSource.userData
                .map { it.viewedArticles }
                .first(),
            subject.userData
                .map { it.viewedArticles }
                .first(),
        )
    }

    @Test
    fun offlineFirstUserDataRepository_bookmark_transfer_logic_delegates_to_nt_preferences() = testScope.runTest {
        subject.setTransferBookmarked(transferId = 0, bookmarked = true)

        assertEquals(
            setOf(0),
            subject.userData
                .map { it.bookmarkedTransfers }
                .first(),
        )

        subject.setTransferBookmarked(transferId = 1, bookmarked = true)

        assertEquals(
            setOf(0, 1),
            subject.userData
                .map { it.bookmarkedTransfers }
                .first(),
        )

        assertEquals(
            ntPreferencesDataSource.userData
                .map { it.bookmarkedTransfers }
                .first(),
            subject.userData
                .map { it.bookmarkedTransfers }
                .first(),
        )
    }

    @Test
    fun offlineFirstUserDataRepository_update_viewed_transfers_delegates_to_nt_preferences() = runTest {
        subject.setTransferViewed(transferId = 0, viewed = true)

        assertEquals(
            setOf(0),
            subject.userData
                .map { it.viewedTransfers }
                .first(),
        )

        subject.setTransferViewed(transferId = 1, viewed = true)

        assertEquals(
            setOf(0, 1),
            subject.userData
                .map { it.viewedTransfers }
                .first(),
        )

        assertEquals(
            ntPreferencesDataSource.userData
                .map { it.viewedTransfers }
                .first(),
            subject.userData
                .map { it.viewedTransfers }
                .first(),
        )
    }

    @Test
    fun offlineFirstUserDataRepository_set_dynamic_color_delegates_to_nt_preferences() = testScope.runTest {
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
    fun offlineFirstUserDataRepository_set_dark_theme_config_delegates_to_nt_preferences() = testScope.runTest {
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
    fun offlineFirstUserDataRepository_set_news_notifications_allowed_delegates_to_nt_preferences() = testScope.runTest {
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
    fun offlineFirstUserDataRepository_set_transfer_notifications_allowed_delegates_to_nt_preferences() = testScope.runTest {
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
    fun offlineFirstUserDataRepository_set_general_notifications_allowed_delegates_to_nt_preferences() = testScope.runTest {
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

    @Test
    fun bookmarking_logs_an_article_or_a_transfer_event() = testScope.runTest {
        subject.setArticleBookmarked(articleId = 11283, bookmarked = true)
        subject.setTransferBookmarked(transferId = 19580, bookmarked = false)

        assertTrue(analyticsHelper.hasLogged(AnalyticsEvent("article_saved", listOf(AnalyticsEvent.Param("saved_article_id", "11283")))))
        assertTrue(analyticsHelper.hasLogged(AnalyticsEvent("transfer_unsaved", listOf(AnalyticsEvent.Param("unsaved_transfer_id", "19580")))))
    }
}
