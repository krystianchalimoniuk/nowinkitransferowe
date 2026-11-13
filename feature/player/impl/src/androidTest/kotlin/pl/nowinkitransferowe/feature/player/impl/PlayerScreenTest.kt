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

package pl.nowinkitransferowe.feature.player.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.lewandowskiTestData
import pl.nowinkitransferowe.core.testing.data.lewandowskiTransfersTestData
import pl.nowinkitransferowe.core.testing.data.polandTestData
import kotlin.test.assertEquals

/**
 * UI tests for [PlayerScreen].
 */
class PlayerScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var club: Int? = null
    private var transfer: Int? = null

    private fun setContent(transfers: List<pl.nowinkitransferowe.core.model.UserTransfer> = lewandowskiTransfersTestData) {
        composeTestRule.setContent {
            PlayerScreen(
                uiState = PlayerUiState.Success(lewandowskiTestData, listOf(polandTestData), transfers, emptyList()),
                showBackButton = true,
                onBackClick = {},
                onTransferClick = { transfer = it },
                onClubClick = { club = it },
                onRumourClick = {},
                today = LocalDate(2026, 11, 13),
            )
        }
    }

    @Test
    fun facts_showBirthWithAgeAndTheCurrentClub() {
        setContent()
        composeTestRule.onNodeWithText("Urodzony 21.08.1988, Warszawa (38 lat)").assertExists()
        composeTestRule.onNodeWithText("Chicago Fire · od lip 2026").assertExists()
        composeTestRule.onNodeWithTag("player:current-club").performClick()
        assertEquals(900, club)
    }

    @Test
    fun history_reportsTransferClicks() {
        setContent()
        // The history is lazy, so on a short screen the row exists only after scrolling to it
        composeTestRule.onNodeWithTag("player:${lewandowskiTestData.id}").performScrollToNode(hasTestTag("player-transfer:3"))
        composeTestRule.onNodeWithTag("player-transfer:3").performClick()
        assertEquals(3, transfer)
    }

    @Test
    fun feeChart_isHiddenWithoutPaidTransfers() {
        setContent(lewandowskiTransfersTestData.filter { it.fee.millionsOfEuro == null })
        composeTestRule.onNodeWithTag("player:fees").assertDoesNotExist()
    }
}
