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
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.database.model.ArticleClubEntity
import pl.nowinkitransferowe.core.database.model.ArticleEntity
import pl.nowinkitransferowe.core.database.model.ArticlePlayerEntity
import pl.nowinkitransferowe.core.database.model.ArticleTagEntity
import pl.nowinkitransferowe.core.database.model.ArticleTopicEntity
import pl.nowinkitransferowe.core.database.model.PopulatedArticle

@Dao
interface ArticleDao {
    /**
     * Saves [articles] and replaces their topics, clubs, players and tags. The clubs and players they
     * refer to must exist, at least as stubs.
     */
    @Transaction
    suspend fun upsertArticles(
        articles: List<ArticleEntity>,
        topics: List<ArticleTopicEntity>,
        clubs: List<ArticleClubEntity>,
        players: List<ArticlePlayerEntity>,
        tags: List<ArticleTagEntity>,
    ) {
        upsertArticleEntities(articles)
        val ids = articles.map(ArticleEntity::id)
        deleteTopics(ids)
        deleteClubs(ids)
        deletePlayers(ids)
        deleteTags(ids)
        insertTopics(topics)
        insertClubs(clubs)
        insertPlayers(players)
        insertTags(tags)
    }

    @Upsert
    suspend fun upsertArticleEntities(articles: List<ArticleEntity>)

    @Insert
    suspend fun insertTopics(topics: List<ArticleTopicEntity>)

    @Insert
    suspend fun insertClubs(clubs: List<ArticleClubEntity>)

    @Insert
    suspend fun insertPlayers(players: List<ArticlePlayerEntity>)

    @Insert
    suspend fun insertTags(tags: List<ArticleTagEntity>)

    @Query("DELETE FROM article_topics WHERE article_id IN (:articleIds)")
    suspend fun deleteTopics(articleIds: List<Int>)

    @Query("DELETE FROM article_clubs WHERE article_id IN (:articleIds)")
    suspend fun deleteClubs(articleIds: List<Int>)

    @Query("DELETE FROM article_players WHERE article_id IN (:articleIds)")
    suspend fun deletePlayers(articleIds: List<Int>)

    @Query("DELETE FROM article_tags WHERE article_id IN (:articleIds)")
    suspend fun deleteTags(articleIds: List<Int>)

    @Query("DELETE FROM articles WHERE id IN (:ids)")
    suspend fun deleteArticles(ids: List<Int>)

    /** The ids out of [ids] that are in the database, to drop references to missing articles. */
    @Query("SELECT id FROM articles WHERE id IN (:ids)")
    suspend fun getExistingIds(ids: Set<Int>): List<Int>

    @Transaction
    @Query("SELECT * FROM articles WHERE id = :id")
    fun observeArticle(id: Int): Flow<PopulatedArticle?>

    /**
     * Newest articles, optionally only of [kinds] and filed under [topic].
     */
    @Transaction
    @Query(
        """
        SELECT * FROM articles
        WHERE (:filterKinds = 0 OR kind IN (:kinds))
          AND (:topic IS NULL OR id IN (SELECT article_id FROM article_topics WHERE topic = :topic))
        ORDER BY published_at DESC
        LIMIT :limit
        """,
    )
    fun observeArticles(
        filterKinds: Boolean = false,
        kinds: Set<String> = emptySet(),
        topic: String? = null,
        limit: Int = Int.MAX_VALUE,
    ): Flow<List<PopulatedArticle>>

    @Transaction
    @Query("SELECT COUNT(*) FROM articles")
    fun observeArticleCount(): Flow<Int>

    @Query("SELECT * FROM articles WHERE id IN (:ids) ORDER BY published_at DESC")
    fun observeArticlesByIds(ids: Set<Int>): Flow<List<PopulatedArticle>>

    @Transaction
    @Query(
        """
        SELECT * FROM articles
        WHERE id IN (SELECT article_id FROM article_clubs WHERE club_id = :clubId)
        ORDER BY published_at DESC
        """,
    )
    fun observeArticlesAboutClub(clubId: Int): Flow<List<PopulatedArticle>>

    @Transaction
    @Query(
        """
        SELECT * FROM articles
        WHERE id IN (SELECT article_id FROM article_players WHERE player_id = :playerId)
        ORDER BY published_at DESC
        """,
    )
    fun observeArticlesAboutPlayer(playerId: Int): Flow<List<PopulatedArticle>>
}
