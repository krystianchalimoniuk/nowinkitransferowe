package pl.nowinkitransferowe.feature.bookmarks

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import kotlinx.coroutines.test.runTest

import org.junit.Test

import org.junit.Rule
import pl.nowinkitransferowe.core.testing.data.userNewsResourcesTestData
import pl.nowinkitransferowe.core.testing.data.userTransferResourcesTestData
import pl.nowinkitransferowe.core.ui.NewsFeedUiState
import pl.nowinkitransferowe.core.ui.TransferFeedUiState

/**
 * UI tests for [BookmarksScreen] composable.
 */
class BookmarksScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loading_showsLoadingSpinner() {
        composeTestRule.setContent {
            BookmarksScreen(

                newsFeedState = NewsFeedUiState.Loading,
                transferFeedState = TransferFeedUiState.Loading,
                onShowSnackbar = { _, _ -> false },
                removeFromNewsBookmarks = {},
                removeFromTransferBookmarks = {},
                onTopicClick = {},
                onNewsClick = {},
                onTransferResourceViewed = {},
                onNewsResourceViewed = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_bookmarks_loading),
            )
            .assertExists()
    }

    @Test
    fun feed_whenHasBookmarks_showsBookmarks() {
        composeTestRule.setContent {
            BookmarksScreen(
                newsFeedState = NewsFeedUiState.Success(
                    userNewsResourcesTestData.take(2),
                ),
                transferFeedState = TransferFeedUiState.Success(
                    feed = userTransferResourcesTestData.take(
                        2
                    )
                ),
                onShowSnackbar = { _, _ -> false },
                removeFromNewsBookmarks = {},
                removeFromTransferBookmarks = {},
                onTopicClick = {},
                onNewsResourceViewed = {},
                onTransferResourceViewed = {},
                onNewsClick = {},
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

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    userTransferResourcesTestData[0].name,
                    substring = true,
                ),
            )
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
    fun feed_whenRemovingBookmark_removesBookmark() {
        var removeNewsFromBookmarksCalled = false
        var removeTransferFromBookmarksCalled = false

        composeTestRule.setContent {
            BookmarksScreen(
                newsFeedState = NewsFeedUiState.Success(
                    userNewsResourcesTestData.take(2),
                ),
                transferFeedState = TransferFeedUiState.Success(
                    feed = userTransferResourcesTestData.take(
                        2
                    )
                ),
                onShowSnackbar = { _, _ -> false },
                removeFromNewsBookmarks = { newsResourceId ->
                    kotlin.test.assertEquals(userNewsResourcesTestData[0].id, newsResourceId)
                    removeNewsFromBookmarksCalled = true
                },
                removeFromTransferBookmarks = { transferResourceId ->
                    kotlin.test.assertEquals(
                        userTransferResourcesTestData[0].id,
                        transferResourceId
                    )
                    removeTransferFromBookmarksCalled = true
                },
                onTopicClick = {},
                onNewsResourceViewed = {},
                onTransferResourceViewed = {},
                onNewsClick = {}
            )
        }

        composeTestRule
            .onAllNodesWithContentDescription(
                composeTestRule.activity.getString(
                    pl.nowinkitransferowe.core.ui.R.string.core_ui_unbookmark,
                ),
            ).filter(
                hasAnyAncestor(
                    hasText(
                        userNewsResourcesTestData[0].title,
                        substring = true,
                    ),
                )
            )
            .assertCountEquals(1)
            .onFirst()
            .performClick()

        kotlin.test.assertTrue(removeNewsFromBookmarksCalled)

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    composeTestRule.activity.getString(R.string.feature_bookmarks_transfers),
                    substring = true,
                ),
            )

        composeTestRule
            .onAllNodesWithContentDescription(
                composeTestRule.activity.getString(
                    pl.nowinkitransferowe.core.ui.R.string.core_ui_unbookmark,
                ),
            ).filter(
                hasAnyAncestor(
                    hasText(
                        userTransferResourcesTestData[0].name,
                        substring = true,
                    ),
                )
            )
            .assertCountEquals(1)
            .onFirst()
            .performClick()

        kotlin.test.assertTrue(removeTransferFromBookmarksCalled)
    }

    @Test
    fun feed_whenHasNoBookmarks_showsEmptyState() {
        composeTestRule.setContent {
            BookmarksScreen(
                newsFeedState = NewsFeedUiState.Success(emptyList()),
                transferFeedState = TransferFeedUiState.Success(emptyList()),
                onShowSnackbar = { _, _ -> false },
                removeFromNewsBookmarks = {},
                removeFromTransferBookmarks = {},
                onNewsClick = {},
                onTopicClick = {},
                onNewsResourceViewed = {},
                onTransferResourceViewed = {}
            )
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.feature_bookmarks_empty_error),
            )
            .assertExists()

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.feature_bookmarks_empty_description),
            )
            .assertExists()
    }

    @Test
    fun feed_whenLifecycleStops_undoBookmarkedStateIsCleared() = runTest {
        var undoStateCleared = false
        val testLifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.STARTED)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides testLifecycleOwner) {
                BookmarksScreen(
                    newsFeedState = NewsFeedUiState.Success(emptyList()),
                    transferFeedState = TransferFeedUiState.Success(emptyList()),
                    onShowSnackbar = { _, _ -> false },
                    removeFromNewsBookmarks = {},
                    removeFromTransferBookmarks = {},
                    onTopicClick = {},
                    onNewsClick = {},
                    onNewsResourceViewed = {},
                    onTransferResourceViewed = {},
                    clearUndoState = {
                        undoStateCleared = true
                    },
                )
            }
        }

        kotlin.test.assertEquals(false, undoStateCleared)
        testLifecycleOwner.handleLifecycleEvent(event = Lifecycle.Event.ON_STOP)
        kotlin.test.assertEquals(true, undoStateCleared)
    }
}
