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

package pl.nowinkitransferowe.feature.search.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.feature.details.api.navigation.navigateToNewsDetails
import pl.nowinkitransferowe.feature.details.transfers.api.navigation.navigateToTransferDetails
import pl.nowinkitransferowe.feature.search.SearchRoute
import pl.nowinkitransferowe.feature.search.SearchViewModel
import pl.nowinkitransferowe.feature.search.api.navigation.SearchNavKey

fun EntryProviderScope<NavKey>.searchEntry(navigator: Navigator) {
    entry<SearchNavKey> { key ->
        SearchRoute(
            onBackClick = { navigator.goBack() },
            onNewsClick = navigator::navigateToNewsDetails,
            onTransferClick = navigator::navigateToTransferDetails,
            searchViewModel = hiltViewModel<SearchViewModel, SearchViewModel.Factory> { factory ->
                factory.create(key)
            },
        )
    }
}
