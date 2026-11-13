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

package pl.nowinkitransferowe.feature.club.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.ClubSeason
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.data.userTransfersTestData
import kotlin.test.assertEquals

/**
 * UI tests for [ClubScreen].
 */
class ClubScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun string(id: Int) = composeTestRule.activity.resources.getString(id)

    private val arrivals = userTransfersTestData.filter { (it.to as? ClubRef.Club)?.id == 83 }

    private var tab: ClubTab? = null
    private var transfer: Int? = null

    private fun setContent(tab: ClubTab, seasons: List<ClubSeason>) {
        composeTestRule.setContent {
            ClubScreen(
                uiState = ClubUiState.Success(
                    club = clubsTestData[0],
                    country = Country("gb-eng", "Anglia"),
                    league = topicsTestData[0],
                    tab = tab,
                    seasons = seasons,
                    availableSeasons = seasons.map { it.season },
                    season = seasons.firstOrNull()?.season,
                    arrivals = arrivals,
                    departures = emptyList(),
                    rumours = emptyList(),
                    transferCount = arrivals.size,
                ),
                showBackButton = true,
                onBackClick = {},
                onTabSelected = { this.tab = it },
                onTransferClick = { transfer = it },
                onRumourClick = {},
                onSeasonSelected = {
                    season = it
                    seasonPicked = true
                },
            )
        }
    }

    private var season: Season? = null
    private var seasonPicked = false

    @Test
    fun seasonChips_reportThePick() {
        setContent(ClubTab.ARRIVALS, listOf(ClubSeason(Season(2026), 313.0, 30.0, -283.0, emptyList(), emptyList())))
        composeTestRule.onNodeWithText(string(R.string.feature_club_impl_all_seasons)).performClick()
        assertEquals(true, seasonPicked)
        assertEquals(null, season)
    }

    @Test
    fun header_showsTheFacts() {
        setContent(ClubTab.FINANCES, emptyList())
        composeTestRule.onNodeWithText("Anglia").assertExists()
        composeTestRule.onNodeWithText(topicsTestData[0].name).assertExists()
        composeTestRule.onNodeWithText("1895").assertExists()
        composeTestRule.onNodeWithText(string(R.string.feature_club_impl_no_finances)).assertExists()
    }

    @Test
    fun finances_showTheNewestSeason() {
        setContent(ClubTab.FINANCES, listOf(ClubSeason(Season(2026), 313.0, 30.0, -283.0, emptyList(), emptyList())))
        composeTestRule.onNodeWithText("Wydatki 26/27").assertExists()
    }

    @Test
    fun arrivals_reportTransferClicksAndTabsReportClicks() {
        setContent(ClubTab.ARRIVALS, emptyList())
        composeTestRule.onNodeWithTag("transfer-item:${arrivals[0].id}").performClick()
        assertEquals(arrivals[0].id, transfer)

        composeTestRule.onNode(
            hasText(string(R.string.feature_club_impl_tab_rumours)) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab),
        ).performClick()
        assertEquals(ClubTab.RUMOURS, tab)
    }
}
