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

package pl.nowinkitransferowe.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.analytics.AnalyticsEvent
import pl.nowinkitransferowe.core.analytics.AnalyticsHelper
import pl.nowinkitransferowe.core.data.repository.RecentSearchRepository
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.domain.GetRecentSearchQueriesUseCase
import pl.nowinkitransferowe.core.domain.GetSearchContentUseCase
import pl.nowinkitransferowe.core.model.UserSearchResult
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    getSearchContentsUseCase: GetSearchContentUseCase,
    recentSearchQueriesUseCase: GetRecentSearchQueriesUseCase,
    private val searchContentsRepository: SearchContentsRepository,
    private val recentSearchRepository: RecentSearchRepository,
    private val userDataRepository: UserDataRepository,
    private val savedStateHandle: SavedStateHandle,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    val searchQuery: StateFlow<String?> =
        savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")

    val searchResultUiState: StateFlow<SearchResultUiState> =
        searchContentsRepository.getSearchContentsCount()
            .flatMapLatest { totalCount ->
                if (totalCount < SEARCH_MIN_FTS_ENTITY_COUNT) {
                    flowOf(SearchResultUiState.SearchNotReady)
                } else {
                    searchQuery.flatMapLatest { query ->
                        if (query == null || query.length < SEARCH_QUERY_MIN_LENGTH) {
                            flowOf(SearchResultUiState.EmptyQuery)
                        } else {
                            getSearchContentsUseCase(query)
                                // Not using .asResult() here, because it emits Loading state every
                                // time the user types a letter in the search box, which flickers the screen.
                                .map<UserSearchResult, SearchResultUiState> { data ->
                                    SearchResultUiState.Success(
                                        transferResources = data.transferResources,
                                        newsResources = data.newsResources,
                                    )
                                }
                                .catch { emit(SearchResultUiState.LoadFailed) }
                        }
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchResultUiState.Loading,
            )

    val recentSearchQueriesUiState: StateFlow<RecentSearchQueriesUiState> =
        recentSearchQueriesUseCase()
            .map(RecentSearchQueriesUiState::Success)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RecentSearchQueriesUiState.Loading,
            )

    fun onSearchQueryChanged(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    /**
     * Called when the search action is explicitly triggered by the user. For example, when the
     * search icon is tapped in the IME or when the enter key is pressed in the search text field.
     *
     * The search results are displayed on the fly as the user types, but to explicitly save the
     * search query in the search text field, defining this method.
     */
    fun onSearchTriggered(query: String) {
        viewModelScope.launch {
            recentSearchRepository.insertOrReplaceRecentSearch(searchQuery = query)
        }
        analyticsHelper.logEventSearchTriggered(query = query)
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            recentSearchRepository.clearRecentSearches()
        }
    }

    fun setNewsResourceBookmarked(newsResourceId: String, isChecked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceBookmarked(newsResourceId, isChecked)
        }
    }

    fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }

    fun setTransferResourceBookmarked(transferResourceId: String, isChecked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTransferResourceBookmarked(transferResourceId, isChecked)
        }
    }

    fun setTransferResourceViewed(transferResourceIds: List<String>, viewed: Boolean) {
        viewModelScope.launch {
            transferResourceIds.forEach {
                userDataRepository.setTransferResourceViewed(it, viewed)
            }
        }
    }
}

private fun AnalyticsHelper.logEventSearchTriggered(query: String) =
    logEvent(
        event = AnalyticsEvent(
            type = SEARCH_QUERY,
            extras = listOf(element = AnalyticsEvent.Param(key = SEARCH_QUERY, value = query)),
        ),
    )

/** Minimum length where search query is considered as [SearchResultUiState.EmptyQuery] */
private const val SEARCH_QUERY_MIN_LENGTH = 2

/** Minimum number of the fts table's entity count where it's considered as search is not ready */
private const val SEARCH_MIN_FTS_ENTITY_COUNT = 1
private const val SEARCH_QUERY = "searchQuery"
