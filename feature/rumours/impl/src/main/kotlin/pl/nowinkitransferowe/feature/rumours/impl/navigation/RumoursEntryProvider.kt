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

package pl.nowinkitransferowe.feature.rumours.impl.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.feature.articledetails.api.navigation.navigateToArticle
import pl.nowinkitransferowe.feature.club.api.navigation.navigateToClub
import pl.nowinkitransferowe.feature.player.api.navigation.navigateToPlayer
import pl.nowinkitransferowe.feature.rumours.api.navigation.RumourNavKey
import pl.nowinkitransferowe.feature.rumours.api.navigation.RumoursNavKey
import pl.nowinkitransferowe.feature.rumours.api.navigation.navigateToRumour
import pl.nowinkitransferowe.feature.rumours.impl.RumourRoute
import pl.nowinkitransferowe.feature.rumours.impl.RumourViewModel
import pl.nowinkitransferowe.feature.rumours.impl.RumoursRoute
import pl.nowinkitransferowe.feature.transferdetails.api.navigation.navigateToTransfer

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.rumoursEntry(
    navigator: Navigator,
    detailPlaceholder: @Composable () -> Unit,
) {
    entry<RumoursNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { detailPlaceholder() }),
    ) {
        RumoursRoute(onRumourClick = navigator::navigateToRumour)
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.rumourEntry(navigator: Navigator) {
    entry<RumourNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(),
    ) { key ->
        RumourRoute(
            showBackButton = true,
            onBackClick = { navigator.goBack() },
            onArticleClick = navigator::navigateToArticle,
            onTransferClick = navigator::navigateToTransfer,
            onClubClick = navigator::navigateToClub,
            onPlayerClick = navigator::navigateToPlayer,
            viewModel = hiltViewModel<RumourViewModel, RumourViewModel.Factory>(
                key = key.rumourId.toString(),
            ) { factory ->
                factory.create(key.rumourId)
            },
        )
    }
}
