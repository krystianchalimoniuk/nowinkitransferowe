package pl.nowinkitransferowe.core.domain

import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.data.model.RecentSearchQuery
import pl.nowinkitransferowe.core.data.repository.RecentSearchRepository
import javax.inject.Inject

/**
 * A use case which returns the recent search queries.
 */
class GetRecentSearchQueriesUseCase @Inject constructor(
    private val recentSearchRepository: RecentSearchRepository,
) {
    operator fun invoke(limit: Int = 10): Flow<List<RecentSearchQuery>> =
        recentSearchRepository.getRecentSearchQueries(limit)
}
