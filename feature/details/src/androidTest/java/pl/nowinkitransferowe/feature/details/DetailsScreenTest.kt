/*
 * Copyright 2024 The Android Open Source Project
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

package pl.nowinkitransferowe.feature.details

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.testing.data.userNewsResourcesTestData
import pl.nowinkitransferowe.core.ui.dateFormatted

class DetailsScreenTest {
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun circularProgressIndicator_whenScreenIsLoading_exists() {
        composeTestRule.setContent {
            DetailsScreen(
                detailsUiState = DetailsUiState.Loading,
                showBackButton = true,
                onBackClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.resources.getString(R.string.feature_details_loading),
            )
            .assertExists()
    }

    @Test
    fun feed_whenLoaded_showsDetailsScreen() {
        composeTestRule.setContent {
            DetailsScreen(
                detailsUiState = DetailsUiState.Success(userNewsResourcesTestData.first()),
                showBackButton = true,
                onBackClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            )
        }

        composeTestRule
            .onNodeWithText(
                userNewsResourcesTestData[0].title,
                substring = true,
            )
            .assertExists()
    }

    @Test
    fun testMetaDataDisplay_withCategory() {
        val newsWithKnownResourceType = userNewsResourcesTestData[0]
        lateinit var dateFormatted: String

        composeTestRule.setContent {
            DetailsScreen(
                detailsUiState = DetailsUiState.Success(userNewsResourcesTestData.first()),
                showBackButton = true,
                onBackClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            )

            dateFormatted = dateFormatted(publishDate = newsWithKnownResourceType.publishDate)
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(
                    pl.nowinkitransferowe.core.ui.R.string.core_ui_card_meta_data_text,
                    dateFormatted,
                    newsWithKnownResourceType.category.combineNameWithEmoji(),
                ),
            )
            .assertExists()
    }
}
