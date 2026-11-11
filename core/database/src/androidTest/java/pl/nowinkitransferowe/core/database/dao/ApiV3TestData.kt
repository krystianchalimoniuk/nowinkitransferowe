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

package pl.nowinkitransferowe.core.database.dao

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import pl.nowinkitransferowe.core.database.model.ArticleEntity
import pl.nowinkitransferowe.core.database.model.ClubEntity
import pl.nowinkitransferowe.core.database.model.PlayerEntity
import pl.nowinkitransferowe.core.database.model.RumourEntity
import pl.nowinkitransferowe.core.database.model.TransferEntity

// Small records for the API v3 DAO tests.

internal fun club(id: Int, name: String, slug: String? = name.lowercase().replace(' ', '-')) = ClubEntity(
    id = id,
    slug = slug,
    name = name,
    crestUrl = slug?.let { "https://nowinkitransferowe.pl/media/w/160/$it.png.webp" },
    country = "gb-eng",
    topic = "premier-league",
)

internal fun clubStub(id: Int, name: String) = ClubEntity(id = id, name = name)

internal fun player(id: Int, name: String, slug: String? = name.lowercase().replace(' ', '-')) = PlayerEntity(
    id = id,
    slug = slug,
    name = name,
)

internal fun playerStub(id: Int, name: String) = PlayerEntity(id = id, name = name)

internal fun article(id: Int, kind: String = "OFFICIAL", publishedAt: String = "2026-10-26T19:55:57Z") = ArticleEntity(
    id = id,
    url = "https://nowinkitransferowe.pl/$id/transfery/artykul",
    title = "Artykuł $id",
    kind = kind,
    lead = "Lead $id",
    contentHtml = "<p>Treść</p>",
    imageUrl = null,
    thumbnailUrl = null,
    imageSource = null,
    newsSource = null,
    authorName = "Redakcja",
    authorAvatarUrl = null,
    featuredUntil = null,
    publishedAt = Instant.parse(publishedAt),
)

internal fun transfer(
    id: Int,
    playerId: Int? = 10281,
    playerName: String = "Djylian N'Guessan",
    fromClubId: Int? = 44,
    fromClubName: String? = "AS Saint-Etienne",
    toClubId: Int? = 86,
    toClubName: String? = "Liverpool FC",
    date: LocalDate = LocalDate(2026, 10, 26),
    articleId: Int? = null,
) = TransferEntity(
    id = id,
    playerId = playerId,
    playerName = playerName,
    fromClubId = fromClubId,
    fromClubName = fromClubName,
    fromStatus = null,
    toClubId = toClubId,
    toClubName = toClubName,
    toStatus = null,
    feeKind = "AMOUNT",
    feeLabel = "5 mln €",
    feeMillions = 5.0,
    date = date,
    season = if (date.monthNumber >= 4) date.year else date.year - 1,
    articleId = articleId,
)

internal fun rumour(
    id: Int,
    playerId: Int = 12148,
    fromClubId: Int? = 37,
    toClubId: Int = 5120,
    status: String = "ACTIVE",
    updatedAt: String = "2026-10-20T10:00:00Z",
    transferId: Int? = null,
    latestArticleId: Int? = null,
) = RumourEntity(
    id = id,
    playerId = playerId,
    playerName = "Gabriel Martinelli",
    fromClubId = fromClubId,
    fromClubName = "Arsenal FC",
    toClubId = toClubId,
    toClubName = "Al-Hilal SFC",
    stage = 4,
    previousStage = 3,
    fee = "ok. 70 mln €",
    status = status,
    transferId = transferId,
    updatedAt = Instant.parse(updatedAt),
    latestArticleId = latestArticleId,
)
