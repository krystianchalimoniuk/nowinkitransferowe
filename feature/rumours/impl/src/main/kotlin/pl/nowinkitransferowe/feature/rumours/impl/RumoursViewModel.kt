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

package pl.nowinkitransferowe.feature.rumours.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.RumourStatsRepository
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStats
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.TopicKind
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

sealed interface RumoursUiState {
    data object Loading : RumoursUiState

    /**
     * The board as on the website: active rumours that moved in the last 45 days, of the league
     * [topic] and at [stage] when those are picked, and the [stats] of the season.
     */
    data class Success(
        val topic: String?,
        val leagues: List<Topic>,
        val stage: RumourStage?,
        val rumours: List<Rumour>,
        val stats: RumourStats?,
    ) : RumoursUiState
}

@HiltViewModel
class RumoursViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    rumourRepository: RumourRepository,
    private val rumourStatsRepository: RumourStatsRepository,
    dictionaryRepository: DictionaryRepository,
) : ViewModel() {

    private val topic = savedStateHandle.getStateFlow<String?>(TOPIC_KEY, null)
    private val stageLevel = savedStateHandle.getStateFlow<Int?>(STAGE_KEY, null)

    val selectedRumourId: StateFlow<Int?> = savedStateHandle.getStateFlow(SELECTED_RUMOUR_ID_KEY, null)

    private val boardSince = Clock.System.now() - BOARD_DAYS.days

    val uiState: StateFlow<RumoursUiState> = combine(topic, stageLevel, ::Pair)
        .flatMapLatest { (topic, level) ->
            val stage = level?.let(RumourStage::fromLevel)
            combine(
                rumourRepository.observeRumours(RumourStatus.ACTIVE, boardSince, topic, stage),
                dictionaryRepository.observeTopics(),
                rumourStatsRepository.observeRumourStats(),
            ) { rumours, topics, stats ->
                RumoursUiState.Success(
                    topic = topic,
                    leagues = topics.filter { it.kind == TopicKind.LEAGUE },
                    stage = stage,
                    rumours = rumours,
                    stats = stats,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RumoursUiState.Loading)

    init {
        // The stats have no change list, so they are fetched when the board opens
        viewModelScope.launch { rumourStatsRepository.refresh() }
    }

    fun selectTopic(topic: String?) {
        savedStateHandle[TOPIC_KEY] = topic
    }

    fun selectStage(stage: RumourStage?) {
        savedStateHandle[STAGE_KEY] = stage?.level
    }

    fun onRumourClick(rumourId: Int?) {
        savedStateHandle[SELECTED_RUMOUR_ID_KEY] = rumourId
    }

    private companion object {
        /** Like the website, the board shows rumours that moved in the last 45 days. */
        const val BOARD_DAYS = 45
        const val TOPIC_KEY = "topic"
        const val STAGE_KEY = "stage"
        const val SELECTED_RUMOUR_ID_KEY = "selectedRumourId"
    }
}
