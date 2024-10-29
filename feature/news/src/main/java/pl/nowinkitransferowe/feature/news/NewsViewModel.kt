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

package pl.nowinkitransferowe.feature.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserNewsResourceRepository
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.core.notifications.DEEP_LINK_NEWS_RESOURCE_ID_KEY
import pl.nowinkitransferowe.core.ui.NewsFeedUiState
import pl.nowinkitransferowe.feature.news.navigation.NewsRoute
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    syncManager: SyncManager,
    private val userDataRepository: UserDataRepository,
    private val userNewsResourceRepository: UserNewsResourceRepository,
) : ViewModel() {

    private val newsRoute: NewsRoute = savedStateHandle.toRoute()
    val selectedNewsId: StateFlow<String?> =
        savedStateHandle.getStateFlow(DEEP_LINK_NEWS_RESOURCE_ID_KEY, newsRoute.initialNewsId)

    private val _page: MutableStateFlow<Int> = MutableStateFlow(1)
    val page: StateFlow<Int> = _page.asStateFlow()
    val newsCount = userNewsResourceRepository.getCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )
    val isSyncing = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val feedState: StateFlow<NewsFeedUiState> =
        _page.flatMapLatest {
            userNewsResourceRepository.observeAllPages(INITIAL_PAGE_SIZE * it, 0)
        }.distinctUntilChanged()
            .map { updatedList ->
                NewsFeedUiState.Success(updatedList)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsFeedUiState.Loading,
            )

    fun updateNewsResourceSaved(newsResourceId: String, isChecked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceBookmarked(newsResourceId, isChecked)
        }
    }

    fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }

    fun onNewsClick(newsId: String?) {
        savedStateHandle[DEEP_LINK_NEWS_RESOURCE_ID_KEY] = newsId
    }

    fun loadNextPage(page: Int, newsCount: Int) {
        if (page * INITIAL_PAGE_SIZE < newsCount) {
            viewModelScope.launch {
                _page.value += 1
            }
        }
    }

    companion object {
        const val INITIAL_PAGE_SIZE = 5
    }
}
