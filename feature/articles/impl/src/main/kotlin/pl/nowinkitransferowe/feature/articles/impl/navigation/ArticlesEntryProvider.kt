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

package pl.nowinkitransferowe.feature.articles.impl.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.feature.articledetails.api.navigation.navigateToArticle
import pl.nowinkitransferowe.feature.articles.api.navigation.ArticlesNavKey
import pl.nowinkitransferowe.feature.articles.impl.ArticlesRoute
import pl.nowinkitransferowe.feature.rumours.api.navigation.RumoursNavKey
import pl.nowinkitransferowe.feature.transferdetails.api.navigation.navigateToTransfer
import pl.nowinkitransferowe.feature.transfers.api.navigation.TransfersNavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.articlesEntry(
    navigator: Navigator,
    detailPlaceholder: @Composable () -> Unit,
) {
    entry<ArticlesNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { detailPlaceholder() }),
    ) {
        ArticlesRoute(
            onArticleClick = navigator::navigateToArticle,
            onTransferClick = navigator::navigateToTransfer,
            onAllTransfersClick = { navigator.navigate(TransfersNavKey) },
            onAllRumoursClick = { navigator.navigate(RumoursNavKey) },
        )
    }
}
