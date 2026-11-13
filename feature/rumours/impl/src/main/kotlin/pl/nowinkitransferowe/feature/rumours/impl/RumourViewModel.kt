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

package pl.nowinkitransferowe.feature.rumours.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Rumour

sealed interface RumourUiState {
    data object Loading : RumourUiState

    /** The rumour was deleted or is not synced yet. */
    data object NotFound : RumourUiState

    /**
     * The rumour with the facts about its [player] and the [articles] that moved it, by id.
     */
    data class Success(
        val rumour: Rumour,
        val isSaved: Boolean,
        val player: Player?,
        val articles: Map<Int, Article>,
    ) : RumourUiState {
        val latestArticle: Article? get() = rumour.latestArticleId?.let(articles::get)
    }
}

@HiltViewModel(assistedFactory = RumourViewModel.Factory::class)
class RumourViewModel @AssistedInject constructor(
    rumourRepository: RumourRepository,
    articleRepository: ArticleRepository,
    playerRepository: PlayerRepository,
    private val userDataRepository: UserDataRepository,
    @Assisted val rumourId: Int,
) : ViewModel() {

    val uiState: StateFlow<RumourUiState> = rumourRepository.observeRumour(rumourId)
        .flatMapLatest { rumour ->
            if (rumour == null) return@flatMapLatest flowOf(RumourUiState.NotFound)
            val articleIds = (listOfNotNull(rumour.latestArticleId) + rumour.history.mapNotNull { it.articleId }).toSet()
            combine(
                userDataRepository.userData,
                playerRepository.observePlayer(rumour.player.id),
                articleRepository.observeArticles(articleIds),
            ) { userData, player, articles ->
                RumourUiState.Success(
                    rumour = rumour,
                    isSaved = rumour.id in userData.bookmarkedRumours,
                    player = player,
                    articles = articles.associateBy(Article::id),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RumourUiState.Loading)

    fun bookmarkRumour(bookmarked: Boolean) {
        viewModelScope.launch { userDataRepository.setRumourBookmarked(rumourId, bookmarked) }
    }

    @AssistedFactory
    interface Factory {
        fun create(rumourId: Int): RumourViewModel
    }
}
