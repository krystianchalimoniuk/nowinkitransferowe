package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.model.UserNewsResource


/**
 * Data layer implementation for [UserNewsResource]
 */
interface UserNewsResourceRepository {
    /**
     * Returns available news resources as a stream.
     */
    fun observeAll(
        query: NewsResourceQuery = NewsResourceQuery(
            filterTopicIds = null,
            filterNewsIds = null,
        ),
    ): Flow<List<UserNewsResource>>

    /**
     * Returns available news resources as a stream.
     */
    fun observeAllPages(limit: Int, offset: Int): Flow<List<UserNewsResource>>

    /**
     * Returns available news resources for the user's followed topics as a stream.
     */
    fun observeAllForFollowedTopics(): Flow<List<UserNewsResource>>

    /**
     * Returns the user's bookmarked news resources as a stream.
     */
    fun observeAllBookmarked(): Flow<List<UserNewsResource>>

    /**
     * Returns the count of news.
     */
    fun getCount(): Flow<Int>

}
