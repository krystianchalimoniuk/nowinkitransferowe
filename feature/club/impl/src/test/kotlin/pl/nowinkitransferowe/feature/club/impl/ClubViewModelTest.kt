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

package pl.nowinkitransferowe.feature.club.impl

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.fixedRumoursTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestClubRepository
import pl.nowinkitransferowe.core.testing.repository.TestClubSeasonsRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestRumourRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ClubViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val clubRepository = TestClubRepository()
    private val seasonsRepository = TestClubSeasonsRepository()
    private val transferRepository = TestTransferRepository()
    private val rumourRepository = TestRumourRepository()
    private val dictionaryRepository = TestDictionaryRepository()
    private val userDataRepository = TestUserDataRepository()

    private fun viewModel(clubId: Int) = ClubViewModel(
        savedStateHandle = SavedStateHandle(),
        clubRepository = clubRepository,
        clubSeasonsRepository = seasonsRepository,
        userTransferRepository = CompositeUserTransferRepository(transferRepository, userDataRepository),
        rumourRepository = rumourRepository,
        dictionaryRepository = dictionaryRepository,
        clubId = clubId,
    )

    @Before
    fun sendData() {
        clubRepository.sendClubs(clubsTestData)
        seasonsRepository.sendSeasons(mapOf(83 to westHamSeasonsTestData.reversed()))
        transferRepository.sendTransfers(transfersTestData)
        rumourRepository.sendRumours(fixedRumoursTestData)
        dictionaryRepository.sendTopics(topicsTestData)
        dictionaryRepository.sendCountries(countriesTestData)
        userDataRepository.setUserData(emptyUserData)
    }

    @Test
    fun missingClub_isNotFound() = runTest {
        val viewModel = viewModel(clubId = 1234)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        assertEquals(ClubUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun club_comesWithItsFactsSeasonsAndTransfers() = runTest {
        val viewModel = viewModel(clubId = 83)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = assertIs<ClubUiState.Success>(viewModel.uiState.value)
        assertEquals("Anglia", state.country?.name)
        assertEquals("premier-league", state.league?.slug)
        assertEquals(westHamSeasonsTestData.map { it.season }, state.seasons.map { it.season })
        assertTrue(state.arrivals.isNotEmpty())
        assertTrue(state.arrivals.all { (it.to as ClubRef.Club).id == 83 })
        assertTrue(state.departures.isEmpty())
        assertEquals(listOf(83), seasonsRepository.refreshedClubs)
    }

    @Test
    fun selectTab_changesTab() = runTest {
        val viewModel = viewModel(clubId = 86)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.selectTab(ClubTab.ARRIVALS)

        assertEquals(ClubTab.ARRIVALS, assertIs<ClubUiState.Success>(viewModel.uiState.value).tab)
    }
}
