package pl.nowinkitransferowe.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import org.jetbrains.annotations.TestOnly
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.model.SearchResult
import pl.nowinkitransferowe.core.model.TransferResource

class TestSearchContentsRepository  : SearchContentsRepository {

    private val cachedTransferResources = MutableStateFlow(emptyList<TransferResource>())
    private val cachedNewsResources = MutableStateFlow(emptyList<NewsResource>())

    override suspend fun populateFtsData() = Unit

    override fun searchContents(searchQuery: String): Flow<SearchResult> =
        combine(cachedTransferResources, cachedNewsResources) { transfers, news ->
            SearchResult(
                transferResources = transfers.filter {
                    searchQuery in it.name
                },
                newsResources = news.filter {
                    searchQuery in it.title || searchQuery in it.topics
                },
            )
        }

    override fun getSearchContentsCount(): Flow<Int> = combine(cachedTransferResources, cachedNewsResources) { transfers, news -> transfers.size + news.size }

    @TestOnly
    fun addTransferResources(transferResources: List<TransferResource>) = cachedTransferResources.update { it + transferResources }

    @TestOnly
    fun addNewsResources(newsResources: List<NewsResource>) =
        cachedNewsResources.update { it + newsResources }
}
