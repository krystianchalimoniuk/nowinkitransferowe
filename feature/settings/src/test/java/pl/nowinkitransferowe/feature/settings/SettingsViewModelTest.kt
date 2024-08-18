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

package pl.nowinkitransferowe.feature.settings

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule

class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel = SettingsViewModel(userDataRepository)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        kotlin.test.assertEquals(SettingsUiState.Loading, viewModel.settingsUiState.value)
    }

    @Test
    fun stateIsSuccessAfterUserDataLoaded() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.settingsUiState.collect() }

        userDataRepository.setDarkThemeConfig(DarkThemeConfig.DARK)

        kotlin.test.assertEquals(
            SettingsUiState.Success(
                UserEditableSettings(
                    darkThemeConfig = DarkThemeConfig.DARK,
                    useDynamicColor = false,
                    isNewsNotificationsAllowed = true,
                    isTransfersNotificationsAllowed = true,
                    isGeneralNotificationsAllowed = true,
                ),
            ),
            viewModel.settingsUiState.value,
        )

        collectJob.cancel()
    }
}
