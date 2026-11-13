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

package pl.nowinkitransferowe.core.datastore

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.nowinkitransferowe.core.datastoreproto.UserPreferences
import pl.nowinkitransferowe.core.datastoretest.InMemoryDataStore
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NtPreferenceDataSourceTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())
    private lateinit var subject: NtPreferencesDataSource

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        subject = NtPreferencesDataSource(
            InMemoryDataStore(UserPreferences.getDefaultInstance()),
        )
    }

    @Test
    fun shouldUseDynamicColorFalseByDefault() = testScope.runTest {
        assertFalse(subject.userData.first().useDynamicColor)
    }

    @Test
    fun userShouldUseDynamicColorIsTrueWhenSet() = testScope.runTest {
        subject.setDynamicColorPreference(true)
        assertTrue(subject.userData.first().useDynamicColor)
    }

    @Test
    fun isNewsNotificationsAllowedFalseByDefault() = runTest {
        assertFalse(subject.userData.first().isNewsNotificationsAllowed)
    }

    @Test
    fun isNewsNotificationsAllowedIsTrueWhenSet() = runTest {
        subject.setNewsNotificationsAllowed(true)
        assertTrue(subject.userData.first().isNewsNotificationsAllowed)
    }

    @Test
    fun isTransferNotificationsAllowedFalseByDefault() = runTest {
        assertFalse(subject.userData.first().isTransfersNotificationsAllowed)
    }

    @Test
    fun isTransferNotificationsAllowedIsTrueWhenSet() = runTest {
        subject.setTransfersNotificationsAllowed(true)
        assertTrue(subject.userData.first().isTransfersNotificationsAllowed)
    }

    @Test
    fun isGeneralNotificationsAllowedFalseByDefault() = runTest {
        assertFalse(subject.userData.first().isTransfersNotificationsAllowed)
    }

    @Test
    fun isGeneralNotificationsAllowedIsTrueWhenSet() = runTest {
        subject.setGeneralNotificationsAllowed(true)
        assertTrue(subject.userData.first().isGeneralNotificationAllowed)
    }

    @Test
    fun followedClubsPlayersAndTopicsAreSaved() = runTest {
        subject.setClubFollowed(86, true)
        subject.setClubFollowed(44, true)
        subject.setPlayerFollowed(10281, true)
        subject.setTopicFollowed("premier-league", true)
        subject.setClubFollowed(44, false)

        val userData = subject.userData.first()
        assertEquals(setOf(86), userData.followedClubs)
        assertEquals(setOf(10281), userData.followedPlayers)
        assertEquals(setOf("premier-league"), userData.followedTopics)
    }

    @Test
    fun followedNotificationSwitchesAreSaved() = runTest {
        subject.setFollowedNotificationsAllowed(true)
        subject.setFollowedLeaguesNotificationsAllowed(false)

        val userData = subject.userData.first()
        assertTrue(userData.isFollowedNotificationsAllowed)
        assertFalse(userData.isFollowedLeaguesNotificationsAllowed)
    }

    @Test
    fun changeListVersionsAreSaved() = runTest {
        assertEquals(ChangeListVersions(), subject.getChangeListVersions())

        subject.updateChangeListVersion { copy(articleVersion = 120) }
        subject.updateChangeListVersion { copy(playerVersion = 7) }

        assertEquals(
            ChangeListVersions(articleVersion = 120, playerVersion = 7),
            subject.getChangeListVersions(),
        )
    }

    @Test
    fun notifiedKeysKeepOnlyTheNewest() = runTest {
        subject.addNotifiedKeys((1..NtPreferencesDataSource.MAX_NOTIFIED_KEYS).map { "article:$it" })
        subject.addNotifiedKeys(listOf("article:1", "rumour:5:3"))

        val keys = subject.getNotifiedKeys()
        assertEquals(NtPreferencesDataSource.MAX_NOTIFIED_KEYS, keys.size)
        assertFalse("article:2" in keys)
        assertTrue("article:1" in keys)
        assertTrue("rumour:5:3" in keys)
    }
}
