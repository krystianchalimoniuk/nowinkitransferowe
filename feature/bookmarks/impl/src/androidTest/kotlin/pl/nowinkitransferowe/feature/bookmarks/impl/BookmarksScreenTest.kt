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

package pl.nowinkitransferowe.feature.bookmarks.impl

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.savedUserArticlesTestData
import pl.nowinkitransferowe.core.testing.data.savedUserTransfersTestData
import pl.nowinkitransferowe.core.ui.ArticleFeedUiState
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
                newsFeedState = ArticleFeedUiState.Loading,
                transferFeedState = TransferFeedUiState.Loading,
                onShowSnackbar = { _, _ -> false },
                removeFromArticleBookmarks = {},
                removeFromTransferBookmarks = {},
                onTopicClick = {},
                onArticleClick = {},
                onTransferViewed = {},
                onArticleViewed = {},
                onTransferClick = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_bookmarks_impl_loading),
            )
            .assertExists()
    }

    @Test
    fun feed_whenHasBookmarks_showsBookmarks() {
        composeTestRule.setContent {
            BookmarksScreen(
                newsFeedState = ArticleFeedUiState.Success(
                    savedUserArticlesTestData.take(2),
                ),
                transferFeedState = TransferFeedUiState.Success(
                    feed = savedUserTransfersTestData.take(
                        2,
                    ),
                ),
                onShowSnackbar = { _, _ -> false },
                removeFromArticleBookmarks = {},
                removeFromTransferBookmarks = {},
                onTopicClick = {},
                onArticleViewed = {},
                onTransferViewed = {},
                onArticleClick = {},
                onTransferClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                savedUserArticlesTestData[0].title,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    savedUserArticlesTestData[1].title,
                    substring = true,
                ),
            )

        composeTestRule
            .onNodeWithText(
                savedUserArticlesTestData[1].title,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    savedUserTransfersTestData[0].player.name,
                    substring = true,
                ),
            )
        composeTestRule
            .onNodeWithText(
                savedUserTransfersTestData[0].player.name,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(
                hasText(
                    savedUserTransfersTestData[1].player.name,
                    substring = true,
                ),
            )

        composeTestRule
            .onNodeWithText(
                savedUserTransfersTestData[1].player.name,
                substring = true,
            )
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun feed_whenRemovingNewsBookmark_removesNewsBookmark() = runTest {
        var removeNewsFromBookmarksCalled = false

        composeTestRule.setContent {
            BookmarksScreen(
                newsFeedState = ArticleFeedUiState.Success(
                    feed = savedUserArticlesTestData.take(2),
                ),
                transferFeedState = TransferFeedUiState.Success(
                    feed = arrayListOf(),
                ),
                onShowSnackbar = { _, _ -> false },
                removeFromArticleBookmarks = { articleId ->
                    assertEquals(savedUserArticlesTestData[0].id, articleId)
                    removeNewsFromBookmarksCalled = true
                },
                removeFromTransferBookmarks = {},
                onTopicClick = {},
                onArticleViewed = {},
                onTransferViewed = {},
                onArticleClick = {},
                onTransferClick = {},
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
                        savedUserArticlesTestData[0].title,
                        substring = true,
                    ),
                ),
            )
            .assertCountEquals(1)
            .onFirst()
            .performClick()

        assertTrue(removeNewsFromBookmarksCalled)
    }

    @Test
    fun feed_whenRemovingTransfersBookmark_removesTransfersBookmark() = runTest {
        var removeTransfersFromBookmarksCalled = false

        composeTestRule.setContent {
            BookmarksScreen(
                newsFeedState = ArticleFeedUiState.Success(
                    feed = arrayListOf(),
                ),
                transferFeedState = TransferFeedUiState.Success(
                    feed = savedUserTransfersTestData.take(
                        2,
                    ),
                ),
                onShowSnackbar = { _, _ -> false },
                removeFromArticleBookmarks = {},
                removeFromTransferBookmarks = { transferId ->
                    assertEquals(savedUserTransfersTestData[0].id, transferId)
                    removeTransfersFromBookmarksCalled = true
                },
                onTopicClick = {},
                onArticleViewed = {},
                onTransferViewed = {},
                onArticleClick = {},
                onTransferClick = {},
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
                        savedUserTransfersTestData[0].player.name,
                        substring = true,
                    ),
                ),
            )
            .assertCountEquals(1)
            .onFirst()
            .performClick()

        assertTrue(removeTransfersFromBookmarksCalled)
    }

    @Test
    fun feed_whenHasNoBookmarks_showsEmptyState() {
        composeTestRule.setContent {
            BookmarksScreen(
                newsFeedState = ArticleFeedUiState.Success(emptyList()),
                transferFeedState = TransferFeedUiState.Success(emptyList()),
                onShowSnackbar = { _, _ -> false },
                removeFromArticleBookmarks = {},
                removeFromTransferBookmarks = {},
                onArticleClick = {},
                onTopicClick = {},
                onArticleViewed = {},
                onTransferViewed = {},
                onTransferClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.feature_bookmarks_impl_empty_error),
            )
            .assertExists()

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.feature_bookmarks_impl_empty_description),
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
                    newsFeedState = ArticleFeedUiState.Success(emptyList()),
                    transferFeedState = TransferFeedUiState.Success(emptyList()),
                    onShowSnackbar = { _, _ -> false },
                    removeFromArticleBookmarks = {},
                    removeFromTransferBookmarks = {},
                    onTopicClick = {},
                    onArticleClick = {},
                    onArticleViewed = {},
                    onTransferViewed = {},
                    onTransferClick = {},
                    clearUndoState = {
                        undoStateCleared = true
                    },
                )
            }
        }

        assertEquals(false, undoStateCleared)
        testLifecycleOwner.handleLifecycleEvent(event = Lifecycle.Event.ON_STOP)
        assertEquals(true, undoStateCleared)
    }
}
