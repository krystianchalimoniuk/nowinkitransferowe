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

package pl.nowinkitransferowe.feature.rumours.impl

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.fixedRumoursTestData
import pl.nowinkitransferowe.core.testing.repository.TestArticleRepository
import pl.nowinkitransferowe.core.testing.repository.TestPlayerRepository
import pl.nowinkitransferowe.core.testing.repository.TestRumourRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RumourViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val rumourRepository = TestRumourRepository()
    private val articleRepository = TestArticleRepository()
    private val playerRepository = TestPlayerRepository()
    private val userDataRepository = TestUserDataRepository()

    private fun viewModel(rumourId: Int) = RumourViewModel(rumourRepository, articleRepository, playerRepository, userDataRepository, rumourId)

    private fun sendData() {
        rumourRepository.sendRumours(fixedRumoursTestData)
        articleRepository.sendArticles(articlesTestData)
        playerRepository.sendPlayers(emptyList())
        userDataRepository.setUserData(pl.nowinkitransferowe.core.testing.repository.emptyUserData)
    }

    @Test
    fun missingRumour_isNotFound() = runTest {
        val viewModel = viewModel(rumourId = 1234)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        sendData()

        assertEquals(RumourUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun rumour_comesWithTheArticlesOfItsHistory() = runTest {
        val viewModel = viewModel(rumourId = 11)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        sendData()

        val state = assertIs<RumourUiState.Success>(viewModel.uiState.value)
        assertEquals(11, state.rumour.id)
        assertEquals(setOf(11282, 11284), state.articles.keys)
        assertEquals(11284, state.latestArticle?.id)
        assertEquals(false, state.isSaved)
    }

    @Test
    fun bookmarkRumour_savesIt() = runTest {
        val viewModel = viewModel(rumourId = 11)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        sendData()

        viewModel.bookmarkRumour(true)

        assertTrue(assertIs<RumourUiState.Success>(viewModel.uiState.value).isSaved)
    }
}
