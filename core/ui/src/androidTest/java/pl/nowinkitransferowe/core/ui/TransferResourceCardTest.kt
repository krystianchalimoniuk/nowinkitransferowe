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
                onClick = {}
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