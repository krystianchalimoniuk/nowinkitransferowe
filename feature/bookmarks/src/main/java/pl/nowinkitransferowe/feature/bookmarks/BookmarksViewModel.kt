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

package pl.nowinkitransferowe.feature.bookmarks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserNewsResourceRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferResourceRepository
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.ui.NewsFeedUiState
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    userNewsResourceRepository: UserNewsResourceRepository,
    userTransferResourceRepository: UserTransferResourceRepository,
) : ViewModel() {

    var shouldDisplayUndoNewsBookmark by mutableStateOf(false)
    private var lastRemovedNewsBookmark: BookmarkElement? = null

    val newsFeedUiState: StateFlow<NewsFeedUiState> =
        userNewsResourceRepository.observeAllBookmarked()
            .map<List<UserNewsResource>, NewsFeedUiState>(NewsFeedUiState::Success)
            .onStart { emit(NewsFeedUiState.Loading) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsFeedUiState.Loading,
            )
    val transferFeedUiState: StateFlow<TransferFeedUiState> =
        userTransferResourceRepository.observeAllBookmarked()
            .map<List<UserTransferResource>, TransferFeedUiState>(TransferFeedUiState::Success)
            .onStart { emit(TransferFeedUiState.Loading) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransferFeedUiState.Loading,
            )

//    val bookmarkUIState: StateFlow<BookmarkUiState> =
//        combine(
//            userNewsResourceRepository.observeAllBookmarked(),
//            userTransferResourceRepository.observeAllBookmarked()
//        ) { news, transfers ->
//            BookmarkUiState.Success(news, transfers)
//        }.stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = BookmarkUiState.Loading
//        )

    fun removeFromSavedNewsResources(newsResourceId: String) {
        viewModelScope.launch {
            shouldDisplayUndoNewsBookmark = true
            lastRemovedNewsBookmark = BookmarkElement.News(newsResourceId)
            userDataRepository.setNewsResourceBookmarked(newsResourceId, false)
        }
    }

    fun removeFromSavedTransferResources(transferResourceId: String) {
        viewModelScope.launch {
            shouldDisplayUndoNewsBookmark = true
            lastRemovedNewsBookmark = BookmarkElement.Transfer(transferResourceId)
            userDataRepository.setTransferResourceBookmarked(transferResourceId, false)
        }
    }

    fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }

    fun setTransferResourceViewed(transferResourceIds: List<String>, viewed: Boolean) {
        viewModelScope.launch {
            transferResourceIds.forEach {
                userDataRepository.setTransferResourceViewed(it, viewed)
            }
        }
    }

    fun undoBookmarkRemoval() {
        viewModelScope.launch {
            lastRemovedNewsBookmark?.let {
                when (it) {
                    is BookmarkElement.News -> {
                        userDataRepository.setNewsResourceBookmarked(it.id, true)
                    }

                    is BookmarkElement.Transfer -> {
                        userDataRepository.setTransferResourceBookmarked(it.id, true)
                    }
                }
            }
        }
        clearUndoState()
    }

    fun clearUndoState() {
        shouldDisplayUndoNewsBookmark = false
        lastRemovedNewsBookmark = null
    }
}

sealed interface BookmarkUiState {
    data object Loading : BookmarkUiState
    data class Success(
        val newsFeed: List<UserNewsResource>,
        val transfersFeed: List<UserTransferResource>,
    ) : BookmarkUiState
}

sealed class BookmarkElement(open val id: String) {
    data class News(override val id: String) : BookmarkElement(id)
    data class Transfer(override val id: String) : BookmarkElement(id)
}
