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

package pl.nowinkitransferowe.feature.transfers.impl

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.model.mapToUserTransfers
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.core.testing.util.TestSyncManager
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import pl.nowinkitransferowe.feature.transfers.impl.navigation.TransfersViewModel
import kotlin.test.assertEquals

class TransfersViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val syncManager = TestSyncManager()
    private val userDataRepository = TestUserDataRepository()
    private val transferRepository = TestTransferRepository()

    private val userTransferRepository = CompositeUserTransferRepository(
        transferRepository = transferRepository,
        userDataRepository = userDataRepository,
    )

    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: TransfersViewModel

    @Before
    fun setup() {
        userDataRepository.setUserData(emptyUserData)
        viewModel = TransfersViewModel(
            syncManager = syncManager,
            savedStateHandle = savedStateHandle,
            userDataRepository = userDataRepository,
            userTransferRepository = userTransferRepository,
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(TransferFeedUiState.Loading, viewModel.feedUiState.value)
    }

    @Test
    fun stateIsLoadingWhenAppIsSyncingWithNoTransfers() = runTest {
        syncManager.setSyncing(true)

        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isSyncing.collect() }

        assertEquals(true, viewModel.isSyncing.value)
    }

    @Test
    fun whenUpdateTransferSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        viewModel.updateTransferSaved(1, true)

        assertEquals(setOf(1), userDataRepository.userData.first().bookmarkedTransfers)

        viewModel.updateTransferSaved(1, false)

        assertEquals(emptySet(), userDataRepository.userData.first().bookmarkedTransfers)
    }

    @Test
    fun transfersUpdatesAfterLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedUiState.collect() }
        transferRepository.sendTransfers(transfersTestData)

        viewModel.updateTransferSaved(transferId = 15000, isChecked = true)

        val userData = emptyUserData.copy(bookmarkedTransfers = setOf(15000))
        assertEquals(
            TransferFeedUiState.Success(feed = transfersTestData.take(TransfersViewModel.PAGE_SIZE).mapToUserTransfers(userData)),
            viewModel.feedUiState.value,
        )
    }

    @Test
    fun whenLoadNexPageFunctionIsCalled_transfersUpdates() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.feedUiState.collect() }
        transferRepository.sendTransfers(transfersTestData)

        viewModel.loadNextPage(viewModel.page.value, transfersTestData.size)

        assertEquals(
            TransferFeedUiState.Success(feed = transfersTestData.take(2 * TransfersViewModel.PAGE_SIZE).mapToUserTransfers(emptyUserData)),
            viewModel.feedUiState.value,
        )
    }

    @Test
    fun transfersMarkedAsViewedAreSaved() = runTest {
        viewModel.setTransfersViewed(listOf(19580, 15000), true)

        assertEquals(setOf(19580, 15000), userDataRepository.userData.first().viewedTransfers)
    }
}
