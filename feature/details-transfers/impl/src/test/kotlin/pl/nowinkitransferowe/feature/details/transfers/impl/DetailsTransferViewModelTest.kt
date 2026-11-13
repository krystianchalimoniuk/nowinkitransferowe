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

package pl.nowinkitransferowe.feature.details.transfers.impl

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.model.mapToUserTransfers
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.core.testing.util.TestImageDownloader
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class DetailsTransferViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val transferRepository = TestTransferRepository()

    private lateinit var viewModel: DetailsTransferViewModel

    @Before
    fun setup() {
        userDataRepository.setUserData(emptyUserData)
        viewModel = DetailsTransferViewModel(
            userDataRepository = userDataRepository,
            userTransferRepository = CompositeUserTransferRepository(transferRepository, userDataRepository),
            imageDownloader = TestImageDownloader(),
            transferResourceId = transfersTestData.first().id.toString(),
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(DetailsTransferUiState.Loading, viewModel.detailsTransferUiState.value)
    }

    @Test
    fun whenEntityExist_uiStateHasEveryTransferOfThePlayer() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.detailsTransferUiState.collect() }
        transferRepository.sendTransfers(transfersTestData)

        // Łukasz Fabiański: 19580 (free, 2026) and 15000 (8 mln €, 2018); both are now viewed.
        val playerTransfers = transfersTestData.filter { it.player.id == 832 }
        val expected = DetailsTransferUiState.Success(
            userTransferResource = playerTransfers.mapToUserTransfers(
                emptyUserData.copy(viewedTransferResources = setOf("19580", "15000")),
            ),
            dataPoints = playerTransfers.sortedBy { it.date }.map {
                DataPoint(
                    date = Util.shortcutDate(Util.dateFormatted(it.date)),
                    price = Util.priceToFloat(it.fee.label),
                    bitmap = null,
                )
            },
        )
        assertEquals(expected = expected, actual = viewModel.detailsTransferUiState.value)
    }
}
