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

package pl.nowinkitransferowe.feature.transfers.impl

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.todayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.core.testing.util.TestSyncManager
import pl.nowinkitransferowe.feature.transfers.impl.navigation.TransfersUiState
import pl.nowinkitransferowe.feature.transfers.impl.navigation.TransfersViewModel
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TransfersViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val transferRepository = TestTransferRepository()
    private lateinit var viewModel: TransfersViewModel

    private val season2425 = Season(2024)

    @Before
    fun setup() {
        userDataRepository.setUserData(emptyUserData)
        transferRepository.clubTopics = mapOf(83 to "premier-league", 86 to "serie-a")
        transferRepository.sendTransfers(transfersTestData)
        viewModel = TransfersViewModel(
            savedStateHandle = SavedStateHandle(),
            syncManager = TestSyncManager(),
            userTransferRepository = CompositeUserTransferRepository(transferRepository, userDataRepository),
            dictionaryRepository = TestDictionaryRepository(),
            userDataRepository = userDataRepository,
        )
    }

    private fun success() = assertIs<TransfersUiState.Success>(viewModel.uiState.value)

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(TransfersUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun currentSeasonIsShownFirstAndSeasonsGoBackTo1718() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = success()
        val current = Season.of(Clock.System.todayIn(Season.TIME_ZONE))
        assertEquals(current, state.season)
        assertEquals(current, state.seasons.first())
        assertEquals(Season(2017), state.seasons.last())
        assertTrue(state.transfers.all { it.season == current })
    }

    @Test
    fun seasonFilterShowsItsTransfersNewestFirst() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.selectSeason(season2425)

        val expected = transfersTestData.filter { it.season == season2425 }.map { it.id }
        assertEquals(expected, success().transfers.map { it.id })
        assertFalse(success().hasMore)
    }

    @Test
    fun leagueFilterKeepsTransfersOfItsClubs() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        viewModel.selectSeason(season2425)

        viewModel.selectTopic("premier-league")
        assertEquals(emptyList(), success().transfers)

        viewModel.selectTopic("serie-a")
        assertTrue(success().transfers.isNotEmpty())
        assertEquals(transfersTestData.filter { it.season == season2425 }.size, success().transfers.size)
    }

    @Test
    fun leaguesAreTheLeagueTopicsOnly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(listOf("premier-league", "serie-a", "ekstraklasa"), success().leagues.map { it.slug })
    }

    @Test
    fun markingAsReadSavesTheIds() = runTest {
        viewModel.setTransfersViewed(listOf(19580, 15000), true)

        assertEquals(setOf(19580, 15000), userDataRepository.userData.first().viewedTransfers)
    }

    @Test
    fun markAllRead_readsEveryTransferNotOnlyTheLoadedOnes() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        transferRepository.sendTransfers(transfersTestData)
        userDataRepository.setUserData(emptyUserData)
        assertTrue(assertIs<TransfersUiState.Success>(viewModel.uiState.value).hasUnread)

        viewModel.markAllRead()

        val state = assertIs<TransfersUiState.Success>(viewModel.uiState.value)
        assertFalse(state.hasUnread)
        assertTrue(state.transfers.all { it.hasBeenViewed })
    }
}
