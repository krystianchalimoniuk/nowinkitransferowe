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

package pl.nowinkitransferowe.feature.bookmarks.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.UserArticleRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.model.UserTransfer
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    userArticleRepository: UserArticleRepository,
    userTransferRepository: UserTransferRepository,
    rumourRepository: RumourRepository,
) : ViewModel() {

    var shouldDisplayUndoBookmark by mutableStateOf(false)
    private var lastRemovedBookmark: BookmarkElement? = null

    val uiState: StateFlow<BookmarksUiState> = combine(
        savedStateHandle.getStateFlow(TAB_KEY, SavedTab.ARTICLES),
        userArticleRepository.observeBookmarked(),
        userTransferRepository.observeBookmarked(),
        userDataRepository.userData
            .map { it.bookmarkedRumours }
            .distinctUntilChanged()
            .flatMapLatest(rumourRepository::observeRumours),
        BookmarksUiState::Success,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BookmarksUiState.Loading,
    )

    fun selectTab(tab: SavedTab) {
        savedStateHandle[TAB_KEY] = tab
    }

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

    fun removeRumourBookmark(rumourId: Int) {
        viewModelScope.launch {
            shouldDisplayUndoBookmark = true
            lastRemovedBookmark = BookmarkElement.Rumour(rumourId)
            userDataRepository.setRumourBookmarked(rumourId, false)
        }
    }

    fun undoBookmarkRemoval() {
        viewModelScope.launch {
            when (val removed = lastRemovedBookmark) {
                is BookmarkElement.Article -> userDataRepository.setArticleBookmarked(removed.id, true)
                is BookmarkElement.Transfer -> userDataRepository.setTransferBookmarked(removed.id, true)
                is BookmarkElement.Rumour -> userDataRepository.setRumourBookmarked(removed.id, true)
                null -> Unit
            }
            clearUndoState()
        }
    }

    fun clearUndoState() {
        shouldDisplayUndoBookmark = false
        lastRemovedBookmark = null
    }
}

/** The tabs of the saved screen. */
enum class SavedTab { ARTICLES, TRANSFERS, RUMOURS }

sealed interface BookmarksUiState {
    data object Loading : BookmarksUiState
    data class Success(
        val tab: SavedTab,
        val articles: List<UserArticle>,
        val transfers: List<UserTransfer>,
        val rumours: List<Rumour>,
    ) : BookmarksUiState
}

sealed class BookmarkElement(open val id: Int) {
    data class Article(override val id: Int) : BookmarkElement(id)
    data class Transfer(override val id: Int) : BookmarkElement(id)
    data class Rumour(override val id: Int) : BookmarkElement(id)
}

private const val TAB_KEY = "tab"
