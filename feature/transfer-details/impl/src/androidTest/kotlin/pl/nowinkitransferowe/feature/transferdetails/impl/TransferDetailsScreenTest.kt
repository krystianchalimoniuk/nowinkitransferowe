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

package pl.nowinkitransferowe.feature.transferdetails.impl

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.playersTestData
import pl.nowinkitransferowe.core.testing.data.userTransfersTestData
import kotlin.test.assertEquals

class TransferDetailsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val fabianski = userTransfersTestData.filter { it.player.id == 832 }
    private val state = TransferDetailsUiState.Success(transfer = fabianski.first(), player = null, history = fabianski)

    @Test
    fun loadingWheelIsShownWhileTheTransferLoads() {
        composeTestRule.setContent { Screen(TransferDetailsUiState.Loading) }

        composeTestRule
            .onNodeWithContentDescription(composeTestRule.activity.getString(R.string.feature_transfer_details_impl_loading))
            .assertExists()
    }

    @Test
    fun playerRouteFactsAndHistoryAreShown() {
        composeTestRule.setContent { Screen(state) }

        composeTestRule.onNodeWithText(fabianski.first().player.name.uppercase()).assertExists()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.feature_transfer_details_impl_history)).assertExists()
        composeTestRule.onNodeWithText(fabianski.first().season.label).assertExists()
    }

    @Test
    fun articleAndOtherTransferClicksAreReported() {
        var article: Int? = null
        var transfer: Int? = null
        val varane = userTransfersTestData.first { it.player.id == 10281 }
        composeTestRule.setContent {
            Screen(
                TransferDetailsUiState.Success(varane, playersTestData.first(), history = listOf(varane, fabianski.last())),
                onArticleClick = { article = it },
                onTransferClick = { transfer = it },
            )
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.feature_transfer_details_impl_article)).performClick()
        composeTestRule.onNodeWithText(fabianski.last().player.name).performClick()

        assertEquals(varane.articleId, article)
        assertEquals(fabianski.last().id, transfer)
    }

    @Composable
    private fun Screen(
        uiState: TransferDetailsUiState,
        onArticleClick: (Int) -> Unit = {},
        onTransferClick: (Int) -> Unit = {},
    ) {
        TransferDetailsScreen(
            uiState = uiState,
            showBackButton = true,
            onBackClick = {},
            onBookmarkChanged = { _, _ -> },
            onArticleClick = onArticleClick,
            onTransferClick = onTransferClick,
        )
    }
}
