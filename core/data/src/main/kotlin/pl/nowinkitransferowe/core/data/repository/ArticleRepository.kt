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

package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.data.Syncable
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind

/**
 * API v3 articles, saved offline.
 */
interface ArticleRepository : Syncable {
    fun observeArticle(id: Int): Flow<Article?>

    /**
     * Newest articles first, optionally only of the given [kinds] and filed under [topic].
     */
    fun observeArticles(
        kinds: Set<ArticleKind>? = null,
        topic: String? = null,
        limit: Int = Int.MAX_VALUE,
    ): Flow<List<Article>>

    fun observeArticles(ids: Set<Int>): Flow<List<Article>>

    fun observeArticleCount(): Flow<Int>

    fun observeArticlesAboutClub(clubId: Int): Flow<List<Article>>

    fun observeArticlesAboutPlayer(playerId: Int): Flow<List<Article>>
}
