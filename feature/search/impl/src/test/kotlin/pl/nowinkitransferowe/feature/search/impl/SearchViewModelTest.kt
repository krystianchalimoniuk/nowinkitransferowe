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

package pl.nowinkitransferowe.feature.search.impl

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.analytics.NoOpAnalyticsHelper
import pl.nowinkitransferowe.core.domain.GetRecentSearchQueriesUseCase
import pl.nowinkitransferowe.core.domain.GetSearchContentUseCase
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestRecentSearchRepository
import pl.nowinkitransferowe.core.testing.repository.TestSearchContentsRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.feature.search.api.navigation.SearchNavKey
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val searchContentsRepository = TestSearchContentsRepository()
    private val getSearchContentsUseCase = GetSearchContentUseCase(
        searchContentsRepository = searchContentsRepository,
        dictionaryRepository = TestDictionaryRepository(),
        userDataRepository = userDataRepository,
    )
    private val recentSearchRepository = TestRecentSearchRepository()
    private val getRecentQueryUseCase = GetRecentSearchQueriesUseCase(recentSearchRepository)

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        viewModel = SearchViewModel(
            getSearchContentsUseCase = getSearchContentsUseCase,
            recentSearchQueriesUseCase = getRecentQueryUseCase,
            searchContentsRepository = searchContentsRepository,
            savedStateHandle = SavedStateHandle(),
            recentSearchRepository = recentSearchRepository,
            userDataRepository = userDataRepository,
            analyticsHelper = NoOpAnalyticsHelper(),
            navKey = SearchNavKey(),
        )
        userDataRepository.setUserData(emptyUserData)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(SearchResultUiState.Loading, viewModel.searchResultUiState.value)
    }

    @Test
    fun stateIsEmptyQuery_withEmptySearchQuery() = runTest {
        searchContentsRepository.addArticles(articlesTestData)
        searchContentsRepository.addTransfers(transfersTestData)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() }

        viewModel.onSearchQueryChanged("")

        assertEquals(SearchResultUiState.EmptyQuery, viewModel.searchResultUiState.value)
    }

    @Test
    fun emptyResultIsReturned_withNotMatchingQuery() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() }
        viewModel.onSearchQueryChanged("XXX")

        searchContentsRepository.addArticles(articlesTestData)
        searchContentsRepository.addTransfers(transfersTestData)

        val result = viewModel.searchResultUiState.value
        assertIs<SearchResultUiState.Success>(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun matchingArticlesAndTransfersAreReturned() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() }
        searchContentsRepository.addArticles(articlesTestData)
        searchContentsRepository.addTransfers(transfersTestData)

        viewModel.onSearchQueryChanged("Varane")

        val result = viewModel.searchResultUiState.value
        assertIs<SearchResultUiState.Success>(result)
        assertEquals(listOf(11283), result.newsResources.map { it.id })
        assertEquals(listOf(14999), result.transferResources.map { it.id })
    }

    @Test
    fun recentSearches_verifyUiStateIsSuccess() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.recentSearchQueriesUiState.collect() }
        viewModel.onSearchTriggered("kotlin")

        val result = viewModel.recentSearchQueriesUiState.value
        assertIs<RecentSearchQueriesUiState.Success>(result)
    }

    @Test
    fun emptySearchText_isNotAddedToRecentSearches() = runTest {
        viewModel.onSearchTriggered("")

        val recentSearchQuery = getRecentQueryUseCase().first().firstOrNull()

        assertNull(recentSearchQuery)
    }

    @Test
    fun searchTextWithSpacesAndOneLetter_isEmptyQuery() = runTest {
        searchContentsRepository.addArticles(articlesTestData)
        searchContentsRepository.addTransfers(transfersTestData)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() }

        viewModel.onSearchQueryChanged("   a")

        assertEquals(SearchResultUiState.EmptyQuery, viewModel.searchResultUiState.value)
    }

    @Test
    fun searchNotReady_withNoFtsTableEntity() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() }

        viewModel.onSearchQueryChanged("")

        assertEquals(SearchResultUiState.SearchNotReady, viewModel.searchResultUiState.value)
    }

    @Test
    fun whenToggleNewsResourceSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        val newsResourceId = 123
        viewModel.setNewsResourceBookmarked(newsResourceId, true)

        assertEquals(
            expected = setOf(newsResourceId),
            actual = userDataRepository.userData.first().bookmarkedArticles,
        )

        viewModel.setNewsResourceBookmarked(newsResourceId, false)

        assertEquals(
            expected = emptySet(),
            actual = userDataRepository.userData.first().bookmarkedArticles,
        )
    }
}
