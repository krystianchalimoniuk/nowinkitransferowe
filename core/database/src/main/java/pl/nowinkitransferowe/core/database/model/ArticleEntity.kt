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

package pl.nowinkitransferowe.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.Author

/**
 * An article. Its topics, clubs, players and tags live in the tables below, in the order of the API.
 */
@Entity(
    tableName = "articles",
    indices = [
        Index(value = ["published_at"]),
        Index(value = ["kind"]),
    ],
)
data class ArticleEntity(
    @PrimaryKey
    val id: Int,
    val url: String,
    val title: String,
    val kind: String,
    val lead: String,
    @ColumnInfo(name = "content_html")
    val contentHtml: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,
    @ColumnInfo(name = "image_source")
    val imageSource: String?,
    @ColumnInfo(name = "news_source")
    val newsSource: String?,
    @ColumnInfo(name = "author_name")
    val authorName: String?,
    @ColumnInfo(name = "author_avatar_url")
    val authorAvatarUrl: String?,
    @ColumnInfo(name = "featured_until")
    val featuredUntil: Instant?,
    @ColumnInfo(name = "published_at")
    val publishedAt: Instant,
)

@Entity(
    tableName = "article_topics",
    primaryKeys = ["article_id", "topic"],
    foreignKeys = [
        ForeignKey(entity = ArticleEntity::class, parentColumns = ["id"], childColumns = ["article_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["topic"])],
)
data class ArticleTopicEntity(
    @ColumnInfo(name = "article_id")
    val articleId: Int,
    val topic: String,
    val position: Int,
)

@Entity(
    tableName = "article_clubs",
    primaryKeys = ["article_id", "club_id"],
    foreignKeys = [
        ForeignKey(entity = ArticleEntity::class, parentColumns = ["id"], childColumns = ["article_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["club_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["club_id"])],
)
data class ArticleClubEntity(
    @ColumnInfo(name = "article_id")
    val articleId: Int,
    @ColumnInfo(name = "club_id")
    val clubId: Int,
    val position: Int,
)

@Entity(
    tableName = "article_players",
    primaryKeys = ["article_id", "player_id"],
    foreignKeys = [
        ForeignKey(entity = ArticleEntity::class, parentColumns = ["id"], childColumns = ["article_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"], childColumns = ["player_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["player_id"])],
)
data class ArticlePlayerEntity(
    @ColumnInfo(name = "article_id")
    val articleId: Int,
    @ColumnInfo(name = "player_id")
    val playerId: Int,
    val position: Int,
)

@Entity(
    tableName = "article_tags",
    primaryKeys = ["article_id", "tag"],
    foreignKeys = [
        ForeignKey(entity = ArticleEntity::class, parentColumns = ["id"], childColumns = ["article_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["tag"])],
)
data class ArticleTagEntity(
    @ColumnInfo(name = "article_id")
    val articleId: Int,
    val tag: String,
    val position: Int,
)

/**
 * An article with its relations, as screens read it.
 */
data class PopulatedArticle(
    @Embedded
    val article: ArticleEntity,
    @Relation(parentColumn = "id", entityColumn = "article_id")
    val topics: List<ArticleTopicEntity>,
    @Relation(parentColumn = "id", entityColumn = "article_id")
    val clubs: List<ArticleClubEntity>,
    @Relation(parentColumn = "id", entityColumn = "article_id")
    val players: List<ArticlePlayerEntity>,
    @Relation(parentColumn = "id", entityColumn = "article_id")
    val tags: List<ArticleTagEntity>,
)

fun PopulatedArticle.asExternalModel() = Article(
    id = article.id,
    url = article.url,
    title = article.title,
    kind = ArticleKind.fromApi(article.kind),
    topics = topics.sortedBy(ArticleTopicEntity::position).map(ArticleTopicEntity::topic),
    lead = article.lead,
    contentHtml = article.contentHtml,
    imageUrl = article.imageUrl,
    thumbnailUrl = article.thumbnailUrl,
    imageSource = article.imageSource,
    newsSource = article.newsSource,
    author = article.authorName?.let { Author(name = it, avatarUrl = article.authorAvatarUrl) },
    tags = tags.sortedBy(ArticleTagEntity::position).map(ArticleTagEntity::tag),
    clubIds = clubs.sortedBy(ArticleClubEntity::position).map(ArticleClubEntity::clubId),
    playerIds = players.sortedBy(ArticlePlayerEntity::position).map(ArticlePlayerEntity::playerId),
    featuredUntil = article.featuredUntil,
    publishedAt = article.publishedAt,
)
