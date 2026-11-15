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

package pl.nowinkitransferowe.feature.followed.impl.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.feature.club.api.navigation.navigateToClub
import pl.nowinkitransferowe.feature.followed.api.navigation.FollowedNavKey
import pl.nowinkitransferowe.feature.followed.impl.FollowedRoute
import pl.nowinkitransferowe.feature.player.api.navigation.navigateToPlayer

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.followedEntry(
    navigator: Navigator,
    detailPlaceholder: @Composable () -> Unit,
) {
    entry<FollowedNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { detailPlaceholder() }),
    ) {
        FollowedRoute(
            onBackClick = { navigator.goBack() },
            onClubClick = navigator::navigateToClub,
            onPlayerClick = navigator::navigateToPlayer,
        )
    }
}
