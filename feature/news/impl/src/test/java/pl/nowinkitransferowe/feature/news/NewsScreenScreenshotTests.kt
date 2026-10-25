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

package pl.nowinkitransferowe.feature.news

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
import pl.nowinkitransferowe.core.screenshottesting.DefaultTestDevices
import pl.nowinkitransferowe.core.screenshottesting.captureForDevice
import pl.nowinkitransferowe.core.screenshottesting.captureMultiDevice
import pl.nowinkitransferowe.core.ui.NewsFeedUiState
import pl.nowinkitransferowe.core.ui.UserNewsResourcePreviewParameterProvider
import java.util.TimeZone

/**
 * Screenshot tests for the [ForYouScreen].
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NewsScreenScreenshotTests {

    /**
     * Use a test activity to set the content on.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val userNewsResources = UserNewsResourcePreviewParameterProvider().values.first()

    @Before
    fun setTimeZone() {
        // Make time zone deterministic in tests
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Test
    fun newsScreenPopulatedFeed() {
        composeTestRule.captureMultiDevice("NewsScreenPopulatedFeed") {
            NtTheme {
                NewsScreen(
                    isSyncing = false,
                    feedState = NewsFeedUiState.Success(
                        feed = userNewsResources,
                    ),
                    onNewsResourcesCheckedChanged = { _, _ -> },
                    onNewsResourceViewed = {},
                    onTopicClick = {},
                    onNewsClick = {},
                    onNewsSelected = {},
                    loadNextPage = {},
                )
            }
        }
    }

    @Test
    fun newsScreenLoading() {
        composeTestRule.captureMultiDevice("NewsScreenLoading") {
            NtTheme {
                NewsScreen(
                    isSyncing = false,
                    feedState = NewsFeedUiState.Loading,
                    onNewsResourcesCheckedChanged = { _, _ -> },
                    onNewsResourceViewed = {},
                    onTopicClick = {},
                    onNewsClick = {},
                    onNewsSelected = {},
                    loadNextPage = {},
                )
            }
        }
    }

    @Test
    fun newsScreenPopulatedAndLoading() {
        composeTestRule.captureMultiDevice("NewsScreenPopulatedAndLoading") {
            NewsScreenPopulatedAndLoading()
        }
    }

    @Test
    fun newsScreenPopulatedAndLoading_dark() {
        composeTestRule.captureForDevice(
            deviceName = "phone_dark",
            deviceSpec = DefaultTestDevices.PHONE.spec,
            screenshotName = "NewsScreenPopulatedAndLoading",
            darkMode = true,
        ) {
            NewsScreenPopulatedAndLoading()
        }
    }

    @Composable
    private fun NewsScreenPopulatedAndLoading() {
        NtTheme {
            NtBackground {
                NtTheme {
                    NewsScreen(
                        isSyncing = true,
                        feedState = NewsFeedUiState.Success(
                            feed = userNewsResources,
                        ),
                        onNewsResourcesCheckedChanged = { _, _ -> },
                        onNewsResourceViewed = {},
                        onTopicClick = {},
                        onNewsClick = {},
                        onNewsSelected = {},
                        loadNextPage = {},

                    )
                }
            }
        }
    }
}
