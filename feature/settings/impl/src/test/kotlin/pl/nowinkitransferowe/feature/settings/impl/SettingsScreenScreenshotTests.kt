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

package pl.nowinkitransferowe.feature.settings.impl

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
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.screenshottesting.DefaultTestDevices
import pl.nowinkitransferowe.core.screenshottesting.captureForDevice
import pl.nowinkitransferowe.core.screenshottesting.captureMultiDevice

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class SettingsScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsScreen() {
        composeTestRule.captureMultiDevice("SettingsScreen") { Screen(notificationsEnabled = true) }
    }

    @Test
    fun settingsScreen_dark_notificationsOff() {
        composeTestRule.captureForDevice("phone_dark", DefaultTestDevices.PHONE.spec, "SettingsScreen", darkMode = true) {
            Screen(notificationsEnabled = false)
        }
    }

    @Composable
    private fun Screen(notificationsEnabled: Boolean) {
        NtTheme {
            NtBackground {
                SettingsScreen(
                    settingsUiState = SettingsUiState.Success(
                        UserEditableSettings(
                            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                            useDynamicColor = false,
                            isNewsNotificationsAllowed = true,
                            isTransfersNotificationsAllowed = false,
                            isGeneralNotificationsAllowed = true,
                        ),
                    ),
                    notificationsEnabled = notificationsEnabled,
                    onBackClick = {},
                    onChangeDynamicColorPreference = {},
                    onChangeDarkThemeConfig = {},
                    onChangeNewsNotificationsPreference = {},
                    onChangeTransfersNotificationsPreference = {},
                    onChangeGeneralNotificationsPreference = {},
                    onOpenSystemNotificationSettings = {},
                    onOpenLicenses = {},
                    appVersion = "4.0.0",
                    supportDynamicColor = true,
                )
            }
        }
    }
}
