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

package pl.nowinkitransferowe.feature.followed.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.playersTestData
import kotlin.test.assertEquals

/**
 * UI tests for [FollowedScreen].
 */
class FollowedScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun followed_openAndUnfollow() {
        var club: Int? = null
        var player: Int? = null
        var unfollowed: Pair<Int, Boolean>? = null
        composeTestRule.setContent {
            FollowedScreen(
                uiState = FollowedUiState.Success(listOf(FollowedClub(clubsTestData[0], "Premier League")), playersTestData),
                onBackClick = {},
                onClubClick = { club = it },
                onPlayerClick = { player = it },
                onClubFollowedChange = { id, followed -> unfollowed = id to followed },
                onPlayerFollowedChange = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithTag("followed:club:83").performClick()
        assertEquals(83, club)
        composeTestRule.onNodeWithTag("followed:player:10281").performClick()
        assertEquals(10281, player)
        composeTestRule.onNodeWithContentDescription("Przestań obserwować: West Ham United").performClick()
        assertEquals(83 to false, unfollowed)
    }

    @Test
    fun nothingFollowed_explainsTheStar() {
        composeTestRule.setContent {
            FollowedScreen(
                uiState = FollowedUiState.Success(emptyList(), emptyList()),
                onBackClick = {},
                onClubClick = {},
                onPlayerClick = {},
                onClubFollowedChange = { _, _ -> },
                onPlayerFollowedChange = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("Nikogo jeszcze nie obserwujesz").assertExists()
    }
}
