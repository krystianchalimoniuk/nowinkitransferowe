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

package pl.nowinkitransferowe.core.designsystem

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtOverlayLoadingWheel
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.screenshottesting.DefaultRoborazziOptions
import pl.nowinkitransferowe.core.screenshottesting.captureMultiTheme

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class, qualifiers = "480dpi")
@LooperMode(LooperMode.Mode.PAUSED)
class LoadingWheelScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadingWheel_multipleThemes() {
        composeTestRule.captureMultiTheme("LoadingWheel") {
            Surface {
                NtLoadingWheel(contentDesc = "test")
            }
        }
    }

    @Test
    fun overlayLoadingWheel_multipleThemes() {
        composeTestRule.captureMultiTheme("LoadingWheel", "OverlayLoadingWheel") {
            Surface {
                NtOverlayLoadingWheel(contentDesc = "test")
            }
        }
    }

    @Test
    fun loadingWheelAnimation() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NtTheme {
                NtLoadingWheel(contentDesc = "")
            }
        }
        // Try multiple frames of the animation; some arbitrary, some synchronized with duration.
        listOf(20L, 115L, 724L, 1000L).forEach { deltaTime ->
            composeTestRule.mainClock.advanceTimeBy(deltaTime)
            composeTestRule.onRoot()
                .captureRoboImage(
                    "src/test/screenshots/LoadingWheel/LoadingWheel_animation_$deltaTime.png",
                    roborazziOptions = DefaultRoborazziOptions,
                )
        }
    }
}
