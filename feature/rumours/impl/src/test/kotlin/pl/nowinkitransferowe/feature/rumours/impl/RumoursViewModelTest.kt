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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.testing.data.rumourStatsTestData
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestRumourRepository
import pl.nowinkitransferowe.core.testing.repository.TestRumourStatsRepository
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.days

class RumoursViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val rumourRepository = TestRumourRepository()
    private val statsRepository = TestRumourStatsRepository()
    private val dictionaryRepository = TestDictionaryRepository()
    private lateinit var viewModel: RumoursViewModel

    @Before
    fun setup() {
        viewModel = RumoursViewModel(SavedStateHandle(), rumourRepository, statsRepository, dictionaryRepository)
    }

    private fun success() = assertIs<RumoursUiState.Success>(viewModel.uiState.value)

    private fun sendData() {
        val old = rumoursTestData[0].copy(id = 99, updatedAt = rumoursTestData[0].updatedAt - 60.days)
        val failed = rumoursTestData[0].copy(id = 98, status = RumourStatus.FAILED)
        rumourRepository.sendRumours(rumoursTestData + old + failed)
        dictionaryRepository.sendTopics(topicsTestData)
        statsRepository.sendStats(rumourStatsTestData)
    }

    @Test
    fun stateIsInitiallyLoading_andStatsAreRefreshed() = runTest {
        assertEquals(RumoursUiState.Loading, viewModel.uiState.value)
        assertEquals(1, statsRepository.refreshCount)
    }

    @Test
    fun board_showsActiveRumoursOfTheLast45Days() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        sendData()

        assertEquals(rumoursTestData.map { it.id }, success().rumours.map { it.id })
        assertEquals(rumourStatsTestData, success().stats)
        assertEquals(topicsTestData.filter { it.kind == pl.nowinkitransferowe.core.model.TopicKind.LEAGUE }, success().leagues)
    }

    @Test
    fun selectStage_filtersRumours() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        sendData()

        viewModel.selectStage(RumourStage.SPECULATION)

        assertEquals(RumourStage.SPECULATION, success().stage)
        assertEquals(listOf(4), success().rumours.map { it.id })

        viewModel.selectStage(null)
        assertEquals(2, success().rumours.size)
    }

    @Test
    fun selectTopic_filtersRumoursByLeague() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        rumourRepository.clubTopics = mapOf(2 to "saudi-pro-league")
        sendData()

        viewModel.selectTopic("saudi-pro-league")

        assertEquals("saudi-pro-league", success().topic)
        assertEquals(listOf(5), success().rumours.map { it.id })
    }
}
