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
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Full-text search; each query returns the ids of the matching records. [query] is FTS syntax, for
 * example `liverpool*`.
 */
@Dao
interface SearchDao {
    @Query("SELECT rowid FROM articles_fts WHERE articles_fts MATCH :query")
    fun searchArticleIds(query: String): Flow<List<Int>>

    @Query("SELECT rowid FROM transfers_fts WHERE transfers_fts MATCH :query")
    fun searchTransferIds(query: String): Flow<List<Int>>

    /** Synced clubs only; a stub has no page to open yet. */
    @Query(
        """
        SELECT clubs.id FROM clubs_fts JOIN clubs ON clubs.id = clubs_fts.rowid
        WHERE clubs_fts MATCH :query AND clubs.slug IS NOT NULL
        """,
    )
    fun searchClubIds(query: String): Flow<List<Int>>

    /** Synced players only. */
    @Query(
        """
        SELECT players.id FROM players_fts JOIN players ON players.id = players_fts.rowid
        WHERE players_fts MATCH :query AND players.slug IS NOT NULL
        """,
    )
    fun searchPlayerIds(query: String): Flow<List<Int>>
}
