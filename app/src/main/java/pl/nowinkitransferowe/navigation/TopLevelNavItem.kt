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

package pl.nowinkitransferowe.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import pl.nowinkitransferowe.R
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.feature.bookmarks.api.navigation.BookmarksNavKey
import pl.nowinkitransferowe.feature.news.api.navigation.NewsNavKey
import pl.nowinkitransferowe.feature.transfers.api.navigation.TransfersNavKey
import pl.nowinkitransferowe.feature.bookmarks.R as bookmarksR
import pl.nowinkitransferowe.feature.news.R as newsR
import pl.nowinkitransferowe.feature.transfers.R as transferR

/**
 * Type for the top level navigation items in the application. Contains UI information about the
 * current route that is used in the top app bar and common navigation UI.
 *
 * @param selectedIcon The icon to be displayed in the navigation UI when this destination is
 * selected.
 * @param unselectedIcon The icon to be displayed in the navigation UI when this destination is
 * not selected.
 * @param iconTextId Text that to be displayed in the navigation UI.
 * @param titleTextId Text that is displayed on the top app bar.
 */
data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
)

val NEWS = TopLevelNavItem(
    selectedIcon = NtIcons.Upcoming,
    unselectedIcon = NtIcons.UpcomingBorder,
    iconTextId = newsR.string.feature_news_impl_title,
    titleTextId = R.string.app_name,
)

val TRANSFERS = TopLevelNavItem(
    selectedIcon = NtIcons.Arrows,
    unselectedIcon = NtIcons.ArrowsBorder,
    iconTextId = transferR.string.feature_transfers_impl_title,
    titleTextId = transferR.string.feature_transfers_impl_title,
)

val BOOKMARKS = TopLevelNavItem(
    selectedIcon = NtIcons.Bookmarks,
    unselectedIcon = NtIcons.BookmarksBorder,
    iconTextId = bookmarksR.string.feature_bookmarks_impl_title,
    titleTextId = bookmarksR.string.feature_bookmarks_impl_title,
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    NewsNavKey to NEWS,
    TransfersNavKey to TRANSFERS,
    BookmarksNavKey to BOOKMARKS,
)
