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

package pl.nowinkitransferowe.feature.details.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.common.result.Result
import pl.nowinkitransferowe.core.common.result.asResult
import pl.nowinkitransferowe.core.data.repository.UserArticleRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserArticle

@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    private val userDataRepository: UserDataRepository,
    userArticleRepository: UserArticleRepository,
    @Assisted val newsResourceId: String,
) : ViewModel() {

    // Waits while the article is not in the database yet, for example a link opened before the
    // first sync.
    val detailsUiState = (newsResourceId.toIntOrNull()?.let(userArticleRepository::observeArticle) ?: flowOf(null))
        .filterNotNull()
        .onEach {
            if (!it.hasBeenViewed) {
                setNewsResourceViewed(it.id.toString(), true)
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
            newsResourceId.toIntOrNull()?.let { userDataRepository.setArticleBookmarked(it, bookmarked) }
        }
    }

    private fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            newsResourceId.toIntOrNull()?.let { userDataRepository.setArticleViewed(it, viewed) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(newsResourceId: String): DetailsViewModel
    }
}

sealed interface DetailsUiState {
    data class Success(val userNewsResource: UserArticle) : DetailsUiState
    data object Error : DetailsUiState
    data object Loading : DetailsUiState
}
