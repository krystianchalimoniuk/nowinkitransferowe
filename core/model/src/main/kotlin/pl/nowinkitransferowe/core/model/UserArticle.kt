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

package pl.nowinkitransferowe.core.model

import kotlinx.datetime.Instant

/**
 * An [Article] with the user's data: whether it is saved and whether it was opened. [topics] are the
 * article's topics found in the dictionary, in the article's order.
 */
data class UserArticle internal constructor(
    val id: Int,
    val url: String,
    val title: String,
    val kind: ArticleKind,
    val topics: List<Topic>,
    val lead: String,
    val contentHtml: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val imageSource: String?,
    val newsSource: String?,
    val author: Author?,
    val tags: List<String>,
    val clubIds: List<Int>,
    val playerIds: List<Int>,
    val featuredUntil: Instant?,
    val publishedAt: Instant,
    val isSaved: Boolean,
    val hasBeenViewed: Boolean,
) {
    /** Picked by the editors until [featuredUntil]. */
    fun isFeatured(now: Instant): Boolean = featuredUntil != null && now < featuredUntil

    constructor(article: Article, topics: Map<String, Topic>, userData: UserData) : this(
        id = article.id,
        url = article.url,
        title = article.title,
        kind = article.kind,
        topics = article.topics.mapNotNull(topics::get),
        lead = article.lead,
        contentHtml = article.contentHtml,
        imageUrl = article.imageUrl,
        thumbnailUrl = article.thumbnailUrl,
        imageSource = article.imageSource,
        newsSource = article.newsSource,
        author = article.author,
        tags = article.tags,
        clubIds = article.clubIds,
        playerIds = article.playerIds,
        featuredUntil = article.featuredUntil,
        publishedAt = article.publishedAt,
        isSaved = article.id in userData.bookmarkedArticles,
        hasBeenViewed = userData.isArticleRead(article.id),
    )
}

fun List<Article>.mapToUserArticles(topics: List<Topic>, userData: UserData): List<UserArticle> {
    val topicsBySlug = topics.associateBy(Topic::slug)
    return map { UserArticle(it, topicsBySlug, userData) }
}
