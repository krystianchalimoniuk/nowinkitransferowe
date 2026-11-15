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

package pl.nowinkitransferowe.feature.followed.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.Player
import javax.inject.Inject

/** A followed [club] with the name of its [league]. */
data class FollowedClub(val club: Club, val league: String?)

sealed interface FollowedUiState {
    data object Loading : FollowedUiState

    /** The followed [clubs] and [players] by name. Ones not synced yet are left out. */
    data class Success(
        val clubs: List<FollowedClub>,
        val players: List<Player>,
    ) : FollowedUiState {
        fun isEmpty() = clubs.isEmpty() && players.isEmpty()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FollowedViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    clubRepository: ClubRepository,
    playerRepository: PlayerRepository,
    dictionaryRepository: DictionaryRepository,
) : ViewModel() {

    private val followed = userDataRepository.userData
        .map { it.followedClubs to it.followedPlayers }
        .distinctUntilChanged()

    val uiState: StateFlow<FollowedUiState> = followed
        .flatMapLatest { (clubIds, playerIds) ->
            combine(
                clubRepository.observeClubs(clubIds),
                playerRepository.observePlayers(playerIds),
                dictionaryRepository.observeTopics(),
            ) { clubs, players, topics ->
                FollowedUiState.Success(
                    clubs = clubs
                        .sortedBy { it.name.lowercase() }
                        .map { club -> FollowedClub(club, topics.find { it.slug == club.topic }?.name) },
                    players = players.sortedBy { it.name.lowercase() },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FollowedUiState.Loading)

    fun setClubFollowed(clubId: Int, followed: Boolean) {
        viewModelScope.launch { userDataRepository.setClubFollowed(clubId, followed) }
    }

    fun setPlayerFollowed(playerId: Int, followed: Boolean) {
        viewModelScope.launch { userDataRepository.setPlayerFollowed(playerId, followed) }
    }
}
