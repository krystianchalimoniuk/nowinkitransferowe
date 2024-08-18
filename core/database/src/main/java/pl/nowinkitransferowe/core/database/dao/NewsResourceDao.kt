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

package pl.nowinkitransferowe.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.model.NewsResource

/**
 * DAO for [NewsResource] and [NewsResourceEntity] access
 */
@Dao
interface NewsResourceDao {

    @Query(
        value = """
        SELECT * FROM news_resources
        WHERE id = :newsId
    """,
    )
    fun getNewsResource(newsId: String): Flow<NewsResourceEntity>

    /**
     * Fetches news resources that match the query parameters
     */
    @Transaction
    @Query(
        value = """
            SELECT * FROM news_resources
            WHERE 
                CASE WHEN :useFilterNewsIds
                    THEN id IN (:filterNewsIds)
                    ELSE 1
                END
            ORDER BY publish_date DESC
    """,
    )
    fun getNewsResources(
        useFilterNewsIds: Boolean = false,
        filterNewsIds: Set<String> = emptySet(),
    ): Flow<List<NewsResourceEntity>>

    /**
     * Fetches news resources that match the query parameters
     */
    @Transaction
    @Query(
        value = """
            SELECT * FROM news_resources ORDER BY publish_date DESC LIMIT :limit OFFSET :offset 
    """,
    )
    fun getNewsResourcesPages(limit: Int, offset: Int): Flow<List<NewsResourceEntity>>

    /**
     * Fetches ids of news resources that match the query parameters
     */
    @Transaction
    @Query(
        value = """
            SELECT id FROM news_resources
            WHERE 
                CASE WHEN :useFilterNewsIds
                    THEN id IN (:filterNewsIds)
                    ELSE 1
                END
            ORDER BY publish_date DESC
    """,
    )
    fun getNewsResourceIds(
        useFilterNewsIds: Boolean = false,
        filterNewsIds: Set<String> = emptySet(),
    ): Flow<List<String>>

    /**
     * Inserts or updates [newsResourceEntities] in the db under the specified primary keys
     */
    @Upsert
    suspend fun upsertNewsResources(newsResourceEntities: List<NewsResourceEntity>)

    /**
     * Deletes rows in the db matching the specified [ids]
     */
    @Query(
        value = """
            DELETE FROM news_resources
            WHERE id in (:ids)
        """,
    )
    suspend fun deleteNewsResources(ids: List<String>)

    @Query("SELECT count(*) FROM news_resources")
    fun getCount(): Flow<Int>
}
