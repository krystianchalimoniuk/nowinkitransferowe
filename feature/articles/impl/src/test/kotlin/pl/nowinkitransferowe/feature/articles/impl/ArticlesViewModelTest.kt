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

package pl.nowinkitransferowe.feature.articles.impl

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserArticleRepository
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestArticleRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestRumourRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.core.testing.util.TestSyncManager
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

class ArticlesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val articleRepository = TestArticleRepository()
    private val transferRepository = TestTransferRepository()
    private val rumourRepository = TestRumourRepository()
    private val dictionaryRepository = TestDictionaryRepository()

    private lateinit var viewModel: ArticlesViewModel

    @Before
    fun setup() {
        userDataRepository.setUserData(emptyUserData)
        viewModel = ArticlesViewModel(
            savedStateHandle = SavedStateHandle(),
            syncManager = TestSyncManager(),
            userDataRepository = userDataRepository,
            userArticleRepository = CompositeUserArticleRepository(articleRepository, dictionaryRepository, userDataRepository),
            userTransferRepository = CompositeUserTransferRepository(transferRepository, userDataRepository),
            rumourRepository = rumourRepository,
            dictionaryRepository = dictionaryRepository,
        )
        articleRepository.sendArticles(articlesTestData)
        transferRepository.sendTransfers(transfersTestData)
        rumourRepository.sendRumours(rumoursTestData)
    }

    private fun success() = assertIs<HomeUiState.Success>(viewModel.uiState.value)

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun newestArticleIsFeaturedAndTheRestFollow() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = success()
        assertEquals(articlesTestData.first().id, state.featured?.id)
        assertEquals(articlesTestData.drop(1).take(ArticlesViewModel.PAGE_SIZE).map { it.id }, state.latest.map { it.id })
        assertTrue(state.showTransfers)
        assertTrue(state.showRumours)
        assertEquals(transfersTestData.take(ArticlesViewModel.CAROUSEL_SIZE).map { it.id }, state.transfers.map { it.id })
        assertEquals(rumoursTestData.map { it.id }, state.rumours.map { it.id })
    }

    @Test
    fun editorsPickIsFeaturedBeforeTheNewest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        val picked = articlesTestData[3].copy(featuredUntil = Clock.System.now() + 1.days)
        articleRepository.sendArticles(articlesTestData.map { if (it.id == picked.id) picked else it })

        assertEquals(picked.id, success().featured?.id)
        assertFalse(success().latest.any { it.id == picked.id })
    }

    @Test
    fun officialTabShowsOfficialArticlesAndTransfersOnly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.selectTab(HomeTab.OFFICIAL)

        val state = success()
        val articles = listOfNotNull(state.featured) + state.latest
        assertTrue(articles.isNotEmpty())
        assertTrue(articles.all { it.kind == ArticleKind.OFFICIAL })
        assertTrue(state.showTransfers)
        assertFalse(state.showRumours)
    }

    @Test
    fun topicFiltersArticlesAndHidesTheCarousels() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        viewModel.selectTopic("serie-a")

        val state = success()
        val articles = listOfNotNull(state.featured) + state.latest
        assertEquals(listOf(11283), articles.map { it.id })
        assertFalse(state.showTransfers)
        assertFalse(state.showRumours)

        viewModel.selectTopic(null)
        assertTrue(success().showTransfers)
    }

    @Test
    fun nextPageLoadsUntilAllArticlesAreShown() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        assertTrue(success().hasMore)

        viewModel.loadNextPage()

        val state = success()
        assertFalse(state.hasMore)
        assertEquals(articlesTestData.size, 1 + state.latest.size)
    }

    @Test
    fun savingAnArticleUpdatesTheUserData() = runTest {
        viewModel.updateArticleSaved(11283, true)

        assertEquals(setOf(11283), userDataRepository.userData.first().bookmarkedArticles)
    }
}
