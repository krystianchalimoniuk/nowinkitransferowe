package pl.nowinkitransferowe.feature.transfers

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import org.junit.Test
import org.junit.Rule
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
            TransferScreen(
                isSyncing = false,
                feedState = TransferFeedUiState.Loading,
                onCleanBackStack = {},
                onDeepLinkOpened = {},
                onTransferResourceViewed = {},
                onTransferResourcesCheckedChanged = { _, _ -> },
                deepLinkedUserTransferResource = null,
                loadNextPage = {},
            )
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

            TransferScreen(
                isSyncing = true,
                feedState = TransferFeedUiState.Success(emptyList()),
                onCleanBackStack = {},
                onDeepLinkOpened = {},
                onTransferResourceViewed = {},
                onTransferResourcesCheckedChanged = { _, _ -> },
                deepLinkedUserTransferResource = null,
                loadNextPage = {},
            )

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
            TransferScreen(
                isSyncing = false,
                feedState = TransferFeedUiState.Success(feed = userTransferResourcesTestData),
                onCleanBackStack = {},
                onDeepLinkOpened = {},
                onTransferResourceViewed = {},
                onTransferResourcesCheckedChanged = { _, _ -> },
                deepLinkedUserTransferResource = null,
                loadNextPage = {},
            )
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
            TransferScreen(
                isSyncing = false,
                feedState = TransferFeedUiState.Success(feed = userTransferResourcesTestData),
                onCleanBackStack = {},
                onDeepLinkOpened = {},
                onTransferResourceViewed = {},
                onTransferResourcesCheckedChanged = { _, _ -> },
                deepLinkedUserTransferResource = null,
                loadNextPage = {},
            )
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