package pl.nowinkitransferowe.core.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.database.model.RecentSearchQueryEntity

data class RecentSearchQuery(
    val query: String,
    val queriedDate: Instant = Clock.System.now(),
)

fun RecentSearchQueryEntity.asExternalModel() = RecentSearchQuery(
    query = query,
    queriedDate = queriedDate,
)
