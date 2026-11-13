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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.testing.data.fixedRumoursTestData
import pl.nowinkitransferowe.core.testing.data.rumourStatsTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import kotlin.test.assertEquals

/**
 * UI tests for [RumoursScreen].
 */
class RumoursScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun string(id: Int) = composeTestRule.activity.resources.getString(id)

    private var stage: RumourStage? = null
    private var topic: String? = "unset"
    private var clicked: Int? = null

    private fun setContent(state: RumoursUiState) {
        composeTestRule.setContent {
            RumoursScreen(
                uiState = state,
                onTopicSelected = { topic = it },
                onStageSelected = { stage = it },
                onRumourClick = { clicked = it },
            )
        }
    }

    private fun success(showAccuracy: Boolean = true, rumours: Boolean = true) = RumoursUiState.Success(
        topic = null,
        leagues = topicsTestData,
        stage = null,
        rumours = if (rumours) fixedRumoursTestData else emptyList(),
        stats = rumourStatsTestData.copy(showAccuracy = showAccuracy),
    )

    @Test
    fun stats_showAccuracyOnlyWhenTheServerAllowsIt() {
        setContent(success(showAccuracy = true))
        composeTestRule.onNodeWithText(string(R.string.feature_rumours_impl_accuracy)).assertExists()
        composeTestRule.onNodeWithText("72%").assertExists()
    }

    @Test
    fun stats_hideAccuracyBelowTwentyClosedRumours() {
        setContent(success(showAccuracy = false))
        composeTestRule.onNodeWithText(string(R.string.feature_rumours_impl_active)).assertExists()
        composeTestRule.onNodeWithText(string(R.string.feature_rumours_impl_accuracy)).assertDoesNotExist()
    }

    @Test
    fun rumourClick_isReported() {
        setContent(success())
        composeTestRule.onNodeWithTag("rumour-item:12").performClick()
        assertEquals(12, clicked)
    }

    @Test
    fun stageMenu_reportsTheStage() {
        setContent(success())
        composeTestRule.onNodeWithTag("rumours:stage").performClick()
        composeTestRule.onNodeWithText("4. Blisko").performClick()
        assertEquals(RumourStage.CLOSE, stage)
    }

    @Test
    fun leagueChip_reportsTheLeague() {
        setContent(success())
        composeTestRule.onNodeWithText(topicsTestData[0].name).performClick()
        assertEquals(topicsTestData[0].slug, topic)
    }

    @Test
    fun noRumours_showsEmptyText() {
        setContent(success(rumours = false))
        composeTestRule.onNodeWithText(string(R.string.feature_rumours_impl_empty)).assertExists()
    }
}
