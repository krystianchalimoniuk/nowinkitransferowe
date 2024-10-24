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
import pl.nowinkitransferowe.feature.bookmarks.navigation.BookmarksRoute
import pl.nowinkitransferowe.feature.news.navigation.NewsRoute
import pl.nowinkitransferowe.feature.transfers.navigation.TransferRoute
import kotlin.reflect.KClass
import pl.nowinkitransferowe.feature.bookmarks.R as bookmarksR
import pl.nowinkitransferowe.feature.news.R as newsR
import pl.nowinkitransferowe.feature.transfers.R as transferR

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val route: KClass<*>,
) {
    NEWS(
        selectedIcon = NtIcons.Upcoming,
        unselectedIcon = NtIcons.UpcomingBorder,
        iconTextId = newsR.string.feature_news_title,
        titleTextId = R.string.app_name,
        route = NewsRoute::class
    ),
    TRANSFERS(
        selectedIcon = NtIcons.Arrows,
        unselectedIcon = NtIcons.ArrowsBorder,
        iconTextId = transferR.string.feature_transfers_title,
        titleTextId = transferR.string.feature_transfers_title,
        route = TransferRoute::class
    ),
    BOOKMARKS(
        selectedIcon = NtIcons.Bookmarks,
        unselectedIcon = NtIcons.BookmarksBorder,
        iconTextId = bookmarksR.string.feature_bookmarks_title,
        titleTextId = bookmarksR.string.feature_bookmarks_title,
        route = BookmarksRoute::class
    ),
}
