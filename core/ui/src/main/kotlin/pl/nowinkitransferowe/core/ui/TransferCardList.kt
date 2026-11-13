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

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import pl.nowinkitransferowe.core.analytics.LocalAnalyticsHelper
import pl.nowinkitransferowe.core.model.UserTransfer

fun LazyListScope.transferCardItems(
    items: List<UserTransfer>,
    onToggleBookmark: (item: UserTransfer) -> Unit,
    onTransferViewed: (Int) -> Unit,
    itemModifier: Modifier = Modifier,
) = items(
    items = items,
    key = { it.id },
    itemContent = { userTransfer ->
        val analyticsHelper = LocalAnalyticsHelper.current

        TransferCard(
            userTransfer = userTransfer,
            isBookmarked = userTransfer.isSaved,
            hasBeenViewed = userTransfer.hasBeenViewed,
            onToggleBookmark = { onToggleBookmark(userTransfer) },
            onClick = {
                analyticsHelper.logArticleOpened(
                    articleId = userTransfer.id,
                )
                onTransferViewed(userTransfer.id)
            },
            modifier = itemModifier,
        )
    },

)
