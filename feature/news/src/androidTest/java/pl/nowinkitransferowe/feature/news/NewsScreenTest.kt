package pl.nowinkitransferowe.feature.news

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
import pl.nowinkitransferowe.core.testing.data.userNewsResourcesTestData
import pl.nowinkitransferowe.core.testing.rules.GrantPostNotificationsPermissionRule
import pl.nowinkitransferowe.core.ui.NewsFeedUiState


class NewsScreenTest {

    @get:Rule(order = 0)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()


    @Test
    fun circularProgressIndicator_whenScreenIsLoading_exists() {
        composeTestRule.setContent {
            NewsScreen(
                isSyncing = false,
                feedState = NewsFeedUiState.Loading,
                onNewsClick = {},
                onTopicClick = {},
                loadNextPage = {},
                onNewsResourcesCheckedChanged = { _, _ -> },
                onNewsSelected = {},
                onNewsResourceViewed = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_news_loading),
            )
            .assertExists()
    }

    @Test
    fun circularProgressIndicator_whenScreenIsSyncing_exists() {
        composeTestRule.setContent {

            NewsScreen(
                isSyncing = true,
                feedState = NewsFeedUiState.Success(emptyList()),
                onNewsClick = {},
                onTopicClick = {},
                loadNextPage = {},
                onNewsResourcesCheckedChanged = { _, _ -> },
                onNewsSelected = {},
                onNewsResourceViewed = {},
            )

        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_news_loading),
            )
            .assertExists()
    }

    @Test
    fun feed_whenLoaded_showsFeed() {
        composeTestRule.setContent {
            NewsScreen(
                isSyncing = false,
                feedState = NewsFeedUiState.Success(feed = userNewsResourcesTestData),
                onNewsClick = {},
                onTopicClick = {},
                loadNextPage = {},
                onNewsResourcesCheckedChanged = { _, _ -> },
                onNewsSelected = {},
                onNewsResourceViewed = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                userNewsResourcesTestData[0].title,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    userNewsResourcesTestData[1].title,
                    substring = true,
                ),
            )

        composeTestRule
            .onNodeWithText(
                userNewsResourcesTestData[1].title,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun feed_whenLoadNextPageExecuted_showsFeedWithNewItems() {
        composeTestRule.setContent {
            NewsScreen(
                isSyncing = false,
                feedState = NewsFeedUiState.Success(feed = userNewsResourcesTestData),
                onNewsClick = {},
                onTopicClick = {},
                loadNextPage = {},
                onNewsResourcesCheckedChanged = { _, _ -> },
                onNewsSelected = {},
                onNewsResourceViewed = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                userNewsResourcesTestData[0].title,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    userNewsResourcesTestData[4].title,
                    substring = true,
                ),
            )
        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    userNewsResourcesTestData[9].title,
                    substring = true,
                ),
            )

        composeTestRule
            .onNodeWithText(
                userNewsResourcesTestData[9].title,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()
    }
}