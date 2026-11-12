/*
 * Copyright 2026 The Android Open Source Project
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
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.database.model.CountryEntity
import pl.nowinkitransferowe.core.database.model.TopicEntity

/**
 * Topics and countries, which the API returns whole.
 */
@Dao
interface DictionaryDao {
    @Transaction
    suspend fun replaceTopics(topics: List<TopicEntity>) {
        deleteTopics()
        insertTopics(topics)
    }

    @Transaction
    suspend fun replaceCountries(countries: List<CountryEntity>) {
        deleteCountries()
        insertCountries(countries)
    }

    @Insert
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Query("DELETE FROM topics")
    suspend fun deleteTopics()

    @Insert
    suspend fun insertCountries(countries: List<CountryEntity>)

    @Query("DELETE FROM countries")
    suspend fun deleteCountries()

    /**
     * True when either dictionary is empty or a topic or country code in the records is missing from it.
     * The API has no change list for the dictionaries, so this decides when to download them again.
     */
    @Query(
        """
        SELECT NOT EXISTS (SELECT 1 FROM topics)
            OR NOT EXISTS (SELECT 1 FROM countries)
            OR EXISTS (SELECT 1 FROM article_topics WHERE topic NOT IN (SELECT slug FROM topics))
            OR EXISTS (SELECT 1 FROM clubs WHERE topic NOT IN (SELECT slug FROM topics))
            OR EXISTS (SELECT 1 FROM clubs WHERE country NOT IN (SELECT code FROM countries))
            OR EXISTS (
                SELECT 1 FROM players
                WHERE nationality NOT IN (SELECT code FROM countries)
                    OR nationality2 NOT IN (SELECT code FROM countries)
            )
        """,
    )
    suspend fun hasMissingEntries(): Boolean

    @Query("SELECT * FROM topics")
    fun observeTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM countries ORDER BY name")
    fun observeCountries(): Flow<List<CountryEntity>>
}
