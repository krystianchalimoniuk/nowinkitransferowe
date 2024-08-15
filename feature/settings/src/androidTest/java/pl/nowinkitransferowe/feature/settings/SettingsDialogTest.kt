package pl.nowinkitransferowe.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test
import org.junit.Rule
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.testing.rules.GrantPostNotificationsPermissionRule


class SettingsDialogTest {

    @get:Rule(order = 0)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()


    private fun getString(id: Int) = composeTestRule.activity.resources.getString(id)


    @Test
    fun whenLoading_showsLoadingText() {
        composeTestRule.setContent {
            SettingsDialog(
                onDismiss = {},
                settingsUiState = SettingsUiState.Loading,
                onChangeDynamicColorPreference = {},
                onChangeDarkThemeConfig = {},
                onChangeNewsNotificationsPreference = {},
                onChangeTransfersNotificationsPreference = {},
                onChangeGeneralNotificationsPreference = {}

            )
        }

        composeTestRule
            .onNodeWithText(getString(R.string.feature_settings_loading))
            .assertExists()
    }

    @Test
    fun whenStateIsSuccess_allDefaultSettingsAreDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                onDismiss = {},
                settingsUiState = SettingsUiState.Success(
                    UserEditableSettings(
                        darkThemeConfig = DarkThemeConfig.DARK,
                        useDynamicColor = false,
                        isNewsNotificationsAllowed = true,
                        isTransfersNotificationsAllowed = true,
                        isGeneralNotificationsAllowed = true
                    ),
                ),
                onChangeDynamicColorPreference = {},
                onChangeDarkThemeConfig = {},
                onChangeNewsNotificationsPreference = {},
                onChangeTransfersNotificationsPreference = {},
                onChangeGeneralNotificationsPreference = {}
            )
        }

        // Check that all the possible settings are displayed.
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_notification_channel_news)).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_notification_channel_transfers)).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_notification_channel_general)).assertExists()


        composeTestRule.onNodeWithText(
            getString(R.string.feature_settings_dark_mode_config_system_default),
        ).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_light)).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_dark)).assertExists()

        // Check that the correct settings are selected.

        composeTestRule.onNodeWithTag(getString(R.string.feature_settings_notification_channel_news)).assertIsOn()
        composeTestRule.onNodeWithTag(getString(R.string.feature_settings_notification_channel_transfers)).assertIsOn()
        composeTestRule.onNodeWithTag(getString(R.string.feature_settings_notification_channel_general)).assertIsOn()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_dark_mode_config_dark)).assertIsSelected()
    }

    @Test
    fun whenStateIsSuccess_allSocialMediaAreDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = SettingsUiState.Success(
                    UserEditableSettings(
                        darkThemeConfig = DarkThemeConfig.DARK,
                        useDynamicColor = false,
                        isNewsNotificationsAllowed = true,
                        isTransfersNotificationsAllowed = true,
                        isGeneralNotificationsAllowed = true
                    ),
                ),
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeDarkThemeConfig = {},
                onChangeNewsNotificationsPreference = {},
                onChangeTransfersNotificationsPreference = {},
                onChangeGeneralNotificationsPreference = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(getString(R.string.feature_settings_instagram_content_description)).assertExists()
        composeTestRule.onNodeWithContentDescription(getString(R.string.feature_settings_x_content_description)).assertExists()
        composeTestRule.onNodeWithContentDescription(getString(R.string.feature_settings_facebook_content_description)).assertExists()
        composeTestRule.onNodeWithContentDescription(getString(R.string.feature_settings_www_content_description)).assertExists()
    }

    @Test
    fun whenStateIsSuccess_allLinksAreDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = SettingsUiState.Success(
                    UserEditableSettings(
                        darkThemeConfig = DarkThemeConfig.DARK,
                        useDynamicColor = false,
                        isNewsNotificationsAllowed = true,
                        isTransfersNotificationsAllowed = true,
                        isGeneralNotificationsAllowed = true
                    ),
                ),
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeDarkThemeConfig = {},
                onChangeNewsNotificationsPreference = {},
                onChangeTransfersNotificationsPreference = {},
                onChangeGeneralNotificationsPreference = {}
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_privacy_policy)).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_licenses)).assertExists()
    }
}
