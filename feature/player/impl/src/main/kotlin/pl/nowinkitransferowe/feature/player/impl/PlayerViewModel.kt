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

package pl.nowinkitransferowe.feature.player.impl

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
import kotlinx.datetime.Clock
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.UserArticleRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.FeeKind
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.model.UserTransfer

sealed interface PlayerUiState {
    data object Loading : PlayerUiState

    /** The player was deleted or is not synced yet. */
    data object NotFound : PlayerUiState

    /**
     * The [player] with his [countries] (first and second nationality), [transfers] newest first,
     * his active [rumours] and the [articles] about him, newest first.
     */
    data class Success(
        val player: Player,
        val countries: List<Country>,
        val transfers: List<UserTransfer>,
        val rumours: List<Rumour>,
        val articles: List<UserArticle> = emptyList(),
    ) : PlayerUiState {
        /** Where the latest transfer took him, as on the website. */
        val currentClub: ClubRef? get() = transfers.firstOrNull()?.to

        /** All fees together; loans and undisclosed fees count as 0, like on the website. */
        val totalMillions: Double get() = transfers.sumOf { it.fee.countedMillions }

        val highestMillions: Double get() = transfers.maxOfOrNull { it.fee.countedMillions } ?: 0.0

        /**
         * The transfers on the chart, oldest first: those with a fee and free ones at 0. Loans and
         * undisclosed fees stay in the history only, like on the website.
         */
        val chartTransfers: List<UserTransfer>
            get() = transfers
                .filter { it.fee.kind == FeeKind.AMOUNT || it.fee.kind == FeeKind.FREE }
                .sortedWith(compareBy<UserTransfer> { it.date }.thenBy { it.id })

        /** A line needs at least two points, and at least one fee to say something. */
        val showChart: Boolean get() = chartTransfers.size >= 2 && chartTransfers.any { it.fee.countedMillions > 0 }
    }
}

@HiltViewModel(assistedFactory = PlayerViewModel.Factory::class)
class PlayerViewModel @AssistedInject constructor(
    playerRepository: PlayerRepository,
    userTransferRepository: UserTransferRepository,
    userArticleRepository: UserArticleRepository,
    private val userDataRepository: UserDataRepository,
    rumourRepository: RumourRepository,
    dictionaryRepository: DictionaryRepository,
    @Assisted val playerId: Int,
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = playerRepository.observePlayer(playerId)
        .flatMapLatest { player ->
            if (player == null) return@flatMapLatest flowOf(PlayerUiState.NotFound)
            combine(
                userTransferRepository.observeTransfersOfPlayer(playerId),
                rumourRepository.observeRumoursOfPlayer(playerId),
                dictionaryRepository.observeCountries(),
                userArticleRepository.observeArticlesAboutPlayer(playerId),
            ) { transfers, rumours, countries, articles ->
                val now = Clock.System.now()
                PlayerUiState.Success(
                    player = player,
                    countries = listOfNotNull(player.nationality, player.nationality2).mapNotNull { code -> countries.find { it.code == code } },
                    transfers = transfers.sortedWith(compareByDescending<UserTransfer> { it.date }.thenByDescending { it.id }),
                    rumours = rumours.filter { it.isFresh(now) },
                    articles = articles.take(MAX_ARTICLES),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerUiState.Loading)

    fun updateArticleSaved(articleId: Int, saved: Boolean) {
        viewModelScope.launch { userDataRepository.setArticleBookmarked(articleId, saved) }
    }

    @AssistedFactory
    interface Factory {
        fun create(playerId: Int): PlayerViewModel
    }

    private companion object {
        const val MAX_ARTICLES = 10
    }
}
