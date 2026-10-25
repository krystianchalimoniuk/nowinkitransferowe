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

package pl.nowinkitransferowe

import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation3.runtime.NavBackStack
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pl.nowinkitransferowe.core.data.repository.CompositeUserNewsResourceRepository
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferResourceRepository
import pl.nowinkitransferowe.core.navigation.NavigationState
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.core.testing.repository.TestNewsRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.util.TestNetworkMonitor
import pl.nowinkitransferowe.core.testing.util.TestTimeZoneMonitor
import pl.nowinkitransferowe.feature.bookmarks.api.navigation.BookmarksNavKey
import pl.nowinkitransferowe.feature.details.transfers.api.navigation.DetailsTransferNavKey
import pl.nowinkitransferowe.feature.news.api.navigation.NewsNavKey
import pl.nowinkitransferowe.feature.transfers.api.navigation.TransfersNavKey
import pl.nowinkitransferowe.ui.NtAppState
import pl.nowinkitransferowe.ui.rememberNtAppState
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Tests [NtAppState].
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
@HiltAndroidTest
class NtAppStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Create the test dependencies.
    private val networkMonitor = TestNetworkMonitor()

    private val timeZoneMonitor = TestTimeZoneMonitor()

    private val userDataRepository = TestUserDataRepository()
    private val userNewsResourceRepository =
        CompositeUserNewsResourceRepository(TestNewsRepository(), userDataRepository)
    private val userTransferResourceRepository =
        CompositeUserTransferResourceRepository(TestTransferRepository(), userDataRepository)

    // Subject under test.
    private lateinit var state: NtAppState

    private fun testNavigationState() = NavigationState(
        startKey = NewsNavKey,
        topLevelStack = NavBackStack(NewsNavKey),
        subStacks = mapOf(
            NewsNavKey to NavBackStack(NewsNavKey),
            TransfersNavKey to NavBackStack(TransfersNavKey),
            BookmarksNavKey to NavBackStack(BookmarksNavKey),
        ),
    )

    private fun testAppState(navigationState: NavigationState, coroutineScope: CoroutineScope) = NtAppState(
        navigationState = navigationState,
        coroutineScope = coroutineScope,
        networkMonitor = networkMonitor,
        userNewsResourceRepository = userNewsResourceRepository,
        userTransferResourceRepository = userTransferResourceRepository,
        timeZoneMonitor = timeZoneMonitor,
    )

    @Test
    fun ntAppState_currentDestination() = runTest {
        val navigationState = testNavigationState()
        val navigator = Navigator(navigationState)

        composeTestRule.setContent {
            state = remember(navigationState) { testAppState(navigationState, backgroundScope) }
        }

        assertEquals(NewsNavKey, state.navigationState.currentTopLevelKey)
        assertEquals(NewsNavKey, state.navigationState.currentKey)

        // Navigate to another destination once
        navigator.navigate(BookmarksNavKey)

        composeTestRule.waitForIdle()

        assertEquals(BookmarksNavKey, state.navigationState.currentTopLevelKey)
        assertEquals(BookmarksNavKey, state.navigationState.currentKey)
    }

    @Test
    fun ntAppState_destinations() = runTest {
        composeTestRule.setContent {
            state = rememberNtAppState(
                networkMonitor = networkMonitor,
                userNewsResourceRepository = userNewsResourceRepository,
                userTransferResourceRepository = userTransferResourceRepository,
                timeZoneMonitor = timeZoneMonitor,
            )
        }

        assertEquals(
            setOf(NewsNavKey, TransfersNavKey, BookmarksNavKey),
            state.navigationState.topLevelKeys,
        )
    }

    @Test
    fun ntAppState_deepLinkKeys_openDetailsAboveTheirList() = runTest {
        val navigationState = testNavigationState()
        val navigator = Navigator(navigationState)
        val details = DetailsTransferNavKey(transferId = "123")

        composeTestRule.setContent {
            state = remember(navigationState) { testAppState(navigationState, backgroundScope) }
        }

        // The keys MainActivity creates for a transfer notification
        listOf(TransfersNavKey, details).forEach(navigator::navigate)
        composeTestRule.waitForIdle()

        assertEquals(TransfersNavKey, state.navigationState.currentTopLevelKey)
        assertEquals(details, state.navigationState.currentKey)
        assertFalse(state.navigationState.isAtTopLevel)

        // Going back shows the transfers list
        navigator.goBack()
        composeTestRule.waitForIdle()

        assertEquals(TransfersNavKey, state.navigationState.currentKey)
    }

    @Test
    fun ntAppState_whenNetworkMonitorIsOffline_StateIsOffline() = runTest(UnconfinedTestDispatcher()) {
        composeTestRule.setContent {
            state = testAppState(testNavigationState(), backgroundScope)
        }

        backgroundScope.launch { state.isOffline.collect() }
        networkMonitor.setConnected(false)
        assertEquals(
            true,
            state.isOffline.value,
        )
    }

    @Test
    fun ntAppState_differentTZ_withTimeZoneMonitorChange() = runTest(UnconfinedTestDispatcher()) {
        composeTestRule.setContent {
            state = testAppState(testNavigationState(), backgroundScope)
        }
        val changedTz = TimeZone.of("Europe/Prague")
        backgroundScope.launch { state.currentTimeZone.collect() }
        timeZoneMonitor.setTimeZone(changedTz)
        assertEquals(
            changedTz,
            state.currentTimeZone.value,
        )
    }
}
