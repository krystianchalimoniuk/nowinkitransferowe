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
import kotlinx.datetime.Clock
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.UserTransfer

sealed interface PlayerUiState {
    data object Loading : PlayerUiState

    /** The player was deleted or is not synced yet. */
    data object NotFound : PlayerUiState

    /**
     * The [player] with his [countries] (first and second nationality), [transfers] newest first
     * and his active [rumours].
     */
    data class Success(
        val player: Player,
        val countries: List<Country>,
        val transfers: List<UserTransfer>,
        val rumours: List<Rumour>,
    ) : PlayerUiState {
        /** Where the latest transfer took him, as on the website. */
        val currentClub: ClubRef? get() = transfers.firstOrNull()?.to
    }
}

@HiltViewModel(assistedFactory = PlayerViewModel.Factory::class)
class PlayerViewModel @AssistedInject constructor(
    playerRepository: PlayerRepository,
    userTransferRepository: UserTransferRepository,
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
            ) { transfers, rumours, countries ->
                val now = Clock.System.now()
                PlayerUiState.Success(
                    player = player,
                    countries = listOfNotNull(player.nationality, player.nationality2).mapNotNull { code -> countries.find { it.code == code } },
                    transfers = transfers.sortedWith(compareByDescending<UserTransfer> { it.date }.thenByDescending { it.id }),
                    rumours = rumours.filter { it.isFresh(now) },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerUiState.Loading)

    @AssistedFactory
    interface Factory {
        fun create(playerId: Int): PlayerViewModel
    }
}
