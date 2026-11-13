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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import org.jetbrains.annotations.TestOnly
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.SearchResult
import pl.nowinkitransferowe.core.model.Transfer

class TestSearchContentsRepository : SearchContentsRepository {

    private val cachedArticles = MutableStateFlow(emptyList<Article>())
    private val cachedTransfers = MutableStateFlow(emptyList<Transfer>())

    override fun searchContents(searchQuery: String): Flow<SearchResult> = combine(cachedArticles, cachedTransfers) { articles, transfers ->
        SearchResult(
            articles = articles.filter { searchQuery in it.title || it.tags.any { tag -> searchQuery in tag } },
            transfers = transfers.filter { searchQuery in it.player.name },
        )
    }

    override fun getSearchContentsCount(): Flow<Int> = combine(cachedArticles, cachedTransfers) { articles, transfers -> articles.size + transfers.size }

    @TestOnly
    fun addArticles(articles: List<Article>) = cachedArticles.update { it + articles }

    @TestOnly
    fun addTransfers(transfers: List<Transfer>) = cachedTransfers.update { it + transfers }
}
