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

package pl.nowinkitransferowe.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.common.result.Result
import pl.nowinkitransferowe.core.common.result.asResult
import pl.nowinkitransferowe.core.data.repository.NewsRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.feature.details.navigation.DetailNewsRoute
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    newsRepository: NewsRepository,
) : ViewModel() {

    val newsResourceId: String = savedStateHandle.toRoute<DetailNewsRoute>().newsId
    val detailsUiState = if (newsResourceId == null) {
        flowOf()
    } else {
        newsRepository.getNewsResource(newsResourceId)
    }.combine(userDataRepository.userData) { newsResource, userData ->
        UserNewsResource(newsResource, userData)
    }.onEach {
        if (!it.hasBeenViewed) {
            setNewsResourceViewed(it.id, true)
        }
    }.asResult()
        .map { result ->
            when (result) {
                is Result.Success -> {
                    DetailsUiState.Success(result.data)
                }

                is Result.Loading -> {
                    DetailsUiState.Loading
                }

                is Result.Error -> DetailsUiState.Error
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUiState.Loading)

    val darkThemeConfig = userDataRepository.userData.map { userData ->
        userData.darkThemeConfig
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DarkThemeConfig.FOLLOW_SYSTEM)

    fun bookmarkNews(newsResourceId: String, bookmarked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceBookmarked(newsResourceId, bookmarked)
        }
    }

    private fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }
}

sealed interface DetailsUiState {
    data class Success(val userNewsResource: UserNewsResource) : DetailsUiState
    data object Error : DetailsUiState
    data object Loading : DetailsUiState
}
