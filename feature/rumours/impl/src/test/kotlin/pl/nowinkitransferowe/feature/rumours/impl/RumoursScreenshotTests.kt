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

package pl.nowinkitransferowe.feature.rumours.impl

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import pl.nowinkitransferowe.core.designsystem.component.NtBackground
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.screenshottesting.DefaultTestDevices
import pl.nowinkitransferowe.core.screenshottesting.captureForDevice
import pl.nowinkitransferowe.core.screenshottesting.captureMultiDevice
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.fixedRumoursTestData
import pl.nowinkitransferowe.core.testing.data.rumourStatsTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RumoursScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Test
    fun rumoursScreen() {
        composeTestRule.captureMultiDevice("RumoursScreen") { Board() }
    }

    @Test
    fun rumoursScreen_dark() {
        composeTestRule.captureForDevice("phone_dark", DefaultTestDevices.PHONE.spec, "RumoursScreen", darkMode = true) { Board() }
    }

    @Test
    fun rumourScreen() {
        composeTestRule.captureForDevice("phone", DefaultTestDevices.PHONE.spec, "RumourScreen") { Card() }
    }

    @Test
    fun rumourScreen_dark() {
        composeTestRule.captureForDevice("phone_dark", DefaultTestDevices.PHONE.spec, "RumourScreen", darkMode = true) { Card() }
    }

    @Composable
    private fun Board() {
        NtTheme {
            NtBackground {
                RumoursScreen(
                    uiState = RumoursUiState.Success(
                        topic = null,
                        leagues = topicsTestData,
                        stage = null,
                        rumours = fixedRumoursTestData,
                        stats = rumourStatsTestData,
                    ),
                    onTopicSelected = {},
                    onStageSelected = {},
                    onRumourClick = {},
                )
            }
        }
    }

    @Composable
    private fun Card() {
        NtTheme {
            NtBackground {
                RumourScreen(
                    uiState = RumourUiState.Success(
                        rumour = fixedRumoursTestData[0],
                        isSaved = true,
                        player = null,
                        articles = articlesTestData.associateBy(Article::id),
                    ),
                    showBackButton = true,
                    onBackClick = {},
                    onBookmarkChanged = {},
                    onArticleClick = {},
                    onTransferClick = {},
                )
            }
        }
    }
}
