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

package pl.nowinkitransferowe.feature.transfers

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.userTransferResourcesTestData
import pl.nowinkitransferowe.core.testing.rules.GrantPostNotificationsPermissionRule
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import pl.nowinkitransferowe.feature.transfers.navigation.TransferScreen

class TransferScreenTest {
    @get:Rule(order = 0)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun circularProgressIndicator_whenScreenIsLoading_exists() {
        composeTestRule.setContent {
            Box {
                TransferScreen(
                    isSyncing = false,
                    feedState = TransferFeedUiState.Loading,
                    onTransferResourceViewed = {},
                    onTransferResourcesCheckedChanged = { _, _ -> },
                    onTransferClick = {},
                    loadNextPage = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_transfers_loading),
            )
            .assertExists()
    }

    @Test
    fun circularProgressIndicator_whenScreenIsSyncing_exists() {
        composeTestRule.setContent {
            Box {
                TransferScreen(
                    isSyncing = true,
                    feedState = TransferFeedUiState.Success(emptyList()),
                    onTransferClick = {},
                    onTransferResourceViewed = {},
                    onTransferResourcesCheckedChanged = { _, _ -> },
                    loadNextPage = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_transfers_loading),
            )
            .assertExists()
    }

    @Test
    fun feed_whenLoaded_showsFeed() {
        composeTestRule.setContent {
            Box {
                TransferScreen(
                    isSyncing = false,
                    feedState = TransferFeedUiState.Success(feed = userTransferResourcesTestData),
                    onTransferClick = {},
                    onTransferResourceViewed = {},
                    onTransferResourcesCheckedChanged = { _, _ -> },
                    loadNextPage = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                userTransferResourcesTestData[0].name,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    userTransferResourcesTestData[1].name,
                    substring = true,
                ),
            )

        composeTestRule
            .onNodeWithText(
                userTransferResourcesTestData[1].name,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun feed_whenLoadNextPageExecuted_showsFeedWithNewItems() {
        composeTestRule.setContent {
            Box {
                TransferScreen(
                    isSyncing = false,
                    feedState = TransferFeedUiState.Success(feed = userTransferResourcesTestData),
                    onTransferClick = {},
                    onTransferResourceViewed = {},
                    onTransferResourcesCheckedChanged = { _, _ -> },
                    loadNextPage = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                userTransferResourcesTestData[0].name,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    userTransferResourcesTestData[4].name,
                    substring = true,
                ),
            )

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    userTransferResourcesTestData[9].name,
                    substring = true,
                ),
            )

        composeTestRule
            .onNodeWithText(
                userTransferResourcesTestData[9].name,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()
    }
}
