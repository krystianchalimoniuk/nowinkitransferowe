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

package pl.nowinkitransferowe.feature.leagues.impl

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.leagueSeasonsTestData
import pl.nowinkitransferowe.core.testing.data.leaguesTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestClubRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestLeaguesRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LeaguesViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val leaguesRepository = TestLeaguesRepository()
    private val dictionaryRepository = TestDictionaryRepository()

    @Test
    fun opensTheCurrentSeason_andFetchesIt() = runTest {
        val viewModel = LeaguesViewModel(SavedStateHandle(), leaguesRepository, dictionaryRepository)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        leaguesRepository.send(leagueSeasonsTestData, mapOf(Season(2026) to leaguesTestData))

        val state = assertIs<LeaguesUiState.Success>(viewModel.uiState.value)
        assertEquals(Season(2026), state.season)
        assertEquals(leaguesTestData, state.leagues)
        assertEquals("Premier League", state.names["premier-league"])
        assertEquals(listOf<Season?>(null), leaguesRepository.refreshedSeasons)
    }

    @Test
    fun selectSeason_showsAndFetchesIt() = runTest {
        val viewModel = LeaguesViewModel(SavedStateHandle(), leaguesRepository, dictionaryRepository)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        leaguesRepository.send(leagueSeasonsTestData, mapOf(Season(2026) to leaguesTestData, Season(2025) to leaguesTestData.take(1)))

        viewModel.selectSeason(Season(2025))

        val state = assertIs<LeaguesUiState.Success>(viewModel.uiState.value)
        assertEquals(Season(2025), state.season)
        assertEquals(1, state.leagues.size)
        assertEquals(Season(2025), leaguesRepository.refreshedSeasons.last())
    }
}

class LeagueViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val leaguesRepository = TestLeaguesRepository()
    private val clubRepository = TestClubRepository()
    private val transferRepository = TestTransferRepository()
    private val userDataRepository = TestUserDataRepository()

    private fun viewModel(topic: String) = LeagueViewModel(
        leaguesRepository = leaguesRepository,
        dictionaryRepository = TestDictionaryRepository().apply { sendTopics(topicsTestData) },
        clubRepository = clubRepository,
        userTransferRepository = CompositeUserTransferRepository(transferRepository, userDataRepository),
        topic = topic,
        seasonStartYear = 2026,
    )

    private fun sendData() {
        leaguesRepository.send(leagueSeasonsTestData, mapOf(Season(2026) to leaguesTestData))
        clubRepository.sendClubs(clubsTestData)
        transferRepository.sendTransfers(transfersTestData)
        userDataRepository.setUserData(emptyUserData)
    }

    @Test
    fun league_comesWithItsClubsTopTransfersAndTheOtherLeagues() = runTest {
        val viewModel = viewModel("serie-a")
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        sendData()

        val state = assertIs<LeagueUiState.Success>(viewModel.uiState.value)
        assertEquals("Serie A", state.name)
        assertEquals("Como 1907", state.clubs[86]?.name)
        assertEquals(listOf(14999), state.topArrivals.map { it.id })
        assertEquals(leaguesTestData.size, state.allLeagues.size)
        assertEquals(listOf<Season?>(Season(2026)), leaguesRepository.refreshedSeasons)
    }

    @Test
    fun missingLeague_isNotFound() = runTest {
        val viewModel = viewModel("la-liga")
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        sendData()

        assertEquals(LeagueUiState.NotFound, viewModel.uiState.value)
    }
}
