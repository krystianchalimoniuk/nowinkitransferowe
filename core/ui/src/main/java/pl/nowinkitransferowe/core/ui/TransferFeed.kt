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
import pl.nowinkitransferowe.core.model.UserTransferResource

/**
 * An extension on [LazyListScope] defining a feed with news resources.
 * Depending on the [feedState], this might emit no items.
 */
@OptIn(ExperimentalFoundationApi::class)
fun LazyStaggeredGridScope.transferFeed(
    feedState: TransferFeedUiState,
    highlightSelectedTransfer: Boolean = false,
    onTransferSelected: (String) -> Unit = {},
    selectedTransferId: String? = null,
    onTransferResourcesCheckedChanged: (String, Boolean) -> Unit,
    onTransferResourceViewed: (String) -> Unit,
    onTransferClick: (String) -> Unit = {},
) {
    when (feedState) {
        TransferFeedUiState.Loading -> Unit
        is TransferFeedUiState.Success -> {
            items(
                items = feedState.feed,
                key = { "transfer-${it.id}" },
                contentType = { "transferFeedItem" },
            ) { userTransferResource ->
                val analyticsHelper = LocalAnalyticsHelper.current

                TransferResourceCardExpanded(
                    userTransferResource = userTransferResource,
                    isBookmarked = userTransferResource.isSaved,
                    selectedTransferId = selectedTransferId,
                    highlightSelectedTransfer = highlightSelectedTransfer,
                    onClick = {
                        analyticsHelper.logTransferResourceOpened(
                            transferResourceId = userTransferResource.id,
                        )
                        onTransferSelected(userTransferResource.id)
                        onTransferClick(userTransferResource.id)
                        onTransferResourceViewed(userTransferResource.id)
                    },
                    hasBeenViewed = userTransferResource.hasBeenViewed,
                    onToggleBookmark = {
                        onTransferResourcesCheckedChanged(
                            userTransferResource.id,
                            !userTransferResource.isSaved,
                        )
                    },
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
sealed interface TransferFeedUiState {
    /**
     * The feed is still loading.
     */
    data object Loading : TransferFeedUiState

    /**
     * The feed is loaded with the given list of news resources.
     */
    data class Success(
        /**
         * The list of news resources contained in this feed.
         */
        val feed: List<UserTransferResource>,
    ) : TransferFeedUiState
}
