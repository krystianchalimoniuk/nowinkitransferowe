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

package pl.nowinkitransferowe.feature.articledetails.impl

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
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.screenshottesting.DefaultTestDevices
import pl.nowinkitransferowe.core.screenshottesting.captureForDevice
import pl.nowinkitransferowe.core.screenshottesting.captureMultiDevice
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.playersTestData
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.data.userArticlesTestData
import java.util.TimeZone

/**
 * Screenshot tests of the article screen. Robolectric draws no WebView, so the content stays
 * empty; the header, the rumour box and the chips show.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ArticleDetailsScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    private val state = ArticleDetailsUiState.Success(
        userArticle = userArticlesTestData.first(),
        clubs = clubsTestData,
        players = playersTestData,
        rumour = rumoursTestData.first(),
    )

    @Test
    fun articleScreen() {
        composeTestRule.captureMultiDevice("ArticleDetailsScreen") { Screen() }
    }

    @Test
    fun articleScreen_dark() {
        composeTestRule.captureForDevice(
            deviceName = "phone_dark",
            deviceSpec = DefaultTestDevices.PHONE.spec,
            screenshotName = "ArticleDetailsScreen",
            darkMode = true,
        ) { Screen() }
    }

    @Composable
    private fun Screen() {
        NtTheme {
            NtBackground {
                ArticleDetailsScreen(
                    detailsUiState = state,
                    showBackButton = true,
                    onBackClick = {},
                    onTopicClick = {},
                    onBookmarkChanged = { _, _ -> },
                    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                )
            }
        }
    }
}
