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

package pl.nowinkitransferowe.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.Author
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.TopicKind
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.model.UserData
import pl.nowinkitransferowe.core.model.mapToUserArticles
import pl.nowinkitransferowe.core.ui.PreviewParameterData.articles

/**
 * This [PreviewParameterProvider](https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/PreviewParameterProvider)
 * provides list of [UserArticle] for Composable previews.
 */
class UserArticlePreviewParameterProvider : PreviewParameterProvider<List<UserArticle>> {

    override val values: Sequence<List<UserArticle>> = sequenceOf(articles)
}

object PreviewParameterData {
    internal val userData: UserData = UserData(
        bookmarkedArticles = setOf(1, 3),
        viewedArticles = setOf(1, 2, 4),
        bookmarkedTransfers = setOf(2, 3),
        viewedTransfers = setOf(2, 3),
        darkThemeConfig = DarkThemeConfig.DARK,
        useDynamicColor = false,
        isNewsNotificationsAllowed = true,
        isTransfersNotificationsAllowed = true,
        isGeneralNotificationAllowed = true,
    )

    private val topics = listOf(
        Topic(slug = "serie-a", name = "Serie A", kind = TopicKind.LEAGUE),
        Topic(slug = "la-liga", name = "La Liga", kind = TopicKind.LEAGUE),
        Topic(slug = "premier-league", name = "Premier League", kind = TopicKind.LEAGUE),
    )

    private fun article(id: Int, title: String, topic: String, publishedAt: String, kind: ArticleKind) = Article(
        id = id,
        url = "https://nowinkitransferowe.pl/$id/transfery/artykul",
        title = title,
        kind = kind,
        topics = listOf(topic),
        lead = "<p><strong>$title</strong> – szczegóły transferu i komentarze klubów.</p>",
        contentHtml = "<p>$title</p>",
        imageUrl = null,
        thumbnailUrl = null,
        imageSource = "Wikimedia",
        newsSource = "Transfermarkt",
        author = Author(name = "Redakcja", avatarUrl = null),
        tags = listOf("Transfery", "Okno transferowe"),
        clubIds = emptyList(),
        playerIds = emptyList(),
        featuredUntil = null,
        publishedAt = Instant.parse(publishedAt),
    )

    val articles: List<UserArticle> = listOf(
        article(1, "Oficjalnie: Raphael Varane w Como 1907", "serie-a", "2024-07-29T11:36:00Z", ArticleKind.OFFICIAL),
        article(2, "Barcelona blisko porozumienia w sprawie napastnika", "la-liga", "2024-07-28T09:00:00Z", ArticleKind.RUMOUR),
        article(3, "Chelsea ogłosiła kolejny transfer", "premier-league", "2024-07-27T18:20:00Z", ArticleKind.NEWS),
    ).mapToUserArticles(topics, userData)
}
