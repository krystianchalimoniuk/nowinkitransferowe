package pl.nowinkitransferowe.data.test.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.nowinkitransferowe.core.data.model.RecentSearchQuery
import pl.nowinkitransferowe.core.data.repository.RecentSearchRepository
import javax.inject.Inject

/**
 * Fake implementation of the [RecentSearchRepository]
 */
internal class FakeRecentSearchRepository @Inject constructor() : RecentSearchRepository {
    override suspend fun insertOrReplaceRecentSearch(searchQuery: String) = Unit

    override fun getRecentSearchQueries(limit: Int): Flow<List<RecentSearchQuery>> =
        flowOf(emptyList())

    override suspend fun clearRecentSearches() = Unit
}
