/*
 * Copyright 2026 The Android Open Source Project
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.savedUserArticlesTestData
import pl.nowinkitransferowe.core.testing.data.savedUserTransfersTestData
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for [BookmarksScreen] composable.
 */
class BookmarksScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun string(id: Int, vararg args: Any) = composeTestRule.activity.resources.getString(id, *args)

    private fun state(tab: SavedTab, articles: Boolean = true, transfers: Boolean = true) = BookmarksUiState.Success(
        tab = tab,
        articles = if (articles) savedUserArticlesTestData else emptyList(),
        transfers = if (transfers) savedUserTransfersTestData else emptyList(),
    )

    private val unbookmark get() = string(pl.nowinkitransferowe.core.ui.R.string.core_ui_unbookmark)

    private fun tab(label: Int, count: Int) = hasText(string(R.string.feature_bookmarks_impl_tab, string(label), count)) and
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    @Test
    fun loading_showsLoadingSpinner() {
        composeTestRule.setContent {
            BookmarksScreen(
                uiState = BookmarksUiState.Loading,
                onTabSelected = {},
                onShowSnackbar = { _, _ -> false },
                removeArticleBookmark = {},
                removeTransferBookmark = {},
                onArticleClick = {},
                onTransferClick = {},
            )
        }

        composeTestRule.onNodeWithContentDescription(string(R.string.feature_bookmarks_impl_loading)).assertExists()
    }

    @Test
    fun articlesTab_showsArticlesAndCountsAndReportsTabClicks() {
        var selected: SavedTab? = null
        composeTestRule.setContent {
            BookmarksScreen(
                uiState = state(SavedTab.ARTICLES),
                onTabSelected = { selected = it },
                onShowSnackbar = { _, _ -> false },
                removeArticleBookmark = {},
                removeTransferBookmark = {},
                onArticleClick = {},
                onTransferClick = {},
            )
        }

        composeTestRule.onNode(tab(R.string.feature_bookmarks_impl_articles, savedUserArticlesTestData.size)).assertIsSelected()
        composeTestRule.onNodeWithText(savedUserArticlesTestData[0].title).assertExists()
        composeTestRule.onNodeWithTag("transfer-item:${savedUserTransfersTestData[0].id}").assertDoesNotExist()

        composeTestRule.onNode(tab(R.string.feature_bookmarks_impl_transfers, savedUserTransfersTestData.size)).performClick()
        assertEquals(SavedTab.TRANSFERS, selected)
    }

    @Test
    fun transfersTab_removingBookmark_reportsTransfer() {
        var removed: Int? = null
        composeTestRule.setContent {
            BookmarksScreen(
                uiState = state(SavedTab.TRANSFERS),
                onTabSelected = {},
                onShowSnackbar = { _, _ -> false },
                removeArticleBookmark = {},
                removeTransferBookmark = { removed = it },
                onArticleClick = {},
                onTransferClick = {},
            )
        }

        composeTestRule.onNodeWithTag("transfer-item:${savedUserTransfersTestData[0].id}").assertExists()
        composeTestRule.onAllNodesWithContentDescription(unbookmark).onFirst().performClick()
        assertEquals(savedUserTransfersTestData[0].id, removed)
    }

    @Test
    fun articlesTab_removingBookmark_reportsArticle() {
        var removed: Int? = null
        composeTestRule.setContent {
            BookmarksScreen(
                uiState = state(SavedTab.ARTICLES),
                onTabSelected = {},
                onShowSnackbar = { _, _ -> false },
                removeArticleBookmark = { removed = it },
                removeTransferBookmark = {},
                onArticleClick = {},
                onTransferClick = {},
            )
        }

        composeTestRule.onAllNodesWithContentDescription(unbookmark).onFirst().performClick()
        assertEquals(savedUserArticlesTestData[0].id, removed)
    }

    @Test
    fun emptyTab_showsEmptyState() {
        composeTestRule.setContent {
            BookmarksScreen(
                uiState = state(SavedTab.TRANSFERS, transfers = false),
                onTabSelected = {},
                onShowSnackbar = { _, _ -> false },
                removeArticleBookmark = {},
                removeTransferBookmark = {},
                onArticleClick = {},
                onTransferClick = {},
            )
        }

        composeTestRule.onNodeWithText(string(R.string.feature_bookmarks_impl_empty_transfers)).assertExists()
    }

    @Test
    fun whenLifecycleStops_undoBookmarkedStateIsCleared() = runTest {
        var undoStateCleared = false
        val testLifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.STARTED)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides testLifecycleOwner) {
                BookmarksScreen(
                    uiState = state(SavedTab.ARTICLES),
                    onTabSelected = {},
                    onShowSnackbar = { _, _ -> false },
                    removeArticleBookmark = {},
                    removeTransferBookmark = {},
                    onArticleClick = {},
                    onTransferClick = {},
                    clearUndoState = { undoStateCleared = true },
                )
            }
        }

        assertEquals(false, undoStateCleared)
        testLifecycleOwner.handleLifecycleEvent(event = Lifecycle.Event.ON_STOP)
        assertTrue(undoStateCleared)
    }
}
