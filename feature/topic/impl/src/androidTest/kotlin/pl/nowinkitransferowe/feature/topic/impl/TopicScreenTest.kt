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

package pl.nowinkitransferowe.feature.topic.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.userArticlesTestData
import kotlin.test.assertEquals

/**
 * UI tests for [TopicScreen].
 */
class TopicScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun league_opensTheLeague_articles_andFollows() {
        var league: String? = null
        var article: Int? = null
        var followed: Boolean? = null
        val articles = userArticlesTestData.take(2)
        composeTestRule.setContent {
            TopicScreen(
                uiState = TopicUiState("Ekstraklasa", TopicSubject.League("ekstraklasa"), isFollowed = false, TopicTab.ALL, articles, hasMore = false),
                onBackClick = {},
                onTabSelected = {},
                onFollowedChange = { followed = it },
                onLoadMore = {},
                onArticleClick = { article = it },
                onArticleSavedChange = { _, _ -> },
                onLeagueClick = { league = it },
                onClubClick = {},
                onPlayerClick = {},
            )
        }

        composeTestRule.onNodeWithTag("topic:link").performClick()
        assertEquals("ekstraklasa", league)
        composeTestRule.onNodeWithTag("topic:article:${articles[0].id}").performClick()
        assertEquals(articles[0].id, article)
        composeTestRule.onNodeWithTag("topic:follow").performClick()
        assertEquals(true, followed)
    }
}
