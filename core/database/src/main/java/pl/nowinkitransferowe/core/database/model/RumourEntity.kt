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
import pl.nowinkitransferowe.core.model.PlayerRef
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.RumourStep

/**
 * A transfer rumour. Like on the website, it goes away with its player or destination club.
 */
@Entity(
    tableName = "rumours",
    foreignKeys = [
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"], childColumns = ["player_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["from_club_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["to_club_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TransferEntity::class, parentColumns = ["id"], childColumns = ["transfer_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = ArticleEntity::class, parentColumns = ["id"], childColumns = ["latest_article_id"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [
        Index(value = ["status", "updated_at"]),
        Index(value = ["player_id"]),
        Index(value = ["from_club_id"]),
        Index(value = ["to_club_id"]),
        Index(value = ["transfer_id"]),
        Index(value = ["latest_article_id"]),
    ],
)
data class RumourEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "player_id")
    val playerId: Int,
    @ColumnInfo(name = "player_name")
    val playerName: String,
    @ColumnInfo(name = "from_club_id")
    val fromClubId: Int?,
    @ColumnInfo(name = "from_club_name")
    val fromClubName: String?,
    @ColumnInfo(name = "to_club_id")
    val toClubId: Int,
    @ColumnInfo(name = "to_club_name")
    val toClubName: String,
    val stage: Int,
    @ColumnInfo(name = "previous_stage")
    val previousStage: Int?,
    val fee: String?,
    val status: String,
    @ColumnInfo(name = "transfer_id")
    val transferId: Int?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    @ColumnInfo(name = "latest_article_id")
    val latestArticleId: Int?,
)

/**
 * A stage a rumour reached; [position] keeps the order of the history.
 */
@Entity(
    tableName = "rumour_stages",
    primaryKeys = ["rumour_id", "position"],
    foreignKeys = [
        ForeignKey(entity = RumourEntity::class, parentColumns = ["id"], childColumns = ["rumour_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ArticleEntity::class, parentColumns = ["id"], childColumns = ["article_id"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index(value = ["article_id"])],
)
data class RumourStageEntity(
    @ColumnInfo(name = "rumour_id")
    val rumourId: Int,
    val position: Int,
    val stage: Int,
    val at: Instant,
    @ColumnInfo(name = "article_id")
    val articleId: Int?,
)

/**
 * A rumour with its history and the current names and crests of its player and clubs.
 */
data class PopulatedRumour(
    @Embedded
    val rumour: RumourEntity,
    @ColumnInfo(name = "current_player_name")
    val currentPlayerName: String?,
    @ColumnInfo(name = "player_photo_url")
    val playerPhotoUrl: String?,
    @ColumnInfo(name = "current_from_club_name")
    val currentFromClubName: String?,
    @ColumnInfo(name = "from_club_crest_url")
    val fromClubCrestUrl: String?,
    @ColumnInfo(name = "current_to_club_name")
    val currentToClubName: String?,
    @ColumnInfo(name = "to_club_crest_url")
    val toClubCrestUrl: String?,
    @Relation(parentColumn = "id", entityColumn = "rumour_id")
    val stages: List<RumourStageEntity>,
)

fun PopulatedRumour.asExternalModel() = Rumour(
    id = rumour.id,
    player = PlayerRef(
        id = rumour.playerId,
        name = currentPlayerName.orCopy(rumour.playerName),
        photoUrl = playerPhotoUrl,
    ),
    from = rumour.fromClubId?.let { clubRef(it, currentFromClubName.orCopy(rumour.fromClubName), fromClubCrestUrl, null) },
    to = clubRef(rumour.toClubId, currentToClubName.orCopy(rumour.toClubName), toClubCrestUrl, null),
    stage = RumourStage.fromLevel(rumour.stage),
    previousStage = rumour.previousStage?.let(RumourStage::fromLevel),
    fee = rumour.fee,
    status = RumourStatus.fromApi(rumour.status),
    transferId = rumour.transferId,
    updatedAt = rumour.updatedAt,
    latestArticleId = rumour.latestArticleId,
    history = stages.sortedBy(RumourStageEntity::position).map {
        RumourStep(stage = RumourStage.fromLevel(it.stage), at = it.at, articleId = it.articleId)
    },
)
