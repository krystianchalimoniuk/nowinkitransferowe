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

package pl.nowinkitransferowe.feature.news.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.feature.details.api.navigation.navigateToNewsDetails
import pl.nowinkitransferowe.feature.news.NewsRoute
import pl.nowinkitransferowe.feature.news.api.navigation.NewsNavKey
import pl.nowinkitransferowe.feature.search.api.navigation.navigateToSearch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.newsEntry(
    navigator: Navigator,
    detailPlaceholder: @Composable () -> Unit,
) {
    entry<NewsNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { detailPlaceholder() }),
    ) {
        NewsRoute(
            onNewsClick = navigator::navigateToNewsDetails,
            onTopicClick = navigator::navigateToSearch,
        )
    }
}
