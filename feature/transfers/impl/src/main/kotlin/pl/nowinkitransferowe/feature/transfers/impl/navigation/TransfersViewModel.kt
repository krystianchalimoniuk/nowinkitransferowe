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

package pl.nowinkitransferowe.feature.transfers.impl.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.todayIn
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.TopicKind
import pl.nowinkitransferowe.core.model.UserTransfer
import javax.inject.Inject

sealed interface TransfersUiState {
    data object Loading : TransfersUiState

    /** Transfers of [season], of the league [topic] when one is picked, newest first. */
    data class Success(
        val season: Season,
        val seasons: List<Season>,
        val topic: String?,
        val leagues: List<Topic>,
        val transfers: List<UserTransfer>,
        val hasMore: Boolean,
    ) : TransfersUiState
}

@HiltViewModel
class TransfersViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    syncManager: SyncManager,
    userTransferRepository: UserTransferRepository,
    dictionaryRepository: DictionaryRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    /** From the current season back to 17/18, the first season with transfers on the website. */
    private val seasons: List<Season> = generateSequence(Season.of(Clock.System.todayIn(Season.TIME_ZONE))) { it.previous() }
        .takeWhile { it.startYear >= FIRST_SEASON }
        .toList()

    private val seasonStart = savedStateHandle.getStateFlow(SEASON_KEY, seasons.first().startYear)
    private val topic = savedStateHandle.getStateFlow<String?>(TOPIC_KEY, null)
    private val page = MutableStateFlow(1)

    val selectedTransferId: StateFlow<Int?> = savedStateHandle.getStateFlow(SELECTED_TRANSFER_ID_KEY, null)

    val isSyncing = syncManager.isSyncing.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val uiState: StateFlow<TransfersUiState> = combine(seasonStart, topic, page, ::Triple)
        .flatMapLatest { (start, topic, page) ->
            val season = Season(start)
            val limit = PAGE_SIZE * page
            combine(
                userTransferRepository.observeTransfers(season = season, topic = topic, limit = limit),
                dictionaryRepository.observeTopics(),
            ) { transfers, topics ->
                TransfersUiState.Success(
                    season = season,
                    seasons = seasons,
                    topic = topic,
                    leagues = topics.filter { it.kind == TopicKind.LEAGUE },
                    transfers = transfers,
                    hasMore = transfers.size == limit,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransfersUiState.Loading)

    fun selectSeason(season: Season) {
        savedStateHandle[SEASON_KEY] = season.startYear
        page.value = 1
    }

    fun selectTopic(topic: String?) {
        savedStateHandle[TOPIC_KEY] = topic
        page.value = 1
    }

    fun loadNextPage() {
        val state = uiState.value
        if (state is TransfersUiState.Success && state.hasMore) page.value += 1
    }

    fun onTransferClick(transferId: Int?) {
        savedStateHandle[SELECTED_TRANSFER_ID_KEY] = transferId
    }

    fun updateTransferSaved(transferId: Int, isChecked: Boolean) {
        viewModelScope.launch { userDataRepository.setTransferBookmarked(transferId, isChecked) }
    }

    fun setTransfersViewed(transferIds: List<Int>, viewed: Boolean) {
        viewModelScope.launch { userDataRepository.setTransfersViewed(transferIds, viewed) }
    }

    companion object {
        const val PAGE_SIZE = 20
        private const val FIRST_SEASON = 2017
        private const val SELECTED_TRANSFER_ID_KEY = "selectedTransferId"
        private const val SEASON_KEY = "season"
        private const val TOPIC_KEY = "topic"
    }
}
