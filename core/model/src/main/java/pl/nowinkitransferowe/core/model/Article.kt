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
 * An article. [topics] are topic slugs; [clubIds] and [playerIds] are the clubs and players it is
 * about. [contentHtml] is sanitized HTML for a WebView.
 */
data class Article(
    val id: Int,
    val url: String,
    val title: String,
    val kind: ArticleKind,
    val topics: List<String>,
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
) {
    /** Picked by the editors; featured articles of a league can be sent as notifications. */
    fun isFeatured(now: Instant): Boolean = featuredUntil != null && now < featuredUntil
}

data class Author(
    val name: String,
    val avatarUrl: String?,
)

enum class ArticleKind {
    OFFICIAL,
    RUMOUR,
    NEWS,
    VIDEO,
    UNKNOWN,
    ;

    companion object {
        fun fromApi(value: String?): ArticleKind = entries.find { it.name == value } ?: UNKNOWN
    }
}
