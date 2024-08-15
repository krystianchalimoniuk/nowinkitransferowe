package pl.nowinkitransferowe.feature.search

import androidx.lifecycle.SavedStateHandle
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
import pl.nowinkitransferowe.core.testing.data.newsResourcesTestData
import pl.nowinkitransferowe.core.testing.data.transferResourceTestData
import pl.nowinkitransferowe.core.testing.repository.TestRecentSearchRepository
import pl.nowinkitransferowe.core.testing.repository.TestSearchContentsRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.collect


class SearchViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val searchContentsRepository = TestSearchContentsRepository()
    private val getSearchContentsUseCase = GetSearchContentUseCase(
        searchContentsRepository = searchContentsRepository,
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
        )
        userDataRepository.setUserData(emptyUserData)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(SearchResultUiState.Loading, viewModel.searchResultUiState.value)
    }

    @Test
    fun stateIsEmptyQuery_withEmptySearchQuery() = runTest {
        searchContentsRepository.addNewsResources(newsResourcesTestData)
        searchContentsRepository.addTransferResources(transferResourceTestData)
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() }

        viewModel.onSearchQueryChanged("")

        assertEquals(SearchResultUiState.EmptyQuery, viewModel.searchResultUiState.value)

        collectJob.cancel()
    }

    @Test
    fun emptyResultIsReturned_withNotMatchingQuery() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() }

        viewModel.onSearchQueryChanged("XXX")
        searchContentsRepository.addNewsResources(newsResourcesTestData)
        searchContentsRepository.addTransferResources(transferResourceTestData)

        val result = viewModel.searchResultUiState.value
        assertIs<SearchResultUiState.Success>(result)

        collectJob.cancel()
    }

    @Test
    fun recentSearches_verifyUiStateIsSuccess() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.recentSearchQueriesUiState.collect() }
        viewModel.onSearchTriggered("kotlin")

        val result = viewModel.recentSearchQueriesUiState.value
        assertIs<RecentSearchQueriesUiState.Success>(result)

        collectJob.cancel()
    }

    @Test
    fun searchNotReady_withNoFtsTableEntity() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchResultUiState.collect() }

        viewModel.onSearchQueryChanged("")

        assertEquals(SearchResultUiState.SearchNotReady, viewModel.searchResultUiState.value)

        collectJob.cancel()
    }

    @Test
    fun whenToggleNewsResourceSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        val newsResourceId = "123"
        viewModel.setNewsResourceBookmarked(newsResourceId, true)

        assertEquals(
            expected = setOf(newsResourceId),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )

        viewModel.setNewsResourceBookmarked(newsResourceId, false)

        assertEquals(
            expected = emptySet(),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )
    }
}