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

import android.net.Uri
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import pl.nowinkitransferowe.core.analytics.LocalAnalyticsHelper
import pl.nowinkitransferowe.core.model.UserNewsResource

fun LazyListScope.userNewsResourceCardItems(
    items: List<UserNewsResource>,
    onToggleBookmark: (item: UserNewsResource) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    itemModifier: Modifier = Modifier,
) = items(
    items = items,
    key = { it.id },
    itemContent = { userNewsResource ->
        val resourceUrl = Uri.parse(userNewsResource.imageUrl)
        val backgroundColor = MaterialTheme.colorScheme.background.toArgb()
        val context = LocalContext.current
        val analyticsHelper = LocalAnalyticsHelper.current

        NewsResourceCardExpanded(
            userNewsResource = userNewsResource,
            isBookmarked = userNewsResource.isSaved,
            hasBeenViewed = userNewsResource.hasBeenViewed,
            onToggleBookmark = { onToggleBookmark(userNewsResource) },
            onClick = {
                analyticsHelper.logNewsResourceOpened(
                    newsResourceId = userNewsResource.id,
                )
//                launchCustomChromeTab(context, resourceUrl, backgroundColor)
                onNewsResourceViewed(userNewsResource.id)
            },
            onTopicClick = onTopicClick,
            modifier = itemModifier,
        )
    },
)
