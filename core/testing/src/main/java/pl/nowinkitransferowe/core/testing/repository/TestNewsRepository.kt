/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.nowinkitransferowe.core.testing.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.repository.NewsRepository
import pl.nowinkitransferowe.core.data.repository.NewsResourceQuery
import pl.nowinkitransferowe.core.model.NewsResource

class TestNewsRepository : NewsRepository {

    /**
     * The backing hot flow for the list of topics ids for testing.
     */
    private val newsResourcesFlow: MutableSharedFlow<List<NewsResource>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun getNewsResources(query: NewsResourceQuery): Flow<List<NewsResource>> =
        newsResourcesFlow.map { newsResources ->
            var result = newsResources
            query.filterNewsIds?.let { filterNewsIds ->
                result = newsResources.filter { it.id in filterNewsIds }
            }
            result
        }

    override fun getNewsResourcesPages(limit: Int, offset: Int): Flow<List<NewsResource>> =
        newsResourcesFlow.map { newsResources ->
            newsResources.take(limit)
        }

    override fun getNewsResource(id: String): Flow<NewsResource> =
        newsResourcesFlow.map { newsResources ->
            newsResources.first { it.id == id }
        }

    override fun getNewsCount(): Flow<Int> =
        newsResourcesFlow.map { newsResources ->
            newsResources.count()
        }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /**
     * A test-only API to allow controlling the list of news resources from tests.
     */
    fun sendNewsResources(newsResources: List<NewsResource>) {
        newsResourcesFlow.tryEmit(newsResources)
    }
}
