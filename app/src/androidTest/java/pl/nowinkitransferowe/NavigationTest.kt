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

package pl.nowinkitransferowe

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.nowinkitransferowe.core.data.repository.NewsRepository
import pl.nowinkitransferowe.core.testing.rules.GrantPostNotificationsPermissionRule
import javax.inject.Inject
import pl.nowinkitransferowe.feature.bookmarks.R as BookmarksR
import pl.nowinkitransferowe.feature.news.R as NewsR
import pl.nowinkitransferowe.feature.settings.R as SettingsR
import pl.nowinkitransferowe.feature.transfers.R as TransfersR

/**
 * Tests all the navigation flows that are handled by the navigation library.
 */
@HiltAndroidTest
class NavigationTest {

    /**
     * Manages the components' state and is used to perform injection on your test
     */
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * Create a temporary folder used to create a Data Store file. This guarantees that
     * the file is removed in between each test, preventing a crash.
     */
    @BindValue
    @get:Rule(order = 1)
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    /**
     * Grant [android.Manifest.permission.POST_NOTIFICATIONS] permission.
     */
    @get:Rule(order = 2)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule()

    /**
     * Use the primary activity to initialize the app normally.
     */
    @get:Rule(order = 3)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var newsRepository: NewsRepository

    // The strings used for matching in these tests
    private val navigateUp by composeTestRule.stringResource(NewsR.string.feature_news_navigate_up)
    private val news by composeTestRule.stringResource(NewsR.string.feature_news_title)
    private val transfers by composeTestRule.stringResource(TransfersR.string.feature_transfers_title)
    private val darkMode by composeTestRule.stringResource(SettingsR.string.feature_settings_dark_mode_config_dark)
    private val appName by composeTestRule.stringResource(R.string.app_name)
    private val saved by composeTestRule.stringResource(BookmarksR.string.feature_bookmarks_title)
    private val settings by composeTestRule.stringResource(SettingsR.string.feature_settings_top_app_bar_action_icon_description)
    private val ok by composeTestRule.stringResource(SettingsR.string.feature_settings_dismiss_dialog_button_text)

    @Before
    fun setup() = hiltRule.inject()

    @Test
    fun firstScreen_isNews() {
        composeTestRule.apply {
            // VERIFY news is selected
            onNodeWithText(news).assertIsSelected()
        }
    }

    // TODO: implement tests related to navigation & resetting of destinations (b/213307564)
    // Restoring content should be tested with another tab than the News one, as that will
    // still succeed even when restoring state is turned off.
    /**
     * When navigating between the different top level destinations, we should restore the state
     * of previously visited destinations.
     */
    @Test
    fun navigationBar_navigateToPreviouslySelectedTab_restoresContent() {
        composeTestRule.apply {
            onAllNodesWithContentDescription(
                activity.getString(
                    pl.nowinkitransferowe.core.ui.R.string.core_ui_bookmark,
                ),
            )
                .onFirst()
                .performClick()
            // WHEN the user navigates to the Transfers destination
            onNodeWithText(transfers).performClick()
            // AND the user navigates to the News destination
            onNodeWithText(news).performClick()
            // THEN the state of the News destination is restored
            onAllNodesWithContentDescription(
                activity.getString(
                    pl.nowinkitransferowe.core.ui.R.string.core_ui_unbookmark,
                ),
            )
                .assertCountEquals(1)
                .onFirst()
        }
    }

    /**
     * When reselecting a tab, it should show that tab's start destination and restore its state.
     */
    @Test
    fun navigationBar_reselectTab_keepsState() {
        composeTestRule.apply {
            // GIVEN the user follows a topic
            onAllNodesWithContentDescription(
                activity.getString(
                    pl.nowinkitransferowe.core.ui.R.string.core_ui_bookmark,
                ),
            )
                .onFirst()
                .performClick()
            // WHEN the user taps the News navigation bar item
            onNodeWithText(news).performClick()
            // THEN the state of the News destination is restored
            onAllNodesWithContentDescription(
                activity.getString(
                    pl.nowinkitransferowe.core.ui.R.string.core_ui_unbookmark,
                ),
            )
                .assertCountEquals(1)
                .onFirst()
        }
    }

    @Test
    fun topLevelDestinations_doNotShowUpArrow() {
        composeTestRule.apply {
            // GIVEN the user is on any of the top level destinations, THEN the Up arrow is not shown.
            onNodeWithContentDescription(navigateUp).assertDoesNotExist()

            onNodeWithText(saved).performClick()
            onNodeWithContentDescription(navigateUp).assertDoesNotExist()

            onNodeWithText(transfers).performClick()
            onNodeWithContentDescription(navigateUp).assertDoesNotExist()
        }
    }

    @Test
    fun topLevelDestinations_showTopBarWithTitle() {
        composeTestRule.apply {
            // Verify that the top bar contains the app name on the first screen.
            onNodeWithText(appName).assertExists()

            // Go to the saved tab, verify that the top bar contains "saved". This means
            // we'll have 2 elements with the text "saved" on screen. One in the top bar, and
            // one in the bottom navigation.
            onNodeWithText(saved).performClick()
            onAllNodesWithText(saved).assertCountEquals(2)

            // As above but for the transfers tab.
            onNodeWithText(transfers).performClick()
            onAllNodesWithText(transfers).assertCountEquals(2)
        }
    }

    @Test
    fun topLevelDestinations_showSettingsIcon() {
        composeTestRule.apply {
            onNodeWithContentDescription(settings).assertExists()

            onNodeWithText(saved).performClick()
            onNodeWithContentDescription(settings).assertExists()

            onNodeWithText(transfers).performClick()
            onNodeWithContentDescription(settings).assertExists()
        }
    }

    @Test
    fun whenSettingsIconIsClicked_settingsDialogIsShown() {
        composeTestRule.apply {
            onNodeWithContentDescription(settings).performClick()

            // Check that one of the settings is actually displayed.
            onNodeWithText(darkMode).assertExists()
        }
    }

    @Test
    fun whenSettingsDialogDismissed_previousScreenIsDisplayed() {
        composeTestRule.apply {
            // Navigate to the saved screen, open the settings dialog, then close it.
            onNodeWithText(saved).performClick()
            onNodeWithContentDescription(settings).performClick()
            onNodeWithText(ok).performClick()

            // Check that the saved screen is still visible and selected.
            onNode(hasText(saved) and hasTestTag("NtNavItem")).assertIsSelected()
        }
    }

    /*
     * There should always be at most one instance of a top-level destination at the same time.
     */
    @Test(expected = NoActivityResumedException::class)
    fun homeDestination_back_quitsApp() {
        composeTestRule.apply {
            // GIVEN the user navigates to the Transfers destination
            onNodeWithText(transfers).performClick()
            // and then navigates to the news destination
            onNodeWithText(news).performClick()
            // WHEN the user uses the system button/gesture to go back
            Espresso.pressBack()
            // THEN the app quits
        }
    }

    /*
     * When pressing back from any top level destination except "News", the app navigates back
     * to the "News" destination, no matter which destinations you visited in between.
     */
    @Test
    fun navigationBar_backFromAnyDestination_returnsToNews() {
        composeTestRule.apply {
            // GIVEN the user navigated to the Transfers destination
            onNodeWithText(transfers).performClick()
            // TODO: Add another destination here to increase test coverage, see b/226357686.
            // WHEN the user uses the system button/gesture to go back,
            Espresso.pressBack()
            // THEN the app shows the News destination
            onNodeWithText(news).assertExists()
        }
    }

    @Test
    fun navigationBar_multipleBackStackNews() = runTest {
        composeTestRule.apply {
            // Select the last news
            val newsItem =
                newsRepository.getNewsResources().first()[4]

            onNodeWithTag("news:feed").performScrollToNode(hasText(newsItem.title))
            onNodeWithText(newsItem.title).performClick()

            // Switch tab
            onNodeWithText(transfers).performClick()

            // Come back to Transfers
            onNodeWithText(news).performClick()

            // Verify the topic is still shown
            onNodeWithTag("news:${newsItem.id}").assertExists()
        }
    }
}
