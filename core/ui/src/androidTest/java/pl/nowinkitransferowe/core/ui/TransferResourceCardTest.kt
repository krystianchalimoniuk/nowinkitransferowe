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

package pl.nowinkitransferowe.core.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.userTransferResourcesTestData

class TransferResourceCardTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testUnreadDot_displayedWhenUnread() {
        val unreadTransfers = userTransferResourcesTestData[2]

        composeTestRule.setContent {
            TransferResourceCardExpanded(
                userTransferResource = unreadTransfers,
                isBookmarked = false,
                hasBeenViewed = false,
                onToggleBookmark = {},
                onClick = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(
                    R.string.core_ui_unread_resource_dot_content_description,
                ),
            )
            .assertIsDisplayed()
    }

    @Test
    fun testUnreadDot_notDisplayedWhenRead() {
        val readTransfer = userTransferResourcesTestData[0]

        composeTestRule.setContent {
            TransferResourceCardExpanded(
                userTransferResource = readTransfer,
                isBookmarked = false,
                hasBeenViewed = true,
                onToggleBookmark = {},
                onClick = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(
                    R.string.core_ui_unread_resource_dot_content_description,
                ),
            )
            .assertDoesNotExist()
    }
}
