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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.model.mapToUserArticles
import javax.inject.Inject

/**
 * Implements a [UserArticleRepository] by combining an [ArticleRepository] with the topics of the
 * [DictionaryRepository] and the [UserDataRepository].
 */
class CompositeUserArticleRepository @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val userDataRepository: UserDataRepository,
) : UserArticleRepository {

    override fun observeArticles(kinds: Set<ArticleKind>?, topic: String?, limit: Int): Flow<List<UserArticle>> = articleRepository.observeArticles(kinds, topic, limit).withUserData()

    override fun observeArticle(id: Int): Flow<UserArticle?> = articleRepository.observeArticle(id)
        .map(::listOfNotNull)
        .withUserData()
        .map { it.firstOrNull() }

    override fun observeArticlesAboutPlayer(playerId: Int): Flow<List<UserArticle>> = articleRepository.observeArticlesAboutPlayer(playerId).withUserData()

    override fun observeBookmarked(): Flow<List<UserArticle>> = userDataRepository.userData
        .map { userData -> userData.bookmarkedArticles }
        .distinctUntilChanged()
        .flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList()) else articleRepository.observeArticles(ids).withUserData()
        }

    override fun observeArticleCount(): Flow<Int> = articleRepository.observeArticleCount()

    private fun Flow<List<Article>>.withUserData(): Flow<List<UserArticle>> = combine(
        this,
        dictionaryRepository.observeTopics(),
        userDataRepository.userData,
    ) { articles, topics, userData ->
        articles.mapToUserArticles(topics, userData)
    }
}
