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

package pl.nowinkitransferowe.feature.transfers.impl

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.data.userTransfersTestData
import pl.nowinkitransferowe.feature.transfers.impl.navigation.TransfersScreen
import pl.nowinkitransferowe.feature.transfers.impl.navigation.TransfersUiState
import kotlin.test.assertEquals

class TransfersScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val state = TransfersUiState.Success(
        season = Season(2026),
        seasons = listOf(Season(2026), Season(2025), Season(2024)),
        topic = null,
        leagues = topicsTestData,
        transfers = userTransfersTestData,
        hasMore = false,
    )

    @Test
    fun transfersAndFiltersAreShown() {
        composeTestRule.setContent { Screen(state) }

        composeTestRule.onNodeWithText(userTransfersTestData.first().player.name).assertExists()
        composeTestRule.onNodeWithText("26/27").assertExists()
        composeTestRule.onNodeWithText("Premier League").assertExists()
    }

    @Test
    fun seasonAndTransferClicksAreReported() {
        var season: Season? = null
        var transfer: Int? = null
        composeTestRule.setContent { Screen(state, onSeasonSelected = { season = it }, onTransferClick = { transfer = it }) }

        composeTestRule.onNodeWithText("24/25").performClick()
        composeTestRule.onNodeWithText(userTransfersTestData.first().player.name).performClick()

        assertEquals(Season(2024), season)
        assertEquals(userTransfersTestData.first().id, transfer)
    }

    @Test
    fun markAsReadReportsTheUnreadTransfers() {
        var marked: List<Int>? = null
        composeTestRule.setContent { Screen(state, onMarkAsRead = { marked = it }) }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.feature_transfers_impl_mark_as_read)).performClick()

        assertEquals(userTransfersTestData.map { it.id }, marked)
    }

    @Composable
    private fun Screen(
        uiState: TransfersUiState,
        onSeasonSelected: (Season) -> Unit = {},
        onTransferClick: (Int) -> Unit = {},
        onMarkAsRead: (List<Int>) -> Unit = {},
    ) {
        TransfersScreen(
            uiState = uiState,
            isSyncing = false,
            onSeasonSelected = onSeasonSelected,
            onTopicSelected = {},
            onTransferClick = onTransferClick,
            onMarkAsRead = onMarkAsRead,
            loadNextPage = {},
        )
    }
}
