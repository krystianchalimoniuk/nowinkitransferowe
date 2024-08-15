package pl.nowinkitransferowe.core.ui

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import pl.nowinkitransferowe.core.analytics.LocalAnalyticsHelper
import pl.nowinkitransferowe.core.model.UserTransferResource

fun LazyListScope.userTransferResourceCardItem(
    items: List<UserTransferResource>,
    onToggleBookmark: (item: UserTransferResource) -> Unit,
    onTransferResourceViewed: (String) -> Unit,
    itemModifier: Modifier = Modifier
) = items(
    items = items,
    key = { it.id },
    itemContent = { userTransferResource ->
        val analyticsHelper = LocalAnalyticsHelper.current

        TransferResourceCardExpanded(
            userTransferResource = userTransferResource,
            isBookmarked = userTransferResource.isSaved,
            hasBeenViewed = userTransferResource.hasBeenViewed,
            onToggleBookmark = { onToggleBookmark(userTransferResource) },
            onClick = {
                analyticsHelper.logNewsResourceOpened(
                    newsResourceId = userTransferResource.id,
                )
                onTransferResourceViewed(userTransferResource.id)
            },
            modifier = itemModifier,
        )

    }

)