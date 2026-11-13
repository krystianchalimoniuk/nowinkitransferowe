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

package pl.nowinkitransferowe.feature.articledetails.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.UserArticleRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.UserArticle

@HiltViewModel(assistedFactory = ArticleDetailsViewModel.Factory::class)
class ArticleDetailsViewModel @AssistedInject constructor(
    private val userDataRepository: UserDataRepository,
    userArticleRepository: UserArticleRepository,
    clubRepository: ClubRepository,
    playerRepository: PlayerRepository,
    rumourRepository: RumourRepository,
    @Assisted val articleId: Int,
) : ViewModel() {

    // Waits while the article is not in the database yet, for example a link opened before the
    // first sync.
    private val article = userArticleRepository.observeArticle(articleId)
        .filterNotNull()
        .onEach { if (!it.hasBeenViewed) setArticleViewed(it.id) }

    val detailsUiState: StateFlow<ArticleDetailsUiState> = article
        .flatMapLatest { article ->
            combine(
                clubRepository.observeClubs(article.clubIds.toSet()),
                playerRepository.observePlayers(article.playerIds.toSet()),
                rumourRepository.observeRumoursOfArticle(article.id),
            ) { clubs, players, rumours ->
                ArticleDetailsUiState.Success(
                    userArticle = article,
                    // In the order the article lists them; stubs are left out until synced.
                    clubs = article.clubIds.mapNotNull { id -> clubs.find { it.id == id } },
                    players = article.playerIds.mapNotNull { id -> players.find { it.id == id } },
                    rumour = rumours.firstOrNull(),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArticleDetailsUiState.Loading)

    val darkThemeConfig = userDataRepository.userData
        .map { it.darkThemeConfig }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DarkThemeConfig.FOLLOW_SYSTEM)

    fun bookmarkArticle(articleId: Int, bookmarked: Boolean) {
        viewModelScope.launch { userDataRepository.setArticleBookmarked(articleId, bookmarked) }
    }

    private fun setArticleViewed(articleId: Int) {
        viewModelScope.launch { userDataRepository.setArticleViewed(articleId, true) }
    }

    @AssistedFactory
    interface Factory {
        fun create(articleId: Int): ArticleDetailsViewModel
    }
}

sealed interface ArticleDetailsUiState {
    /**
     * The article with its synced [clubs] and [players], and the [rumour] it moved to a stage, if
     * any.
     */
    data class Success(
        val userArticle: UserArticle,
        val clubs: List<Club> = emptyList(),
        val players: List<Player> = emptyList(),
        val rumour: Rumour? = null,
    ) : ArticleDetailsUiState

    data object Loading : ArticleDetailsUiState
}
