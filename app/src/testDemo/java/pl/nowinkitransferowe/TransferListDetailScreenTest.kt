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

@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package pl.nowinkitransferowe

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.core.navigation.rememberNavigationState
import pl.nowinkitransferowe.core.navigation.toEntries
import pl.nowinkitransferowe.feature.details.transfers.DetailsTransferPlaceholder
import pl.nowinkitransferowe.feature.details.transfers.navigation.transferDetailsEntry
import pl.nowinkitransferowe.feature.transfers.api.navigation.TransfersNavKey
import pl.nowinkitransferowe.feature.transfers.navigation.transfersEntry
import pl.nowinkitransferowe.uitesthiltmanifest.HiltComponentActivity
import javax.inject.Inject
import kotlin.properties.ReadOnlyProperty
import kotlin.test.assertTrue

private const val EXPANDED_WIDTH = "w1200dp-h840dp"
private const val COMPACT_WIDTH = "w412dp-h915dp"

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class TransferListDetailScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var transferRepository: TransferRepository

    /** Convenience function for getting all transfer during tests, */
    private fun getTransfers(): List<TransferResource> = runBlocking {
        transferRepository.getTransferResources().first()
    }

    // The strings used for matching in these tests.
    private val placeholderText by composeTestRule.stringResource(pl.nowinkitransferowe.feature.details.transfers.R.string.feature_details_transfers_impl_select_a_transfer)
    private val listPaneTag = "transfers:feed"

    private val TransferResource.testTag
        get() = "transfer:${this.id}"

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Composable
    private fun TestNavDisplay() {
        val navigationState = rememberNavigationState(
            startKey = TransfersNavKey,
            topLevelKeys = setOf(TransfersNavKey),
        )
        val navigator = Navigator(navigationState)
        val entryProvider = entryProvider {
            transfersEntry(navigator, detailPlaceholder = { DetailsTransferPlaceholder() })
            transferDetailsEntry(navigator)
        }
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            sceneStrategies = listOf(rememberListDetailSceneStrategy()),
        )
    }

    @Test
    @Config(qualifiers = EXPANDED_WIDTH)
    fun expandedWidth_initialState_showsTwoPanesWithPlaceholder() {
        composeTestRule.apply {
            setContent {
                NtTheme {
                    TestNavDisplay()
                }
            }

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsDisplayed()
        }
    }

    @Test
    @Config(qualifiers = COMPACT_WIDTH)
    fun compactWidth_initialState_showsListPane() {
        composeTestRule.apply {
            setContent {
                NtTheme {
                    TestNavDisplay()
                }
            }

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
        }
    }

    @Test
    @Config(qualifiers = EXPANDED_WIDTH)
    fun expandedWidth_transferSelected_updatesDetailPane() {
        composeTestRule.apply {
            setContent {
                NtTheme {
                    TestNavDisplay()
                }
            }

            val firstTransfers = getTransfers().first()
            onNodeWithText(firstTransfers.name).performClick()

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
            onNodeWithTag(firstTransfers.testTag).assertIsDisplayed()
        }
    }

    @Test
    @Config(qualifiers = COMPACT_WIDTH)
    fun compactWidth_transferSelected_showsTransferDetailPane() {
        composeTestRule.apply {
            setContent {
                NtTheme {
                    TestNavDisplay()
                }
            }

            val firstTransfers = getTransfers().first()
            onNodeWithText(firstTransfers.name).performClick()

            onNodeWithTag(listPaneTag).assertIsNotDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
            onNodeWithTag(firstTransfers.testTag).assertIsDisplayed()
        }
    }

    @Test
    @Config(qualifiers = EXPANDED_WIDTH)
    fun expandedWidth_backPressFromTransferDetail_leavesTransfers() {
        var unhandledBackPress = false
        composeTestRule.apply {
            setContent {
                NtTheme {
                    // With both panes shown, the list-detail scene does not handle back, so it
                    // "falls through" to this BackHandler.
                    BackHandler {
                        unhandledBackPress = true
                    }
                    TestNavDisplay()
                }
            }

            val firstTransfers = getTransfers().first()
            onNodeWithText(firstTransfers.name).performClick()

            waitForIdle()
            Espresso.pressBack()

            assertTrue(unhandledBackPress)
        }
    }

    @Test
    @Config(qualifiers = COMPACT_WIDTH)
    fun compactWidth_backPressFromTransferDetail_showsListPane() {
        composeTestRule.apply {
            setContent {
                NtTheme {
                    TestNavDisplay()
                }
            }

            val firstTransfers = getTransfers().first()
            onNodeWithText(firstTransfers.name).performClick()
            waitForIdle()
            Espresso.pressBack()

            onNodeWithTag(listPaneTag).assertIsDisplayed()
            onNodeWithText(placeholderText).assertIsNotDisplayed()
            onNodeWithTag(firstTransfers.testTag).assertIsNotDisplayed()
        }
    }
}

private fun AndroidComposeTestRule<*, *>.stringResource(
    @StringRes resId: Int,
): ReadOnlyProperty<Any, String> = ReadOnlyProperty { _, _ -> activity.getString(resId) }
