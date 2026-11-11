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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import pl.nowinkitransferowe.core.designsystem.component.NtButton
import pl.nowinkitransferowe.core.designsystem.component.NtOutlinedButton
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.screenshottesting.captureMultiTheme

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class, qualifiers = "480dpi")
@LooperMode(LooperMode.Mode.PAUSED)
class ButtonScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun ntButton_multipleThemes() {
        composeTestRule.captureMultiTheme("Button") { description ->
            Surface {
                NtButton(onClick = {}, text = { Text("$description Button") })
            }
        }
    }

    @Test
    fun ntOutlineButton_multipleThemes() {
        composeTestRule.captureMultiTheme("Button", "OutlineButton") { description ->
            Surface {
                NtOutlinedButton(onClick = {}, text = { Text("$description OutlineButton") })
            }
        }
    }

    @Test
    fun ntButton_leadingIcon_multipleThemes() {
        composeTestRule.captureMultiTheme(
            name = "Button",
            overrideFileName = "ButtonLeadingIcon",
        ) { description ->
            Surface {
                NtButton(
                    onClick = {},
                    text = { Text("$description Icon Button") },
                    leadingIcon = { Icon(imageVector = NtIcons.Add, contentDescription = null) },
                )
            }
        }
    }
}
