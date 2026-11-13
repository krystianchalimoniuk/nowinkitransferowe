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

package pl.nowinkitransferowe.feature.player.impl

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.testing.data.lewandowskiTestData
import pl.nowinkitransferowe.core.testing.data.polandTestData
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestPlayerRepository
import pl.nowinkitransferowe.core.testing.repository.TestRumourRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PlayerViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val playerRepository = TestPlayerRepository()
    private val transferRepository = TestTransferRepository()
    private val rumourRepository = TestRumourRepository()
    private val dictionaryRepository = TestDictionaryRepository()
    private val userDataRepository = TestUserDataRepository()

    private fun viewModel(playerId: Int) = PlayerViewModel(
        playerRepository = playerRepository,
        userTransferRepository = CompositeUserTransferRepository(transferRepository, userDataRepository),
        rumourRepository = rumourRepository,
        dictionaryRepository = dictionaryRepository,
        playerId = playerId,
    )

    private val playerId = transfersTestData[0].player.id

    @Before
    fun sendData() {
        playerRepository.sendPlayers(listOf(lewandowskiTestData.copy(id = playerId)))
        transferRepository.sendTransfers(transfersTestData)
        val rumour = rumoursTestData[0].copy(player = rumoursTestData[0].player.copy(id = playerId))
        rumourRepository.sendRumours(listOf(rumour, rumour.copy(id = 99, status = RumourStatus.FAILED)))
        dictionaryRepository.sendCountries(listOf(polandTestData))
        userDataRepository.setUserData(emptyUserData)
    }

    @Test
    fun missingPlayer_isNotFound() = runTest {
        val viewModel = viewModel(playerId = 1234)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        assertEquals(PlayerUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun player_comesWithHisTransfersNewestFirstAndActiveRumours() = runTest {
        val viewModel = viewModel(playerId)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = assertIs<PlayerUiState.Success>(viewModel.uiState.value)
        val expected = transfersTestData.filter { it.player.id == playerId }.sortedByDescending { it.date }
        assertEquals(expected.map { it.id }, state.transfers.map { it.id })
        assertEquals(expected.first().to, state.currentClub)
        assertEquals(listOf(polandTestData), state.countries)
        assertEquals(listOf(rumoursTestData[0].id), state.rumours.map { it.id })
    }
}
