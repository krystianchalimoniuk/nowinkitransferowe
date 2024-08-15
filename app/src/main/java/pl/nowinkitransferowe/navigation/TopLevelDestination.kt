package pl.nowinkitransferowe.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import pl.nowinkitransferowe.R
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.feature.news.R as newsR
import pl.nowinkitransferowe.feature.transfers.R as transferR
import pl.nowinkitransferowe.feature.bookmarks.R as bookmarksR

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
) {
    NEWS(
        selectedIcon = NtIcons.Upcoming,
        unselectedIcon = NtIcons.UpcomingBorder,
        iconTextId = newsR.string.feature_news_title,
        titleTextId = R.string.app_name,
    ),
    TRANSFERS(
        selectedIcon = NtIcons.Arrows,
        unselectedIcon = NtIcons.ArrowsBorder,
        iconTextId = transferR.string.feature_transfers_title,
        titleTextId = transferR.string.feature_transfers_title,
    ),
    BOOKMARKS(
        selectedIcon = NtIcons.Bookmarks,
        unselectedIcon = NtIcons.BookmarksBorder,
        iconTextId = bookmarksR.string.feature_bookmarks_title,
        titleTextId = bookmarksR.string.feature_bookmarks_title,
    ),
}
