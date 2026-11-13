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
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import kotlin.test.assertEquals

/**
 * UI tests for [SettingsScreen].
 */
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun getString(id: Int, vararg args: Any) = composeTestRule.activity.resources.getString(id, *args)

    private val settings = UserEditableSettings(
        darkThemeConfig = DarkThemeConfig.DARK,
        useDynamicColor = false,
        isNewsNotificationsAllowed = true,
        isTransfersNotificationsAllowed = false,
        isGeneralNotificationsAllowed = true,
    )

    private var darkThemeConfig: DarkThemeConfig? = null
    private var transfersAllowed: Boolean? = null
    private var backClicked = false
    private var systemSettingsOpened = false

    private fun setContent(
        uiState: SettingsUiState = SettingsUiState.Success(settings),
        notificationsEnabled: Boolean = true,
    ) {
        composeTestRule.setContent {
            SettingsScreen(
                settingsUiState = uiState,
                notificationsEnabled = notificationsEnabled,
                onBackClick = { backClicked = true },
                onChangeDynamicColorPreference = {},
                onChangeDarkThemeConfig = { darkThemeConfig = it },
                onChangeNewsNotificationsPreference = {},
                onChangeTransfersNotificationsPreference = { transfersAllowed = it },
                onChangeGeneralNotificationsPreference = {},
                onOpenSystemNotificationSettings = { systemSettingsOpened = true },
                onOpenLicenses = {},
                appVersion = "4.0.0",
                supportDynamicColor = true,
            )
        }
    }

    @Test
    fun whenLoading_showsLoadingWheel() {
        setContent(uiState = SettingsUiState.Loading)
        composeTestRule.onNodeWithContentDescription(getString(R.string.feature_settings_impl_loading)).assertExists()
    }

    @Test
    fun theme_showsSelectedAndReportsChoice() {
        setContent()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dark_mode_config_dark)).assertIsOn()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dark_mode_config_light)).performClick()
        assertEquals(DarkThemeConfig.LIGHT, darkThemeConfig)
    }

    @Test
    fun notificationSwitches_showStateAndReportChanges() {
        setContent()
        val general = getString(R.string.feature_settings_impl_notification_channel_general)
        val transfers = getString(R.string.feature_settings_impl_notification_channel_transfers)
        composeTestRule.onNodeWithTag(general).assertIsOn()
        composeTestRule.onNodeWithTag(transfers).performScrollTo().assertIsOff().performClick()
        assertEquals(true, transfersAllowed)
    }

    @Test
    fun whenNotificationsAreOff_linksToSystemSettings() {
        setContent(notificationsEnabled = false)
        composeTestRule.onNodeWithTag(getString(R.string.feature_settings_impl_notification_channel_general)).assertDoesNotExist()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_notifications_off)).performClick()
        assertEquals(true, systemSettingsOpened)
    }

    @Test
    fun informationAndBack_areShown() {
        setContent()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_privacy_policy)).performScrollTo().assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_version, "4.0.0")).performScrollTo().assertExists()
        composeTestRule.onNodeWithContentDescription(getString(R.string.feature_settings_impl_x_content_description)).performScrollTo().assertExists()
        composeTestRule.onNodeWithContentDescription(getString(R.string.feature_settings_impl_navigate_up)).performClick()
        assertEquals(true, backClicked)
    }
}
