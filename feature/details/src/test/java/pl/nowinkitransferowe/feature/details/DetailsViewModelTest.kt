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

package pl.nowinkitransferowe.feature.details

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserNewsResourceRepository
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.testing.data.newsResourcesTestData
import pl.nowinkitransferowe.core.testing.repository.TestNewsRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.feature.details.navigation.LINKED_NEWS_RESOURCE_ID
import kotlin.test.assertEquals

class DetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val newsRepository = TestNewsRepository()

    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    )

    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: DetailsViewModel

    @Before
    fun setup() {
        viewModel = DetailsViewModel(
            savedStateHandle = savedStateHandle,
            userDataRepository = userDataRepository,
            newsRepository = newsRepository,
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(DetailsUiState.Loading, viewModel.detailsUiState.value)
    }

    @Test
    fun whenEntityIsNotExist_uiStateIsError() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.detailsUiState.collect() }
        savedStateHandle[LINKED_NEWS_RESOURCE_ID] = "11"
        newsRepository.sendNewsResources(newsResourcesTestData)
        assertEquals(expected = DetailsUiState.Error, actual = viewModel.detailsUiState.value)
        collectJob.cancel()
    }

    @Test
    fun whenEntityExist_uiStateIsSuccess() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.detailsUiState.collect() }
        savedStateHandle[LINKED_NEWS_RESOURCE_ID] = newsResourcesTestData.first().id
        newsRepository.sendNewsResources(newsResourcesTestData)
        userDataRepository.setDynamicColorPreference(false)
        val expected = DetailsUiState.Success(
            userNewsResource = UserNewsResource(
                newsResource = newsResourcesTestData[0],
                userData = emptyUserData,
            ).copy(hasBeenViewed = true),
        )
        assertEquals(expected = expected, actual = viewModel.detailsUiState.value)
        collectJob.cancel()
    }

    @Test
    fun whenUpdateNewsResourceSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        val newsResourceId = "123"
        viewModel.bookmarkNews(newsResourceId, true)

        assertEquals(
            expected = setOf(newsResourceId),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )

        viewModel.bookmarkNews(newsResourceId, false)

        assertEquals(
            expected = emptySet(),
            actual = userDataRepository.userData.first().bookmarkedNewsResources,
        )
    }
}
