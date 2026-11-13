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

package pl.nowinkitransferowe.feature.transferdetails.impl.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.feature.articledetails.api.navigation.navigateToArticle
import pl.nowinkitransferowe.feature.transferdetails.api.navigation.TransferDetailsNavKey
import pl.nowinkitransferowe.feature.transferdetails.api.navigation.navigateToTransfer
import pl.nowinkitransferowe.feature.transferdetails.impl.TransferDetailsRoute
import pl.nowinkitransferowe.feature.transferdetails.impl.TransferDetailsViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.transferDetailsEntry(navigator: Navigator) {
    entry<TransferDetailsNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(),
    ) { key ->
        TransferDetailsRoute(
            showBackButton = true,
            onBackClick = { navigator.goBack() },
            onArticleClick = navigator::navigateToArticle,
            onTransferClick = navigator::navigateToTransfer,
            viewModel = hiltViewModel<TransferDetailsViewModel, TransferDetailsViewModel.Factory>(
                key = key.transferId.toString(),
            ) { factory ->
                factory.create(key.transferId)
            },
        )
    }
}
