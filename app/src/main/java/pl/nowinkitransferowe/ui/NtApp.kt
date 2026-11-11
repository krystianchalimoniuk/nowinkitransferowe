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

package pl.nowinkitransferowe.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import pl.nowinkitransferowe.R
import pl.nowinkitransferowe.core.designsystem.component.NtBackground
import pl.nowinkitransferowe.core.designsystem.component.NtGradientBackground
import pl.nowinkitransferowe.core.designsystem.component.NtNavigationSuiteScaffold
import pl.nowinkitransferowe.core.designsystem.component.NtTopAppBar
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.LocalGradientColors
import pl.nowinkitransferowe.core.navigation.Navigator
import pl.nowinkitransferowe.core.navigation.toEntries
import pl.nowinkitransferowe.feature.bookmarks.navigation.LocalSnackbarHostState
import pl.nowinkitransferowe.feature.bookmarks.navigation.bookmarksEntry
import pl.nowinkitransferowe.feature.details.DetailPlaceholder
import pl.nowinkitransferowe.feature.details.navigation.newsDetailsEntry
import pl.nowinkitransferowe.feature.details.transfers.DetailsTransferPlaceholder
import pl.nowinkitransferowe.feature.details.transfers.navigation.transferDetailsEntry
import pl.nowinkitransferowe.feature.news.navigation.newsEntry
import pl.nowinkitransferowe.feature.search.api.navigation.navigateToSearch
import pl.nowinkitransferowe.feature.search.navigation.searchEntry
import pl.nowinkitransferowe.feature.settings.SettingsDialog
import pl.nowinkitransferowe.feature.transfers.navigation.transfersEntry
import pl.nowinkitransferowe.navigation.TOP_LEVEL_NAV_ITEMS
import pl.nowinkitransferowe.feature.settings.R as settingsR

@Composable
fun NtApp(
    appState: NtAppState,
    modifier: Modifier = Modifier,
    deepLinkKeys: List<NavKey> = emptyList(),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    NtBackground(modifier = modifier) {
        NtGradientBackground(
            gradientColors =
            LocalGradientColors.current,
        ) {
            val snackbarHostState = remember { SnackbarHostState() }

            val isOffline by appState.isOffline.collectAsStateWithLifecycle()

            // If user is not connected to the internet show a snack bar to inform them.
            val notConnectedMessage = stringResource(R.string.not_connected)
            LaunchedEffect(isOffline) {
                if (isOffline) {
                    snackbarHostState.showSnackbar(
                        message = notConnectedMessage,
                        duration = Indefinite,
                    )
                }
            }

            CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                NtApp(
                    appState = appState,
                    showSettingsDialog = showSettingsDialog,
                    onSettingsDismissed = { showSettingsDialog = false },
                    onTopAppBarActionClick = { showSettingsDialog = true },
                    deepLinkKeys = deepLinkKeys,
                    windowAdaptiveInfo = windowAdaptiveInfo,
                )
            }
        }
    }
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
internal fun NtApp(
    appState: NtAppState,
    showSettingsDialog: Boolean,
    onSettingsDismissed: () -> Unit,
    onTopAppBarActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    deepLinkKeys: List<NavKey> = emptyList(),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val unreadNavKeys by appState.topLevelNavKeysWithUnreadResources
        .collectAsStateWithLifecycle()

    val snackbarHostState = LocalSnackbarHostState.current

    val navigator = remember { Navigator(appState.navigationState) }

    // Open the destinations of a deep link, e.g. a notification, once
    LaunchedEffect(deepLinkKeys) {
        deepLinkKeys.forEach(navigator::navigate)
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { onSettingsDismissed() },
        )
    }

    NtNavigationSuiteScaffold(
        navigationSuiteItems = {
            TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
                val hasUnread = unreadNavKeys.contains(navKey)
                val selected = navKey == appState.navigationState.currentTopLevelKey
                item(
                    selected = selected,
                    onClick = { navigator.navigate(navKey) },
                    icon = {
                        Icon(
                            imageVector = navItem.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = navItem.selectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(navItem.iconTextId)) },
                    modifier =
                    Modifier
                        .testTag("NtNavItem")
                        .then(if (hasUnread) Modifier.notificationDot() else Modifier),
                )
            }
        },
        windowAdaptiveInfo = windowAdaptiveInfo,
    ) {
        Scaffold(
            modifier = modifier.semantics {
                testTagsAsResourceId = true
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.exclude(
                            WindowInsets.ime,
                        ),
                    ),
                )
            },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal,
                        ),
                    ),
            ) {
                // Only show the top app bar on top level destinations.
                var shouldShowTopAppBar = false

                if (appState.navigationState.isAtTopLevel) {
                    shouldShowTopAppBar = true

                    val destination = TOP_LEVEL_NAV_ITEMS[appState.navigationState.currentTopLevelKey]
                        ?: error("Top level nav item not found for ${appState.navigationState.currentTopLevelKey}")

                    NtTopAppBar(
                        titleRes = destination.titleTextId,
                        navigationIcon = NtIcons.Search,
                        navigationIconContentDescription = stringResource(
                            id = settingsR.string.feature_settings_impl_top_app_bar_navigation_icon_description,
                        ),
                        actionIcon = NtIcons.Settings,
                        actionIconContentDescription = stringResource(
                            id = settingsR.string.feature_settings_impl_top_app_bar_action_icon_description,
                        ),
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                        onActionClick = {
                            onTopAppBarActionClick()
                        },
                        onNavigationClick = { navigator.navigateToSearch() },
                    )
                }

                Box(
                    // Workaround for https://issuetracker.google.com/338478720
                    modifier = Modifier.consumeWindowInsets(
                        if (shouldShowTopAppBar) {
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                        } else {
                            WindowInsets(0, 0, 0, 0)
                        },
                    ),
                ) {
                    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

                    val entryProvider = entryProvider {
                        newsEntry(navigator, detailPlaceholder = { DetailPlaceholder() })
                        transfersEntry(navigator, detailPlaceholder = { DetailsTransferPlaceholder() })
                        bookmarksEntry(navigator)
                        newsDetailsEntry(navigator)
                        transferDetailsEntry(navigator)
                        searchEntry(navigator)
                    }

                    NavDisplay(
                        entries = appState.navigationState.toEntries(entryProvider),
                        sceneStrategies = listOf(listDetailStrategy),
                        onBack = { navigator.goBack() },
                    )
                }

                // TODO: We may want to add padding or spacer when the snackbar is shown so that
                //  content doesn't display behind it.
            }
        }
    }
}

private fun Modifier.notificationDot(): Modifier = composed {
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    drawWithContent {
        drawContent()
        drawCircle(
            tertiaryColor,
            radius = 5.dp.toPx(),
            // This is based on the dimensions of the NavigationBar's "indicator pill";
            // however, its parameters are private, so we must depend on them implicitly
            // (NavigationBarTokens.ActiveIndicatorWidth = 64.dp)
            center = center + Offset(
                64.dp.toPx() * .45f,
                32.dp.toPx() * -.45f - 6.dp.toPx(),
            ),
        )
    }
}
