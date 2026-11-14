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
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.UserArticle

/**
 * Articles joined with the user's data and with the names of their topics.
 */
interface UserArticleRepository {
    /** The newest [limit] articles, optionally only of [kinds] and filed under [topic]. */
    fun observeArticles(
        kinds: Set<ArticleKind>? = null,
        topic: String? = null,
        limit: Int = Int.MAX_VALUE,
    ): Flow<List<UserArticle>>

    fun observeArticle(id: Int): Flow<UserArticle?>

    /** Articles about the player with [playerId], newest first. */
    fun observeArticlesAboutPlayer(playerId: Int): Flow<List<UserArticle>>

    fun observeBookmarked(): Flow<List<UserArticle>>

    fun observeArticleCount(): Flow<Int>

    /** Whether any article added since the last visit is not opened yet. */
    fun observeHasUnread(): Flow<Boolean>

    /** Marks every article as read. */
    suspend fun markAllRead()
}
