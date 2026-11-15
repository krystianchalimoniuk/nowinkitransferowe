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

package pl.nowinkitransferowe.feature.followed.impl

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
import pl.nowinkitransferowe.core.testing.data.lewandowskiTestData
import pl.nowinkitransferowe.core.testing.data.playersTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FollowedScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val followed = FollowedUiState.Success(
        clubs = listOf(FollowedClub(clubsTestData[1], "Serie A"), FollowedClub(clubsTestData[0], "Premier League")),
        players = listOf(playersTestData[0], lewandowskiTestData),
        notifications = FollowedNotifications(followedLeagues = setOf("premier-league")),
        leagues = topicsTestData,
    )

    @Test
    fun followedScreen() {
        composeTestRule.captureMultiDevice("FollowedScreen") { Followed(followed) }
    }

    @Test
    fun followedScreen_dark() {
        composeTestRule.captureForDevice("phone_dark", DefaultTestDevices.PHONE.spec, "FollowedScreen", darkMode = true) { Followed(followed) }
    }

    @Test
    fun followedScreen_empty() {
        composeTestRule.captureForDevice("phone", DefaultTestDevices.PHONE.spec, "FollowedScreenEmpty") {
            Followed(FollowedUiState.Success(emptyList(), emptyList()))
        }
    }

    @Composable
    private fun Followed(state: FollowedUiState) {
        NtTheme {
            NtBackground {
                FollowedScreen(
                    uiState = state,
                    onBackClick = {},
                    onClubClick = {},
                    onPlayerClick = {},
                    onClubFollowedChange = { _, _ -> },
                    onPlayerFollowedChange = { _, _ -> },
                )
            }
        }
    }
}
