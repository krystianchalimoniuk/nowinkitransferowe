package pl.nowinkitransferowe.data.test.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.model.SearchResult
import javax.inject.Inject

/**
 * Fake implementation of the [SearchContentsRepository]
 */
internal class FakeSearchContentsRepository @Inject constructor() : SearchContentsRepository {

    override suspend fun populateFtsData() = Unit
    override fun searchContents(searchQuery: String): Flow<SearchResult> = flowOf()
    override fun getSearchContentsCount(): Flow<Int> = flowOf(1)
}