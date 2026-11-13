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

package pl.nowinkitransferowe.feature.transferdetails.impl

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.testing.data.playersTestData
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestPlayerRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class TransferDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val transferRepository = TestTransferRepository()
    private val playerRepository = TestPlayerRepository()

    private fun viewModel(transferId: Int) = TransferDetailsViewModel(
        userDataRepository = userDataRepository,
        userTransferRepository = CompositeUserTransferRepository(transferRepository, userDataRepository),
        playerRepository = playerRepository,
        transferId = transferId,
    )

    @Before
    fun setup() {
        userDataRepository.setUserData(emptyUserData)
        playerRepository.sendPlayers(playersTestData)
    }

    @Test
    fun stateIsLoadingUntilTheTransferIsSynced() = runTest {
        val viewModel = viewModel(19580)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        transferRepository.sendTransfers(emptyList())

        assertEquals(TransferDetailsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun transferHistoryOfThePlayerAndViewedMark() = runTest {
        val viewModel = viewModel(19580)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        transferRepository.sendTransfers(transfersTestData)

        val state = assertIs<TransferDetailsUiState.Success>(viewModel.uiState.value)
        assertEquals(19580, state.transfer.id)
        assertEquals(listOf(19580, 15000), state.history.map { it.id })
        // Łukasz Fabiański is not among the synced players.
        assertNull(state.player)
        assertEquals(setOf(19580), userDataRepository.userData.first().viewedTransfers)
    }

    @Test
    fun syncedPlayerIsShown() = runTest {
        val viewModel = viewModel(14999)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        transferRepository.sendTransfers(transfersTestData)

        val state = assertIs<TransferDetailsUiState.Success>(viewModel.uiState.value)
        assertEquals("Raphael Varane", state.player?.name)
    }

    @Test
    fun bookmarkingSavesTheTransfer() = runTest {
        viewModel(19580).bookmarkTransfer(19580, true)

        assertEquals(setOf(19580), userDataRepository.userData.first().bookmarkedTransfers)
    }
}
