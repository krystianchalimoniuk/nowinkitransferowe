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

package pl.nowinkitransferowe.feature.onboarding.impl

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.TopicKind
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.repository.TestClubRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OnboardingViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val clubRepository = TestClubRepository()
    private val dictionaryRepository = TestDictionaryRepository()
    private val viewModel by lazy { OnboardingViewModel(SavedStateHandle(), userDataRepository, clubRepository, dictionaryRepository) }

    // West Ham in the Premier League, Como in Serie A, Legia in Ekstraklasa
    private val legia = clubsTestData[0].copy(id = 7, slug = "legia", name = "Legia Warszawa", topic = "ekstraklasa")

    @Before
    fun setUp() {
        userDataRepository.setUserData(emptyUserData.copy(isClubsOnboardingShown = false))
        dictionaryRepository.sendTopics(topicsTestData + Topic("puchar-polski", "Puchar Polski", TopicKind.CUP))
    }

    @Test
    fun withoutClubs_isLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(OnboardingUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun leaguesWithClubs_ekstraklasaFirst_andItsClubsShown() = runTest {
        clubRepository.sendClubs(clubsTestData + legia)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = assertIs<OnboardingUiState.Success>(viewModel.uiState.value)
        assertEquals(listOf("ekstraklasa", "premier-league", "serie-a"), state.leagues.map { it.slug })
        assertEquals("ekstraklasa", state.league)
        assertEquals(listOf("Legia Warszawa"), state.clubs.map { it.name })

        viewModel.selectLeague("serie-a")
        assertEquals(listOf("Como 1907"), assertIs<OnboardingUiState.Success>(viewModel.uiState.value).clubs.map { it.name })
    }

    @Test
    fun pickedClubs_areFollowedWhenDone_andTheScreenDoesNotComeAgain() = runTest {
        clubRepository.sendClubs(clubsTestData + legia)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.togglePicked(7)
        viewModel.togglePicked(83)
        viewModel.togglePicked(83)
        assertEquals(setOf(7), assertIs<OnboardingUiState.Success>(viewModel.uiState.value).picked)

        viewModel.finish()
        val userData = userDataRepository.userData.first()
        assertEquals(setOf(7), userData.followedClubs)
        assertTrue(userData.isClubsOnboardingShown)
    }

    @Test
    fun skip_followsNothing() = runTest {
        clubRepository.sendClubs(clubsTestData + legia)
        viewModel.togglePicked(7)

        viewModel.finish(skip = true)

        val userData = userDataRepository.userData.first()
        assertTrue(userData.followedClubs.isEmpty())
        assertTrue(userData.isClubsOnboardingShown)
    }
}
