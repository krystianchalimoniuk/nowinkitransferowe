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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.LeaguesRepository
import pl.nowinkitransferowe.core.model.League
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.SeasonOption
import javax.inject.Inject

sealed interface LeaguesUiState {
    data object Loading : LeaguesUiState

    /** Nothing stored and the fetch failed, for example offline on the first visit. */
    data object Unavailable : LeaguesUiState

    /**
     * The [leagues] of [season] with their [names] by topic slug; the season switch shows only
     * with two or more [seasons].
     */
    data class Success(
        val season: Season,
        val seasons: List<SeasonOption>,
        val leagues: List<League>,
        val names: Map<String, String>,
    ) : LeaguesUiState
}

@HiltViewModel
class LeaguesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val leaguesRepository: LeaguesRepository,
    dictionaryRepository: DictionaryRepository,
) : ViewModel() {

    private val pickedSeason = savedStateHandle.getStateFlow<Int?>(SEASON_KEY, null)

    private val fetchFailed = MutableStateFlow(false)

    val uiState: StateFlow<LeaguesUiState> = combine(leaguesRepository.observeSeasons(), pickedSeason, fetchFailed, ::Triple)
        .flatMapLatest { (seasons, picked, failed) ->
            // The current season until another one is picked
            val season = picked?.let(::Season) ?: seasons.firstOrNull { it.current }?.season ?: seasons.firstOrNull()?.season
            if (season == null) {
                return@flatMapLatest flowOf(if (failed) LeaguesUiState.Unavailable else LeaguesUiState.Loading)
            }
            combine(leaguesRepository.observeLeagues(season), dictionaryRepository.observeTopics()) { leagues, topics ->
                LeaguesUiState.Success(
                    season = season,
                    seasons = seasons.sortedByDescending { it.season },
                    leagues = leagues,
                    names = topics.associate { it.slug to it.name },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaguesUiState.Loading)

    init {
        // The leagues have no change list, so they are fetched when the screen opens
        viewModelScope.launch { fetchFailed.value = !leaguesRepository.refresh() }
    }

    fun selectSeason(season: Season) {
        savedStateHandle[SEASON_KEY] = season.startYear
        viewModelScope.launch { leaguesRepository.refresh(season) }
    }

    private companion object {
        const val SEASON_KEY = "season"
    }
}
