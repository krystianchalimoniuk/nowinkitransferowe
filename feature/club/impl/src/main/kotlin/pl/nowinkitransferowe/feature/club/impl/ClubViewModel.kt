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
import kotlinx.datetime.Clock
import kotlinx.datetime.todayIn
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
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.UserTransfer

/** The tabs of the club screen. */
enum class ClubTab { FINANCES, ARRIVALS, DEPARTURES, RUMOURS }

sealed interface ClubUiState {
    data object Loading : ClubUiState

    /** The club was deleted or is not synced yet. */
    data object NotFound : ClubUiState

    /**
     * The [club] with its [country] and [league] names and its spending [seasons], newest first.
     * [arrivals] and [departures] are of the picked [season] (all of them when it is null), newest
     * first; [rumours] are its active ones, as on the board.
     */
    data class Success(
        val club: Club,
        val country: Country?,
        val league: Topic?,
        val tab: ClubTab,
        val seasons: List<ClubSeason>,
        val availableSeasons: List<Season>,
        val season: Season?,
        val arrivals: List<UserTransfer>,
        val departures: List<UserTransfer>,
        val rumours: List<Rumour>,
        val transferCount: Int,
    ) : ClubUiState {
        /** Spending of the picked season, or of all seasons together. */
        val finances: ClubSeason?
            get() = if (season != null) {
                seasons.find { it.season == season }
            } else {
                seasons.takeIf { it.isNotEmpty() }?.let { all ->
                    ClubSeason(
                        season = all.first().season,
                        spent = all.sumOf { it.spent },
                        earned = all.sumOf { it.earned },
                        balance = all.sumOf { it.balance },
                        arrivals = all.flatMap { it.arrivals },
                        departures = all.flatMap { it.departures },
                    )
                }
            }
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

    /** Start year of the picked season, [ALL_SEASONS] for all of them, null before a pick. */
    private val pickedSeason = savedStateHandle.getStateFlow<Int?>(SEASON_KEY, null)

    private val currentSeason = Season.of(Clock.System.todayIn(Season.TIME_ZONE))

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
            combine(tab, pickedSeason, transfers, dictionary) { tab, picked, (transfers, rumours, seasons), (countries, topics) ->
                val available = (transfers.map { it.season } + seasons.map { it.season }).distinct().sortedDescending()
                // The current season until another one (or all of them) is picked. Transfers
                // already agreed for the next window make a later season, which is not picked
                // by itself; without transfers this season, the latest earlier one is.
                val season = when (picked) {
                    null -> available.firstOrNull { it <= currentSeason } ?: available.firstOrNull()
                    ALL_SEASONS -> null
                    else -> Season(picked)
                }
                val shown = if (season == null) transfers else transfers.filter { it.season == season }
                val now = Clock.System.now()
                ClubUiState.Success(
                    club = club,
                    country = countries.find { it.code == club.country },
                    league = topics.find { it.slug == club.topic },
                    tab = tab,
                    seasons = seasons.sortedByDescending { it.season },
                    availableSeasons = available,
                    season = season,
                    arrivals = shown.filter { it.to.isClub(clubId) },
                    departures = shown.filter { it.from.isClub(clubId) },
                    rumours = rumours.filter { it.isFresh(now) },
                    transferCount = transfers.size,
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

    /** Picks [season], or all seasons when it is null. */
    fun selectSeason(season: Season?) {
        savedStateHandle[SEASON_KEY] = season?.startYear ?: ALL_SEASONS
    }

    private fun ClubRef?.isClub(id: Int) = this is ClubRef.Club && this.id == id

    @AssistedFactory
    interface Factory {
        fun create(clubId: Int): ClubViewModel
    }

    private companion object {
        const val TAB_KEY = "tab"
        const val SEASON_KEY = "season"
        const val ALL_SEASONS = 0
    }
}
