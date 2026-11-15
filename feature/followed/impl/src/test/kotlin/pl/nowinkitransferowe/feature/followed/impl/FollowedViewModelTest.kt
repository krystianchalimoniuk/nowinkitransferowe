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

package pl.nowinkitransferowe.feature.followed.impl

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.lewandowskiTestData
import pl.nowinkitransferowe.core.testing.data.playersTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.repository.TestClubRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestPlayerRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FollowedViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val clubRepository = TestClubRepository()
    private val playerRepository = TestPlayerRepository()
    private val dictionaryRepository = TestDictionaryRepository()

    private val viewModel by lazy {
        FollowedViewModel(userDataRepository, clubRepository, playerRepository, dictionaryRepository)
    }

    @Before
    fun sendData() {
        clubRepository.sendClubs(clubsTestData)
        playerRepository.sendPlayers(playersTestData + lewandowskiTestData)
        dictionaryRepository.sendTopics(topicsTestData)
    }

    @Test
    fun nothingFollowed_isEmpty() = runTest {
        userDataRepository.setUserData(emptyUserData)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertTrue(assertIs<FollowedUiState.Success>(viewModel.uiState.value).isEmpty())
    }

    @Test
    fun followed_areByNameWithTheirLeagues_andMissingOnesLeftOut() = runTest {
        userDataRepository.setUserData(
            emptyUserData.copy(followedClubs = setOf(83, 86, 9999), followedPlayers = setOf(lewandowskiTestData.id, 10281)),
        )
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = assertIs<FollowedUiState.Success>(viewModel.uiState.value)
        assertEquals(listOf("Como 1907" to "Serie A", "West Ham United" to "Premier League"), state.clubs.map { it.club.name to it.league })
        assertEquals(listOf("Raphael Varane", "Robert Lewandowski"), state.players.map { it.name })
    }

    @Test
    fun unfollowing_removesFromTheList() = runTest {
        userDataRepository.setUserData(emptyUserData.copy(followedClubs = setOf(83, 86), followedPlayers = setOf(10281)))
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.setClubFollowed(83, false)
        viewModel.setPlayerFollowed(10281, false)

        val state = assertIs<FollowedUiState.Success>(viewModel.uiState.value)
        assertEquals(listOf(86), state.clubs.map { it.club.id })
        assertTrue(state.players.isEmpty())
        assertEquals(setOf(86), userDataRepository.userData.first().followedClubs)
    }
}
