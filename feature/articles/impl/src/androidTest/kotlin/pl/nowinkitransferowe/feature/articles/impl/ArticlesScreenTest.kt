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

package pl.nowinkitransferowe.feature.articles.impl

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.data.userArticlesTestData
import pl.nowinkitransferowe.core.testing.data.userTransfersTestData
import pl.nowinkitransferowe.core.testing.rules.GrantPostNotificationsPermissionRule
import kotlin.test.assertEquals

class ArticlesScreenTest {

    @get:Rule(order = 0)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule()

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val state = HomeUiState.Success(
        tab = HomeTab.ALL,
        topic = null,
        topics = topicsTestData,
        featured = userArticlesTestData.first(),
        latest = userArticlesTestData.drop(1),
        transfers = userTransfersTestData,
        rumours = rumoursTestData,
        hasMore = false,
    )

    @Test
    fun featuredArticleCarouselsAndLatestAreShown() {
        composeTestRule.setContent { Screen(state) }

        composeTestRule.onNodeWithText(userArticlesTestData.first().title.uppercase()).assertExists()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.feature_articles_impl_transfer_window)).assertExists()
        composeTestRule.onNodeWithTag("news:feed").performScrollToNode(hasText(userArticlesTestData.last().title))
        composeTestRule.onNodeWithText(userArticlesTestData.last().title).assertExists()
    }

    @Test
    fun selectedTabIsMarkedAndTabClicksAreReported() {
        var selected: HomeTab? = null
        composeTestRule.setContent { Screen(state.copy(tab = HomeTab.RUMOURS), onTabSelected = { selected = it }) }

        composeTestRule.onNode(tab(R.string.feature_articles_impl_tab_rumours)).assertIsSelected()
        composeTestRule.onNode(tab(R.string.feature_articles_impl_tab_official)).performClick()

        assertEquals(HomeTab.OFFICIAL, selected)
    }

    @Test
    fun articleClickReportsItsId() {
        var clicked: Int? = null
        composeTestRule.setContent { Screen(state, onArticleClick = { clicked = it }) }

        composeTestRule.onNodeWithText(userArticlesTestData.first().title.uppercase()).performClick()

        assertEquals(userArticlesTestData.first().id, clicked)
    }

    /** A tab with the given label; the rumours section below has the same "Plotki" title. */
    private fun tab(label: Int) = hasText(composeTestRule.activity.getString(label)) and
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    @Composable
    private fun Screen(
        uiState: HomeUiState,
        onTabSelected: (HomeTab) -> Unit = {},
        onArticleClick: (Int) -> Unit = {},
    ) {
        ArticlesScreen(
            uiState = uiState,
            isSyncing = false,
            onTabSelected = onTabSelected,
            onTopicSelected = {},
            onArticleClick = onArticleClick,
            onArticleSavedChanged = { _, _ -> },
            onTransferClick = {},
            onAllTransfersClick = {},
            onRumourClick = {},
            onAllRumoursClick = {},
            loadNextPage = {},
        )
    }
}
