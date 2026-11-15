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

package pl.nowinkitransferowe.feature.onboarding.impl

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import pl.nowinkitransferowe.core.designsystem.component.NtBackground
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.screenshottesting.DefaultTestDevices
import pl.nowinkitransferowe.core.screenshottesting.captureForDevice
import pl.nowinkitransferowe.core.screenshottesting.captureMultiDevice
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class OnboardingScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val clubs = clubsTestData.map { it.copy(topic = "premier-league") } +
        listOf("Arsenal", "Liverpool FC", "Manchester City", "Chelsea FC").mapIndexed { index, name ->
            clubsTestData[0].copy(id = 200 + index, slug = name.lowercase(), name = name)
        }

    private val picked = OnboardingUiState.Success(
        leagues = topicsTestData,
        league = "premier-league",
        clubs = clubs,
        picked = setOf(83, 201),
    )

    @Test
    fun onboardingScreen() {
        composeTestRule.captureMultiDevice("OnboardingScreen") { Onboarding(picked) }
    }

    @Test
    fun onboardingScreen_dark() {
        composeTestRule.captureForDevice("phone_dark", DefaultTestDevices.PHONE.spec, "OnboardingScreen", darkMode = true) { Onboarding(picked) }
    }

    @Test
    fun onboardingScreen_loading() {
        composeTestRule.captureForDevice("phone", DefaultTestDevices.PHONE.spec, "OnboardingScreenLoading") { Onboarding(OnboardingUiState.Loading) }
    }

    @Composable
    private fun Onboarding(state: OnboardingUiState) {
        NtTheme {
            NtBackground {
                OnboardingScreen(uiState = state, onLeagueSelected = {}, onClubToggled = {}, onDone = {}, onSkip = {})
            }
        }
    }
}
