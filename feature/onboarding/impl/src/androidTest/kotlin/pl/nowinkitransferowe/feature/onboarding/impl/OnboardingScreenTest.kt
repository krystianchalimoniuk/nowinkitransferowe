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
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for [OnboardingScreen].
 */
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pickingAClub_enablesDone_andTheLeaguesAndSkipReport() {
        var toggled: Int? = null
        var league: String? = null
        var done = false
        var skipped = false
        composeTestRule.setContent {
            OnboardingScreen(
                uiState = OnboardingUiState.Success(topicsTestData, "premier-league", clubsTestData.take(1), picked = emptySet()),
                onLeagueSelected = { league = it },
                onClubToggled = { toggled = it },
                onDone = { done = true },
                onSkip = { skipped = true },
            )
        }

        composeTestRule.onNodeWithTag("onboarding:done").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("onboarding:club:83").performClick()
        assertEquals(83, toggled)
        composeTestRule.onNodeWithTag("onboarding:league:serie-a").performClick()
        assertEquals("serie-a", league)
        composeTestRule.onNodeWithTag("onboarding:skip").performClick()
        assertTrue(skipped)
        assertEquals(false, done)
    }

    @Test
    fun withPickedClubs_doneReports() {
        var done = false
        composeTestRule.setContent {
            OnboardingScreen(
                uiState = OnboardingUiState.Success(topicsTestData, "premier-league", clubsTestData.take(1), picked = setOf(83)),
                onLeagueSelected = {},
                onClubToggled = {},
                onDone = { done = true },
                onSkip = {},
            )
        }

        composeTestRule.onNodeWithTag("onboarding:done").assertIsEnabled().performClick()
        assertTrue(done)
    }
}
