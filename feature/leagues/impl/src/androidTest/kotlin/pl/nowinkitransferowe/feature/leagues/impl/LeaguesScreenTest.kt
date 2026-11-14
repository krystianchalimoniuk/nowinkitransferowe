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

package pl.nowinkitransferowe.feature.leagues.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.SeasonOption
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.leagueSeasonsTestData
import pl.nowinkitransferowe.core.testing.data.leaguesTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import kotlin.test.assertEquals

/**
 * UI tests for [LeaguesScreen] and [LeagueScreen].
 */
class LeaguesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val names = topicsTestData.associate { it.slug to it.name }

    @Test
    fun leagues_reportTheSeasonAndTheLeagueClicked() {
        var season: Season? = null
        var league: Pair<String, Season>? = null
        composeTestRule.setContent {
            LeaguesScreen(
                uiState = LeaguesUiState.Success(Season(2026), leagueSeasonsTestData, leaguesTestData, names),
                onSeasonSelected = { season = it },
                onLeagueClick = { topic, picked -> league = topic to picked },
            )
        }

        composeTestRule.onNodeWithText("25/26").performClick()
        assertEquals(Season(2025), season)
        composeTestRule.onNodeWithTag("league:serie-a").performClick()
        assertEquals("serie-a" to Season(2026), league)
    }

    @Test
    fun oneSeason_hidesTheSeasonSwitch() {
        composeTestRule.setContent {
            LeaguesScreen(
                uiState = LeaguesUiState.Success(Season(2026), listOf(SeasonOption(Season(2026), true)), leaguesTestData, names),
                onSeasonSelected = {},
                onLeagueClick = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("26/27").assertDoesNotExist()
    }

    @Test
    fun league_opensItsClubs() {
        var club: Int? = null
        composeTestRule.setContent {
            LeagueScreen(
                uiState = LeagueUiState.Success(
                    season = Season(2026),
                    league = leaguesTestData[0],
                    allLeagues = leaguesTestData,
                    names = names,
                    clubs = clubsTestData.associateBy(Club::id),
                    topArrivals = emptyList(),
                    topDepartures = emptyList(),
                ),
                showBackButton = true,
                onBackClick = {},
                onClubClick = { club = it },
                onTransferClick = {},
            )
        }

        composeTestRule.onNodeWithTag("league-club:83").performClick()
        assertEquals(83, club)
    }
}
