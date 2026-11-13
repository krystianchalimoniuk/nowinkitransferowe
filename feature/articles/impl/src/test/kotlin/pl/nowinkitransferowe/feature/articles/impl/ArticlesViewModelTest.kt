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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserArticleRepository
import pl.nowinkitransferowe.core.model.mapToUserArticles
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.repository.TestArticleRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.core.testing.util.TestSyncManager
import pl.nowinkitransferowe.core.ui.ArticleFeedUiState
import kotlin.test.assertEquals

class ArticlesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val syncManager = TestSyncManager()
    private val userDataRepository = TestUserDataRepository()
    private val articleRepository = TestArticleRepository()

    private val userArticleRepository = CompositeUserArticleRepository(
        articleRepository = articleRepository,
        dictionaryRepository = TestDictionaryRepository(),
        userDataRepository = userDataRepository,
    )

    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: ArticlesViewModel

    @Before
    fun setup() {
        userDataRepository.setUserData(emptyUserData)
        viewModel = ArticlesViewModel(
            syncManager = syncManager,
            savedStateHandle = savedStateHandle,
            userDataRepository = userDataRepository,
            userArticleRepository = userArticleRepository,
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(ArticleFeedUiState.Loading, viewModel.feedState.value)
    }

    @Test
    fun stateIsLoadingWhenAppIsSyncingWithNoNews() = runTest {
        syncManager.setSyncing(true)

        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isSyncing.collect() }

        assertEquals(true, viewModel.isSyncing.value)
    }

    @Test
    fun whenUpdateArticleSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        viewModel.updateArticleSaved(123, true)

        assertEquals(setOf(123), userDataRepository.userData.first().bookmarkedArticles)

        viewModel.updateArticleSaved(123, false)

        assertEquals(emptySet(), userDataRepository.userData.first().bookmarkedArticles)
    }

    @Test
    fun firstPageShowsTheNewestArticlesWithTheUserData() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }
        articleRepository.sendArticles(articlesTestData)

        viewModel.updateArticleSaved(articleId = 11283, isChecked = true)

        val userData = emptyUserData.copy(bookmarkedArticles = setOf(11283))
        assertEquals(
            ArticleFeedUiState.Success(
                feed = articlesTestData.take(ArticlesViewModel.INITIAL_PAGE_SIZE).mapToUserArticles(topicsTestData, userData),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun whenLoadNexPageFunctionIsCalled_articlesUpdates() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }
        articleRepository.sendArticles(articlesTestData)

        viewModel.loadNextPage(viewModel.page.value, articlesTestData.size)

        assertEquals(
            ArticleFeedUiState.Success(
                feed = articlesTestData.take(2 * ArticlesViewModel.INITIAL_PAGE_SIZE).mapToUserArticles(topicsTestData, emptyUserData),
            ),
            viewModel.feedState.value,
        )
    }

    @Test
    fun articleShowsTheNamesOfItsTopics() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedState.collect() }
        articleRepository.sendArticles(articlesTestData)

        val feed = (viewModel.feedState.value as ArticleFeedUiState.Success).feed
        assertEquals(listOf("Premier League"), feed.first().topics.map { it.name })
        assertEquals(emptyList(), feed[3].topics)
    }
}
