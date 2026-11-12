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

package pl.nowinkitransferowe.core.network.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Network representation of an article from `GET /api/v3/articles`.
 *
 * [kind] is one of `OFFICIAL`, `RUMOUR`, `NEWS` or `VIDEO`; [contentHtml] is sanitized HTML.
 */
@Serializable
data class NetworkArticle(
    val id: Int,
    val url: String,
    val title: String,
    val socialTitle: String? = null,
    val kind: String,
    val topics: List<String> = emptyList(),
    // Null for some older articles (79 of 9515 on 2026-11-12).
    val lead: String? = null,
    val contentHtml: String,
    val image: NetworkArticleImage? = null,
    val imageSource: String? = null,
    val newsSource: String? = null,
    val author: NetworkArticleAuthor? = null,
    val tags: List<String> = emptyList(),
    val clubIds: List<Int> = emptyList(),
    val playerIds: List<Int> = emptyList(),
    val featuredUntil: Instant? = null,
    val publishedAt: Instant,
)

@Serializable
data class NetworkArticleImage(
    val url: String,
    val thumbnailUrl: String,
)

@Serializable
data class NetworkArticleAuthor(
    val name: String,
    val avatarUrl: String? = null,
)
