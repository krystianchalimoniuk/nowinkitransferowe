/*
 * Copyright 2024 The Android Open Source Project
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

package pl.nowinkitransferowe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.TopicKind
import pl.nowinkitransferowe.core.model.UserData
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    clubRepository: ClubRepository,
    dictionaryRepository: DictionaryRepository,
) : ViewModel() {

    /**
     * Whether the clubs of the leagues were on the phone when the app started. On the first start
     * they are not (the first sync brings them later), so "Wybierz swoje kluby" waits for the next
     * start instead of a spinner; checked once, so it never pops up while the app is in use.
     */
    private val clubsReadyAtStart = viewModelScope.async(start = CoroutineStart.LAZY) {
        val leagues = dictionaryRepository.observeTopics().first()
            .filter { it.kind == TopicKind.LEAGUE }
            .mapTo(mutableSetOf()) { it.slug }
        leagues.isNotEmpty() && clubRepository.observeClubsInLeagues(leagues).first().isNotEmpty()
    }

    val uiState: StateFlow<MainActivityUiState> = flow {
        val clubsReady = clubsReadyAtStart.await()
        emitAll(
            userDataRepository.userData.map {
                MainActivityUiState.Success(it, showClubsOnboarding = clubsReady && !it.isClubsOnboardingShown)
            },
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(
        val userData: UserData,
        /** "Wybierz swoje kluby": not done yet and the clubs were there at the start. */
        val showClubsOnboarding: Boolean = false,
    ) : MainActivityUiState
}
