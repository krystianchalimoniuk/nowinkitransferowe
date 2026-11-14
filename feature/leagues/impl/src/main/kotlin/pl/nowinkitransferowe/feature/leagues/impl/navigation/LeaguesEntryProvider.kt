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

package pl.nowinkitransferowe.feature.leagues.impl.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.feature.club.api.navigation.navigateToClub
import pl.nowinkitransferowe.feature.leagues.api.navigation.LeagueNavKey
import pl.nowinkitransferowe.feature.leagues.api.navigation.LeaguesNavKey
import pl.nowinkitransferowe.feature.leagues.api.navigation.navigateToLeague
import pl.nowinkitransferowe.feature.leagues.impl.LeagueRoute
import pl.nowinkitransferowe.feature.leagues.impl.LeagueViewModel
import pl.nowinkitransferowe.feature.leagues.impl.LeaguesRoute
import pl.nowinkitransferowe.feature.transferdetails.api.navigation.navigateToTransfer

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.leaguesEntry(
    navigator: Navigator,
    detailPlaceholder: @Composable () -> Unit,
) {
    entry<LeaguesNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { detailPlaceholder() }),
    ) {
        LeaguesRoute(onLeagueClick = { topic, season -> navigator.navigateToLeague(topic, season.startYear) })
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.leagueEntry(navigator: Navigator) {
    entry<LeagueNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(),
    ) { key ->
        LeagueRoute(
            showBackButton = true,
            onBackClick = { navigator.goBack() },
            onClubClick = navigator::navigateToClub,
            onTransferClick = navigator::navigateToTransfer,
            viewModel = hiltViewModel<LeagueViewModel, LeagueViewModel.Factory>(key = "${key.topic}/${key.seasonStartYear}") { factory ->
                factory.create(key.topic, key.seasonStartYear)
            },
        )
    }
}
