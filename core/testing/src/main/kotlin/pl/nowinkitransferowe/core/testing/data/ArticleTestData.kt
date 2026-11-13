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

package pl.nowinkitransferowe.core.testing.data

import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.Author
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.TopicKind
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.model.mapToUserArticles
import pl.nowinkitransferowe.core.testing.repository.emptyUserData

val topicsTestData: List<Topic> = listOf(
    Topic(slug = "premier-league", name = "Premier League", kind = TopicKind.LEAGUE),
    Topic(slug = "serie-a", name = "Serie A", kind = TopicKind.LEAGUE),
    Topic(slug = "ekstraklasa", name = "Ekstraklasa", kind = TopicKind.LEAGUE),
)

private fun testArticle(
    id: Int,
    title: String,
    topics: List<String>,
    publishedAt: String,
    kind: ArticleKind = ArticleKind.NEWS,
) = Article(
    id = id,
    url = "https://nowinkitransferowe.pl/$id/transfery/artykul-$id",
    title = title,
    kind = kind,
    topics = topics,
    lead = "Lead of $title",
    contentHtml = "<p>Content of $title</p>",
    imageUrl = "https://nowinkitransferowe.pl/media/$id.jpg",
    thumbnailUrl = "https://nowinkitransferowe.pl/media/w/640/$id.jpg.webp",
    imageSource = "Wikimedia",
    newsSource = "Transfermarkt",
    author = Author(name = "Redakcja", avatarUrl = null),
    tags = listOf("Tag $id"),
    clubIds = emptyList(),
    playerIds = emptyList(),
    featuredUntil = null,
    publishedAt = Instant.parse(publishedAt),
)

/** 12 articles, newest first like the repositories return them. */
val articlesTestData: List<Article> = listOf(
    testArticle(11284, "PSG zarzuciło sieć na Fermina Lopeza", listOf("premier-league"), "2026-11-11T12:00:00Z", ArticleKind.RUMOUR),
    testArticle(11283, "Oficjalnie: Varane w Como", listOf("serie-a"), "2026-11-10T12:00:00Z", ArticleKind.OFFICIAL),
    testArticle(11282, "Legia szuka napastnika", listOf("ekstraklasa"), "2026-11-09T12:00:00Z"),
    testArticle(11281, "Podsumowanie okna", emptyList(), "2026-11-08T12:00:00Z"),
) + (1..8).map { day ->
    testArticle(11281 - day, "Nowinki dnia $day", listOf("premier-league"), "2026-10-${(31 - day).toString().padStart(2, '0')}T12:00:00Z")
}

/** [articlesTestData] with their topics and nothing saved or viewed. */
val userArticlesTestData: List<UserArticle> = articlesTestData.mapToUserArticles(topicsTestData, emptyUserData)

/** [articlesTestData] all saved, as the bookmarks screen shows them. */
val savedUserArticlesTestData: List<UserArticle> = articlesTestData.mapToUserArticles(
    topicsTestData,
    emptyUserData.copy(bookmarkedArticles = articlesTestData.map { it.id }.toSet()),
)
