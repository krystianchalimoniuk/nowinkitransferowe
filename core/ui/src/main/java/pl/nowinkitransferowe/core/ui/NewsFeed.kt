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

package pl.nowinkitransferowe.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.nowinkitransferowe.core.analytics.LocalAnalyticsHelper
import pl.nowinkitransferowe.core.model.UserNewsResource

/**
 * An extension on [LazyListScope] defining a feed with news resources.
 * Depending on the [feedState], this might emit no items.
 */
@OptIn(ExperimentalFoundationApi::class)
fun LazyStaggeredGridScope.newsFeed(
    feedState: NewsFeedUiState,
    onNewsResourcesCheckedChanged: (String, Boolean) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    selectedNewsId: String? = null,
    highlightSelectedNews: Boolean = false,
    onNewsClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onNewsSelected: (String?) -> Unit,
) {
    when (feedState) {
        NewsFeedUiState.Loading -> Unit
        is NewsFeedUiState.Success -> {
            items(
                items = feedState.feed,
                key = { it.id },
                contentType = { "newsFeedItem" },
            ) { userNewsResource ->
                val analyticsHelper = LocalAnalyticsHelper.current
                NewsResourceCardExpanded(
                    userNewsResource = userNewsResource,
                    isBookmarked = userNewsResource.isSaved,
                    onClick = {
                        analyticsHelper.logNewsResourceOpened(
                            newsResourceId = userNewsResource.id,
                        )
                        onNewsSelected(userNewsResource.id)
                        onNewsClick(userNewsResource.id)
                        onNewsResourceViewed(userNewsResource.id)
                    },
                    hasBeenViewed = userNewsResource.hasBeenViewed,
                    onToggleBookmark = {
                        onNewsResourcesCheckedChanged(
                            userNewsResource.id,
                            !userNewsResource.isSaved,
                        )
                    },
                    selectedNewsId = selectedNewsId,
                    highlightSelectedNews = highlightSelectedNews,
                    onTopicClick = onTopicClick,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .animateItemPlacement(),
                )
            }
        }
    }
}

/**
 * A sealed hierarchy describing the state of the feed of news resources.
 */
sealed interface NewsFeedUiState {
    /**
     * The feed is still loading.
     */
    data object Loading : NewsFeedUiState

    /**
     * The feed is loaded with the given list of news resources.
     */
    data class Success(
        /**
         * The list of news resources contained in this feed.
         */
        val feed: List<UserNewsResource>,
    ) : NewsFeedUiState
}
