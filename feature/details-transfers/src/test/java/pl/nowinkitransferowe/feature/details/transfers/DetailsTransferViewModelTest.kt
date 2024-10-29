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

package pl.nowinkitransferowe.feature.details.transfers

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.invoke
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.testing.data.transferResourceTestData
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.core.testing.util.TestImageDownloader
import pl.nowinkitransferowe.feature.details.transfers.navigation.DetailTransferRoute
import kotlin.test.assertEquals

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 *
 * These tests use Robolectric because the subject under test (the ViewModel) uses
 * `SavedStateHandle.toRoute` which has a dependency on `android.os.Bundle`.
 *
 * TODO: Remove Robolectric if/when AndroidX Navigation API is updated to remove Android dependency.
 *  *  See https://issuetracker.google.com/340966212.
 */
@RunWith(RobolectricTestRunner::class)
class DetailsTransferViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val transferRepository = TestTransferRepository()
    private val imageDownloader = TestImageDownloader()

    private lateinit var viewModel: DetailsTransferViewModel
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(route = DetailTransferRoute(transferId = transferResourceTestData.first().id))

    @Before
    fun setup() {
        viewModel = DetailsTransferViewModel(
            savedStateHandle = savedStateHandle,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            imageDownloader = imageDownloader,
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(DetailsTransferUiState.Loading, viewModel.detailsTransferUiState.value)
    }

//    @Test
//    fun whenEntityIsNotExist_uiStateIsError() = runTest {
//        savedStateHandle[DEEP_LINK_TRANSFER_RESOURCE_ID_KEY] = 11
//        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.detailsTransferUiState.collect() }
//        transferRepository.sendTransferResources(transferResourceTestData)
//        assertEquals(
//            expected = DetailsTransferUiState.Error,
//            actual = viewModel.detailsTransferUiState.value,
//        )
//    }

    @Test
    fun whenEntityExist_uiStateIsSuccess() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.detailsTransferUiState.collect() }
        transferRepository.sendTransferResources(transferResourceTestData)
        userDataRepository.setDynamicColorPreference(false)
        val expected = DetailsTransferUiState.Success(
            userTransferResource = transferResourceTestData.map {
                UserTransferResource(
                    it,
                    userData = emptyUserData,
                ).copy(hasBeenViewed = true)
            }.take(1),
            dataPoints = arrayListOf(
                DataPoint(
                    date = Util.shortcutDate(
                        Util.dateFormatted(
                            (
                                Instant.parse(
                                    "2022-10-06T23:00:00.000Z",
                                )
                                ),
                        ),
                    ),
                    price = 0.0f,
                    bitmap = null,
                ),
            ),
        )
        assertEquals(expected = expected, actual = viewModel.detailsTransferUiState.value)
    }
}
