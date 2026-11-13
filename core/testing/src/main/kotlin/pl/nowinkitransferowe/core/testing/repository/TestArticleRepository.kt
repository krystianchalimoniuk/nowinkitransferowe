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

package pl.nowinkitransferowe.core.testing.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind

class TestArticleRepository : ArticleRepository {

    private val articlesFlow: MutableSharedFlow<List<Article>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun observeArticle(id: Int): Flow<Article?> = articlesFlow.map { articles -> articles.find { it.id == id } }

    override fun observeArticles(kinds: Set<ArticleKind>?, topic: String?, limit: Int): Flow<List<Article>> = articlesFlow.map { articles ->
        articles
            .filter { kinds == null || it.kind in kinds }
            .filter { topic == null || topic in it.topics }
            .take(limit)
    }

    override fun observeArticles(ids: Set<Int>): Flow<List<Article>> = articlesFlow.map { articles -> articles.filter { it.id in ids } }

    override fun observeArticleCount(): Flow<Int> = articlesFlow.map { it.size }

    override fun observeArticlesAboutClub(clubId: Int): Flow<List<Article>> = articlesFlow.map { articles -> articles.filter { clubId in it.clubIds } }

    override fun observeArticlesAboutPlayer(playerId: Int): Flow<List<Article>> = articlesFlow.map { articles -> articles.filter { playerId in it.playerIds } }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /**
     * A test-only API to allow controlling the list of articles from tests.
     */
    fun sendArticles(articles: List<Article>) {
        articlesFlow.tryEmit(articles)
    }
}
