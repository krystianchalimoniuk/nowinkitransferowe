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

package pl.nowinkitransferowe.feature.rumours.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.fixedRumoursTestData
import kotlin.test.assertEquals

/**
 * UI tests for [RumourScreen].
 */
class RumourScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun string(id: Int) = composeTestRule.activity.resources.getString(id)

    private var articleClicked: Int? = null
    private var transferClicked: Int? = null
    private var bookmarked: Boolean? = null

    private fun setContent(rumourIndex: Int) {
        val rumour = fixedRumoursTestData[rumourIndex]
        composeTestRule.setContent {
            RumourScreen(
                uiState = RumourUiState.Success(
                    rumour = rumour,
                    isSaved = false,
                    player = null,
                    articles = articlesTestData.associateBy(Article::id),
                ),
                showBackButton = true,
                onBackClick = {},
                onBookmarkChanged = { bookmarked = it },
                onArticleClick = { articleClicked = it },
                onTransferClick = { transferClicked = it },
            )
        }
    }

    @Test
    fun history_showsTheArticlesAndStepsWithoutOne() {
        setContent(0)
        val withArticle = articlesTestData.first { it.id == 11282 }.title
        composeTestRule.onNodeWithText(string(R.string.feature_rumours_impl_no_article)).performScrollTo().assertExists()
        composeTestRule.onNodeWithText(withArticle).performScrollTo().performClick()
        assertEquals(11282, articleClicked)
    }

    @Test
    fun readButton_opensTheLatestArticle() {
        setContent(0)
        composeTestRule.onNodeWithTag("rumour:read").performClick()
        assertEquals(11284, articleClicked)
    }

    @Test
    fun bookmark_isReported() {
        setContent(0)
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(pl.nowinkitransferowe.core.ui.R.string.core_ui_bookmark),
        ).performClick()
        assertEquals(true, bookmarked)
    }

    @Test
    fun rumourWithTransfer_linksToIt() {
        setContent(2)
        composeTestRule.onNodeWithText(string(R.string.feature_rumours_impl_see_transfer)).performClick()
        assertEquals(101, transferClicked)
        composeTestRule.onNodeWithTag("rumour:read").assertDoesNotExist()
    }
}
