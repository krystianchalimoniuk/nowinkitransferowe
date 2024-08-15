package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import pl.nowinkitransferowe.core.data.Syncable
import pl.nowinkitransferowe.core.model.NewsResource

/**
 * Encapsulation class for query parameters for [NewsResource]
 */
data class NewsResourceQuery(
    /**
     * Topic ids to filter for. Null means any topic id will match.
     */
    val filterTopicIds: Set<String>? = null,
    /**
     * News ids to filter for. Null means any news id will match.
     */
    val filterNewsIds: Set<String>? = null,
)

/**
 * Data layer implementation for [NewsResource]
 */
interface NewsRepository : Syncable {


    /**
     * Returns available news resources that match the specified [query].
     */
    fun getNewsResources(
        query: NewsResourceQuery = NewsResourceQuery(
            filterTopicIds = null,
            filterNewsIds = null,
        ),
    ): Flow<List<NewsResource>>


    fun getNewsResourcesPages(limit: Int, offset: Int): Flow<List<NewsResource>>

    /**
     * Returns available news resource that match the specified [id].
     */
    fun getNewsResource(id: String): Flow<NewsResource>

    /**
     * Returns counts number.
     */
    fun getNewsCount(): Flow<Int>


}
