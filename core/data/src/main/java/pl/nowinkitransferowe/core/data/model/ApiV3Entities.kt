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

package pl.nowinkitransferowe.core.data.model

import pl.nowinkitransferowe.core.database.model.ArticleClubEntity
import pl.nowinkitransferowe.core.database.model.ArticleEntity
import pl.nowinkitransferowe.core.database.model.ArticlePlayerEntity
import pl.nowinkitransferowe.core.database.model.ArticleTagEntity
import pl.nowinkitransferowe.core.database.model.ArticleTopicEntity
import pl.nowinkitransferowe.core.database.model.ClubEntity
import pl.nowinkitransferowe.core.database.model.CountryEntity
import pl.nowinkitransferowe.core.database.model.PlayerEntity
import pl.nowinkitransferowe.core.database.model.RumourEntity
import pl.nowinkitransferowe.core.database.model.RumourStageEntity
import pl.nowinkitransferowe.core.database.model.TopicEntity
import pl.nowinkitransferowe.core.database.model.TransferEntity
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.network.model.NetworkArticle
import pl.nowinkitransferowe.core.network.model.NetworkClub
import pl.nowinkitransferowe.core.network.model.NetworkCountry
import pl.nowinkitransferowe.core.network.model.NetworkPlayer
import pl.nowinkitransferowe.core.network.model.NetworkRumour
import pl.nowinkitransferowe.core.network.model.NetworkTopic
import pl.nowinkitransferowe.core.network.model.NetworkTransfer

// Mapping of API v3 records to Room entities.
//
// Records refer to clubs, players, articles and transfers by id. Clubs and players that are not
// in the database yet are saved first as stubs: an id and the name copied into the record, or an
// empty name when the record has only the id (articles). Their full data arrives with the club
// and player change lists. Ids of articles and transfers that are not in the database are
// cleared by the callers through the existing-ids sets passed here.

fun NetworkArticle.asEntity() = ArticleEntity(
    id = id,
    url = url,
    title = title,
    kind = kind,
    lead = lead,
    contentHtml = contentHtml,
    imageUrl = image?.url,
    thumbnailUrl = image?.thumbnailUrl,
    imageSource = imageSource,
    newsSource = newsSource,
    authorName = author?.name,
    authorAvatarUrl = author?.avatarUrl,
    featuredUntil = featuredUntil,
    publishedAt = publishedAt,
)

fun NetworkArticle.topicEntities() = topics.distinct().mapIndexed { position, topic ->
    ArticleTopicEntity(articleId = id, topic = topic, position = position)
}

fun NetworkArticle.clubEntities() = clubIds.distinct().mapIndexed { position, clubId ->
    ArticleClubEntity(articleId = id, clubId = clubId, position = position)
}

fun NetworkArticle.playerEntities() = playerIds.distinct().mapIndexed { position, playerId ->
    ArticlePlayerEntity(articleId = id, playerId = playerId, position = position)
}

fun NetworkArticle.tagEntities() = tags.distinct().mapIndexed { position, tag ->
    ArticleTagEntity(articleId = id, tag = tag, position = position)
}

fun NetworkTransfer.asEntity(existingArticleIds: Set<Int>) = TransferEntity(
    id = id,
    playerId = playerId,
    playerName = playerName,
    fromClubId = fromClubId,
    fromClubName = fromClubName,
    fromStatus = fromStatus,
    toClubId = toClubId,
    toClubName = toClubName,
    toStatus = toStatus,
    feeKind = fee.kind,
    feeLabel = fee.label,
    feeMillions = fee.millionsOfEuro,
    date = date,
    season = Season.of(date).startYear,
    articleId = articleId?.takeIf { it in existingArticleIds },
)

/**
 * Returns null for a rumour without the destination club, which the database requires.
 */
fun NetworkRumour.asEntity(existingTransferIds: Set<Int>, existingArticleIds: Set<Int>): RumourEntity? {
    return RumourEntity(
        id = id,
        playerId = playerId,
        playerName = playerName,
        fromClubId = fromClubId,
        fromClubName = fromClubName,
        toClubId = toClubId ?: return null,
        toClubName = toClubName.orEmpty(),
        stage = stage,
        previousStage = previousStage,
        fee = fee,
        status = status,
        transferId = transferId?.takeIf { it in existingTransferIds },
        updatedAt = updatedAt,
        latestArticleId = latestArticleId?.takeIf { it in existingArticleIds },
    )
}

fun NetworkRumour.stageEntities(existingArticleIds: Set<Int>) = history.mapIndexed { position, step ->
    RumourStageEntity(
        rumourId = id,
        position = position,
        stage = step.stage,
        at = step.at,
        articleId = step.articleId?.takeIf { it in existingArticleIds },
    )
}

fun NetworkClub.asEntity() = ClubEntity(
    id = id,
    slug = slug,
    name = name,
    url = url,
    crestUrl = crestUrl,
    fullName = fullName,
    country = country,
    city = city,
    founded = founded,
    stadium = stadium,
    stadiumCapacity = stadiumCapacity,
    website = website,
    topic = topic,
)

fun NetworkPlayer.asEntity() = PlayerEntity(
    id = id,
    slug = slug,
    name = name,
    url = url,
    photoUrl = photoUrl,
    fullName = fullName,
    birthDate = birthDate,
    birthPlace = birthPlace,
    nationality = nationality,
    nationality2 = nationality2,
    positionGroup = positionGroup,
    position = position,
    heightCm = heightCm,
)

fun NetworkTopic.asEntity() = TopicEntity(slug = slug, name = name, kind = kind)

fun NetworkCountry.asEntity() = CountryEntity(code = code, name = name)

/**
 * A stub of a club known only by [id] and the [name] copied into another record.
 */
internal fun clubStub(id: Int, name: String?) = ClubEntity(id = id, name = name.orEmpty())

/**
 * A stub of a player known only by [id] and the [name] copied into another record.
 */
internal fun playerStub(id: Int, name: String?) = PlayerEntity(id = id, name = name.orEmpty())
