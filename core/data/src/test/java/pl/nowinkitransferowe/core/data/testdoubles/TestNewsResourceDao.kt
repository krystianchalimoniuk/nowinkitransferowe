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

package pl.nowinkitransferowe.core.data.testdoubles

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import pl.nowinkitransferowe.core.database.dao.NewsResourceDao
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity

/**
 * Test double for [NewsResourceDao]
 */
class TestNewsResourceDao : NewsResourceDao {

    private val entitiesStateFlow = MutableStateFlow(emptyList<NewsResourceEntity>())

    override fun getNewsResource(newsId: String): Flow<NewsResourceEntity> =
        entitiesStateFlow.map { newsResourceList ->
            newsResourceList.first { it.id == newsId }
        }

    override fun getNewsResources(
        useFilterNewsIds: Boolean,
        filterNewsIds: Set<String>,
    ): Flow<List<NewsResourceEntity>> =
        entitiesStateFlow
            .map { resources ->
                var result = resources
                if (useFilterNewsIds) {
                    result = result.filter { resource ->
                        resource.id in filterNewsIds
                    }
                }
                result
            }

    override fun getNewsResourcesPages(limit: Int, offset: Int): Flow<List<NewsResourceEntity>> =
        entitiesStateFlow
            .map { resources ->
                resources.take(limit)
            }

    override fun getNewsResourceIds(
        useFilterNewsIds: Boolean,
        filterNewsIds: Set<String>,
    ): Flow<List<String>> =
        entitiesStateFlow
            .map { resources ->
                var result = resources
                if (useFilterNewsIds) {
                    result = result.filter { resource ->
                        resource.id in filterNewsIds
                    }
                }
                result.map { it.id }
            }

    override suspend fun upsertNewsResources(newsResourceEntities: List<NewsResourceEntity>) {
        entitiesStateFlow.update { oldValues ->
            // New values come first so they overwrite old values
            (newsResourceEntities + oldValues)
                .distinctBy(NewsResourceEntity::id)
                .sortedWith(
                    compareBy(NewsResourceEntity::publishDate).reversed(),
                )
        }
    }

    override suspend fun deleteNewsResources(ids: List<String>) {
        val idSet = ids.toSet()
        entitiesStateFlow.update { entities ->
            entities.filterNot { it.id in idSet }
        }
    }

    override fun getCount(): Flow<Int> =
        entitiesStateFlow.map {
            it.size
        }
}
