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

package pl.nowinkitransferowe.feature.club.impl

import androidx.lifecycle.SavedStateHandle
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
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.ClubSeasonsRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.ClubSeason
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.UserTransfer

/** The tabs of the club screen. */
enum class ClubTab { FINANCES, ARRIVALS, DEPARTURES, RUMOURS }

sealed interface ClubUiState {
    data object Loading : ClubUiState

    /** The club was deleted or is not synced yet. */
    data object NotFound : ClubUiState

    /**
     * The [club] with its [country] and [league] names, its [seasons] newest first, and its
     * transfers in and out and rumours, newest first.
     */
    data class Success(
        val club: Club,
        val country: Country?,
        val league: Topic?,
        val tab: ClubTab,
        val seasons: List<ClubSeason>,
        val arrivals: List<UserTransfer>,
        val departures: List<UserTransfer>,
        val rumours: List<Rumour>,
    ) : ClubUiState {
        val transferCount: Int get() = arrivals.size + departures.size
    }
}

@HiltViewModel(assistedFactory = ClubViewModel.Factory::class)
class ClubViewModel @AssistedInject constructor(
    private val savedStateHandle: SavedStateHandle,
    clubRepository: ClubRepository,
    private val clubSeasonsRepository: ClubSeasonsRepository,
    userTransferRepository: UserTransferRepository,
    rumourRepository: RumourRepository,
    dictionaryRepository: DictionaryRepository,
    @Assisted val clubId: Int,
) : ViewModel() {

    private val tab = savedStateHandle.getStateFlow(TAB_KEY, ClubTab.FINANCES)

    private val transfers = combine(
        userTransferRepository.observeTransfersOfClub(clubId),
        rumourRepository.observeRumoursOfClub(clubId),
        clubSeasonsRepository.observeClubSeasons(clubId),
        ::Triple,
    )

    private val dictionary = combine(dictionaryRepository.observeCountries(), dictionaryRepository.observeTopics(), ::Pair)

    val uiState: StateFlow<ClubUiState> = clubRepository.observeClub(clubId)
        .flatMapLatest { club ->
            if (club == null) return@flatMapLatest flowOf(ClubUiState.NotFound)
            combine(tab, transfers, dictionary) { tab, (transfers, rumours, seasons), (countries, topics) ->
                ClubUiState.Success(
                    club = club,
                    country = countries.find { it.code == club.country },
                    league = topics.find { it.slug == club.topic },
                    tab = tab,
                    seasons = seasons.sortedByDescending { it.season },
                    arrivals = transfers.filter { it.to.isClub(clubId) },
                    departures = transfers.filter { it.from.isClub(clubId) },
                    rumours = rumours,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClubUiState.Loading)

    init {
        // The seasons have no change list, so they are fetched when the screen opens
        viewModelScope.launch { clubSeasonsRepository.refresh(clubId) }
    }

    fun selectTab(tab: ClubTab) {
        savedStateHandle[TAB_KEY] = tab
    }

    private fun ClubRef?.isClub(id: Int) = this is ClubRef.Club && this.id == id

    @AssistedFactory
    interface Factory {
        fun create(clubId: Int): ClubViewModel
    }

    private companion object {
        const val TAB_KEY = "tab"
    }
}
