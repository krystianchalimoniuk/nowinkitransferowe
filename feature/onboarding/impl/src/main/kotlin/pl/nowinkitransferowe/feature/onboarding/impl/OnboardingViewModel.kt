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

package pl.nowinkitransferowe.feature.onboarding.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.TopicKind
import javax.inject.Inject

sealed interface OnboardingUiState {
    /** The leagues and their clubs are not on the phone yet, e.g. during the first sync. */
    data object Loading : OnboardingUiState

    /** The [clubs] of the picked [league] out of [leagues], and the [picked] club ids. */
    data class Success(
        val leagues: List<Topic>,
        val league: String,
        val clubs: List<Club>,
        val picked: Set<Int>,
    ) : OnboardingUiState
}

/**
 * "Wybierz swoje kluby": the clubs of the leagues to pick from, one league at a time. The picked
 * clubs become followed when the user is done; skipping follows nothing. Either way the screen
 * does not come again.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    clubRepository: ClubRepository,
    dictionaryRepository: DictionaryRepository,
) : ViewModel() {

    private val pickedLeague = savedStateHandle.getStateFlow<String?>(LEAGUE_KEY, null)
    private val picked = savedStateHandle.getStateFlow(PICKED_KEY, intArrayOf())

    // Ekstraklasa first, as for most readers it is the home league
    private val leagues = dictionaryRepository.observeTopics()
        .map { topics -> topics.filter { it.kind == TopicKind.LEAGUE }.sortedBy { if (it.slug == HOME_LEAGUE) 0 else 1 } }

    val uiState: StateFlow<OnboardingUiState> = leagues
        .flatMapLatest { leagues ->
            clubRepository.observeClubsInLeagues(leagues.mapTo(mutableSetOf()) { it.slug }).map { clubs -> leagues to clubs }
        }
        .let { leaguesAndClubs -> combine(leaguesAndClubs, pickedLeague, picked, ::Triple) }
        .map { (leaguesAndClubs, pickedLeague, picked) ->
            val (leagues, clubs) = leaguesAndClubs
            val withClubs = leagues.filter { league -> clubs.any { it.topic == league.slug } }
            if (withClubs.isEmpty()) return@map OnboardingUiState.Loading
            val league = pickedLeague?.takeIf { slug -> withClubs.any { it.slug == slug } } ?: withClubs.first().slug
            OnboardingUiState.Success(
                leagues = withClubs,
                league = league,
                clubs = clubs.filter { it.topic == league },
                picked = picked.toSet(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingUiState.Loading)

    fun selectLeague(slug: String) {
        savedStateHandle[LEAGUE_KEY] = slug
    }

    fun togglePicked(clubId: Int) {
        val current = picked.value.toSet()
        savedStateHandle[PICKED_KEY] = (if (clubId in current) current - clubId else current + clubId).toIntArray()
    }

    /** Follows the picked clubs; [skip] follows none. */
    fun finish(skip: Boolean = false) {
        val clubs = if (skip) emptySet() else picked.value.toSet()
        viewModelScope.launch { userDataRepository.finishClubsOnboarding(clubs) }
    }

    private companion object {
        const val LEAGUE_KEY = "league"
        const val PICKED_KEY = "picked"
        const val HOME_LEAGUE = "ekstraklasa"
    }
}
