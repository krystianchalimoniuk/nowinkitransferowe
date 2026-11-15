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

package pl.nowinkitransferowe.feature.topic.impl

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
import pl.nowinkitransferowe.core.testing.data.userArticlesTestData

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TopicScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val league = TopicUiState("Ekstraklasa", TopicSubject.League("ekstraklasa"), isFollowed = true, TopicTab.ALL, userArticlesTestData.take(4), hasMore = true)

    @Test
    fun topicScreen() {
        composeTestRule.captureMultiDevice("TopicScreen") { Topic(league) }
    }

    @Test
    fun topicScreen_dark() {
        composeTestRule.captureForDevice("phone_dark", DefaultTestDevices.PHONE.spec, "TopicScreen", darkMode = true) { Topic(league) }
    }

    @Test
    fun tagScreen_ofAClub() {
        composeTestRule.captureForDevice("phone", DefaultTestDevices.PHONE.spec, "TagScreenClub") {
            Topic(TopicUiState("West Ham United", TopicSubject.OfClub(clubsTestData[0]), isFollowed = false, TopicTab.ALL, userArticlesTestData.take(2), hasMore = false))
        }
    }

    @Composable
    private fun Topic(state: TopicUiState) {
        NtTheme {
            NtBackground {
                TopicScreen(
                    uiState = state,
                    onBackClick = {},
                    onTabSelected = {},
                    onFollowedChange = {},
                    onLoadMore = {},
                    onArticleClick = {},
                    onArticleSavedChange = { _, _ -> },
                    onLeagueClick = {},
                    onClubClick = {},
                    onPlayerClick = {},
                )
            }
        }
    }
}
