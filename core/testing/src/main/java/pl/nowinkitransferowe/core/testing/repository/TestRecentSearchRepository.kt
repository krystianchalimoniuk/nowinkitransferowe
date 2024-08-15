package pl.nowinkitransferowe.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.nowinkitransferowe.core.data.model.RecentSearchQuery
import pl.nowinkitransferowe.core.data.repository.RecentSearchRepository

class TestRecentSearchRepository : RecentSearchRepository {

    private val cachedRecentSearches: MutableList<RecentSearchQuery> = mutableListOf()

    override fun getRecentSearchQueries(limit: Int): Flow<List<RecentSearchQuery>> =
        flowOf(cachedRecentSearches.sortedByDescending { it.queriedDate }.take(limit))

    override suspend fun insertOrReplaceRecentSearch(searchQuery: String) {
        cachedRecentSearches.add(RecentSearchQuery(searchQuery))
    }

    override suspend fun clearRecentSearches() = cachedRecentSearches.clear()
}