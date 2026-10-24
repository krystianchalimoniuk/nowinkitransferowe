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

package pl.nowinkitransferowe.feature.search

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.model.RecentSearchQuery
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserData
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.testing.data.newsResourcesTestData

/**
 * UI test for checking the correct behaviour of the Search screen.
 */
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var clearSearchContentDesc: String
    private lateinit var clearRecentSearchesContentDesc: String
    private lateinit var transfersString: String
    private lateinit var newsString: String
    private lateinit var tryAnotherSearchString: String
    private lateinit var searchNotReadyString: String

    private val userData: UserData = UserData(
        bookmarkedNewsResources = setOf("1", "3"),
        viewedNewsResources = setOf("1", "2", "4"),
        bookmarkedTransferResources = setOf("1", "3", "5"),
        viewedTransferResources = setOf("1", "2", "3"),
        darkThemeConfig = DarkThemeConfig.DARK,
        useDynamicColor = false,
        isNewsNotificationsAllowed = true,
        isTransfersNotificationsAllowed = true,
        isGeneralNotificationAllowed = true,
    )

    @Before
    fun setup() {
        composeTestRule.activity.apply {
            clearSearchContentDesc = getString(R.string.feature_search_clear_search_text_content_desc)
            clearRecentSearchesContentDesc = getString(R.string.feature_search_clear_recent_searches_content_desc)
            transfersString = getString(R.string.feature_search_transfers)
            newsString = getString(R.string.feature_search_news)
            tryAnotherSearchString = getString(R.string.feature_search_result_not_found, "")
            searchNotReadyString = getString(R.string.feature_search_not_ready)
        }
    }

    @Test
    fun searchTextField_isFocused() {
        composeTestRule.setContent {
            SearchScreen()
        }

        composeTestRule
            .onNodeWithTag("searchTextField")
            .assertIsNotFocused()
    }

    @Test
    fun emptySearchResult_emptyScreenIsDisplayed() {
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.Success(),
            )
        }

        composeTestRule
            .onNodeWithText(tryAnotherSearchString)
            .assertIsDisplayed()
    }

    @Test
    fun emptySearchResult_nonEmptyRecentSearches_emptySearchScreenAndRecentSearchesAreDisplayed() {
        val recentSearches = listOf("kotlin")
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.Success(),
                recentSearchesUiState = RecentSearchQueriesUiState.Success(
                    recentQueries = recentSearches.map(::RecentSearchQuery),
                ),
            )
        }

        composeTestRule
            .onNodeWithText(tryAnotherSearchString)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(clearRecentSearchesContentDesc)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("kotlin")
            .assertIsDisplayed()
    }

    @Test
    fun searchResultWithNewsResources_firstNewsResourcesIsVisible() {
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.Success(
                    newsResources = newsResourcesTestData.map {
                        UserNewsResource(
                            newsResource = it,
                            userData = userData,
                        )
                    },
                ),
            )
        }

        composeTestRule
            .onNodeWithText(newsString)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(newsResourcesTestData[0].title)
            .assertIsDisplayed()
    }

    @Test
    fun emptyQuery_notEmptyRecentSearches_verifyClearSearchesButton_displayed() {
        val recentSearches = listOf("kotlin", "testing")
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.EmptyQuery,
                recentSearchesUiState = RecentSearchQueriesUiState.Success(
                    recentQueries = recentSearches.map(::RecentSearchQuery),
                ),
            )
        }

        composeTestRule
            .onNodeWithContentDescription(clearRecentSearchesContentDesc)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("kotlin")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("testing")
            .assertIsDisplayed()
    }

    @Test
    fun searchNotReady_verifySearchNotReadyMessageIsVisible() {
        composeTestRule.setContent {
            SearchScreen(
                searchResultUiState = SearchResultUiState.SearchNotReady,
            )
        }

        composeTestRule
            .onNodeWithText(searchNotReadyString)
            .assertIsDisplayed()
    }
}
