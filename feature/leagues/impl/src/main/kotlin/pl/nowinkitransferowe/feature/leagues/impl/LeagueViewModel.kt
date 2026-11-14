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

package pl.nowinkitransferowe.feature.leagues.impl

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
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.LeaguesRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.League
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.UserTransfer

sealed interface LeagueUiState {
    data object Loading : LeagueUiState

    /** The league is not in the stored season, for example offline on the first visit. */
    data object NotFound : LeagueUiState

    /**
     * The [league] of [season] with its [clubs] by id, its most expensive [topArrivals] and
     * [topDepartures], and [allLeagues] to compare it with, named by [names].
     */
    data class Success(
        val season: Season,
        val league: League,
        val allLeagues: List<League>,
        val names: Map<String, String>,
        val clubs: Map<Int, Club>,
        val topArrivals: List<UserTransfer>,
        val topDepartures: List<UserTransfer>,
    ) : LeagueUiState {
        val name: String get() = names[league.topic] ?: league.topic
    }
}

@HiltViewModel(assistedFactory = LeagueViewModel.Factory::class)
class LeagueViewModel @AssistedInject constructor(
    private val leaguesRepository: LeaguesRepository,
    dictionaryRepository: DictionaryRepository,
    clubRepository: ClubRepository,
    userTransferRepository: UserTransferRepository,
    @Assisted("topic") val topic: String,
    @Assisted("season") seasonStartYear: Int,
) : ViewModel() {

    private val season = Season(seasonStartYear)

    val uiState: StateFlow<LeagueUiState> = leaguesRepository.observeLeagues(season)
        .flatMapLatest { leagues ->
            val league = leagues.find { it.topic == topic } ?: return@flatMapLatest flowOf(LeagueUiState.NotFound)
            combine(
                dictionaryRepository.observeTopics(),
                clubRepository.observeClubs(league.clubs.map { it.clubId }.toSet()),
                userTransferRepository.observeTransfers(league.topArrivals.toSet()),
                userTransferRepository.observeTransfers(league.topDepartures.toSet()),
            ) { topics, clubs, arrivals, departures ->
                LeagueUiState.Success(
                    season = season,
                    league = league,
                    allLeagues = leagues,
                    names = topics.associate { it.slug to it.name },
                    clubs = clubs.associateBy(Club::id),
                    // In the server's order: the most expensive first
                    topArrivals = league.topArrivals.mapNotNull { id -> arrivals.find { it.id == id } },
                    topDepartures = league.topDepartures.mapNotNull { id -> departures.find { it.id == id } },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeagueUiState.Loading)

    init {
        viewModelScope.launch { leaguesRepository.refresh(season) }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("topic") topic: String, @Assisted("season") seasonStartYear: Int): LeagueViewModel
    }
}
