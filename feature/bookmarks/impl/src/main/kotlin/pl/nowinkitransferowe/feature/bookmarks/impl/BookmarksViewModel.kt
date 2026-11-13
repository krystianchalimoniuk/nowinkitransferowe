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

package pl.nowinkitransferowe.feature.bookmarks.impl

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
import pl.nowinkitransferowe.core.data.repository.UserArticleRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.ui.ArticleFeedUiState
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    userArticleRepository: UserArticleRepository,
    userTransferRepository: UserTransferRepository,
) : ViewModel() {

    var shouldDisplayUndoBookmark by mutableStateOf(false)
    private var lastRemovedBookmark: BookmarkElement? = null

    val newsFeedUiState: StateFlow<ArticleFeedUiState> =
        userArticleRepository.observeBookmarked()
            .map<List<UserArticle>, ArticleFeedUiState>(ArticleFeedUiState::Success)
            .onStart { emit(ArticleFeedUiState.Loading) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ArticleFeedUiState.Loading,
            )
    val transferFeedUiState: StateFlow<TransferFeedUiState> =
        userTransferRepository.observeBookmarked()
            .map<List<UserTransfer>, TransferFeedUiState>(TransferFeedUiState::Success)
            .onStart { emit(TransferFeedUiState.Loading) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransferFeedUiState.Loading,
            )

    fun removeArticleBookmark(articleId: Int) {
        viewModelScope.launch {
            shouldDisplayUndoBookmark = true
            lastRemovedBookmark = BookmarkElement.Article(articleId)
            userDataRepository.setArticleBookmarked(articleId, false)
        }
    }

    fun removeTransferBookmark(transferId: Int) {
        viewModelScope.launch {
            shouldDisplayUndoBookmark = true
            lastRemovedBookmark = BookmarkElement.Transfer(transferId)
            userDataRepository.setTransferBookmarked(transferId, false)
        }
    }

    fun setArticleViewed(articleId: Int, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setArticleViewed(articleId, viewed)
        }
    }

    fun setTransferViewed(transferIds: List<Int>, viewed: Boolean) {
        viewModelScope.launch {
            transferIds.forEach {
                userDataRepository.setTransferViewed(it, viewed)
            }
        }
    }

    fun undoBookmarkRemoval() {
        viewModelScope.launch {
            lastRemovedBookmark?.let {
                when (it) {
                    is BookmarkElement.Article -> {
                        userDataRepository.setArticleBookmarked(it.id, true)
                    }

                    is BookmarkElement.Transfer -> {
                        userDataRepository.setTransferBookmarked(it.id, true)
                    }
                }
            }
        }
        clearUndoState()
    }

    fun clearUndoState() {
        shouldDisplayUndoBookmark = false
        lastRemovedBookmark = null
    }
}

sealed interface BookmarkUiState {
    data object Loading : BookmarkUiState
    data class Success(
        val articleFeed: List<UserArticle>,
        val transfersFeed: List<UserTransfer>,
    ) : BookmarkUiState
}

sealed class BookmarkElement(open val id: Int) {
    data class Article(override val id: Int) : BookmarkElement(id)
    data class Transfer(override val id: Int) : BookmarkElement(id)
}
