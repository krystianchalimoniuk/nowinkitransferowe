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
import kotlinx.datetime.LocalDate
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Fee
import pl.nowinkitransferowe.core.model.FeeKind
import pl.nowinkitransferowe.core.model.NoClubStatus
import pl.nowinkitransferowe.core.model.PlayerRef
import pl.nowinkitransferowe.core.model.Transfer

/**
 * A transfer. The names are copies from the API, used for search and until the club or player is
 * synced; screens show the current name from [ClubEntity] and [PlayerEntity]. [season] is the start
 * year of the season of [date].
 */
@Entity(
    tableName = "transfers",
    foreignKeys = [
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"], childColumns = ["player_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["from_club_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["to_club_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = ArticleEntity::class, parentColumns = ["id"], childColumns = ["article_id"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [
        Index(value = ["player_id"]),
        Index(value = ["from_club_id"]),
        Index(value = ["to_club_id"]),
        Index(value = ["article_id"]),
        Index(value = ["date"]),
        Index(value = ["season"]),
    ],
)
data class TransferEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "player_id")
    val playerId: Int?,
    @ColumnInfo(name = "player_name")
    val playerName: String,
    @ColumnInfo(name = "from_club_id")
    val fromClubId: Int?,
    @ColumnInfo(name = "from_club_name")
    val fromClubName: String?,
    @ColumnInfo(name = "from_status")
    val fromStatus: String?,
    @ColumnInfo(name = "to_club_id")
    val toClubId: Int?,
    @ColumnInfo(name = "to_club_name")
    val toClubName: String?,
    @ColumnInfo(name = "to_status")
    val toStatus: String?,
    @ColumnInfo(name = "fee_kind")
    val feeKind: String,
    @ColumnInfo(name = "fee_label")
    val feeLabel: String,
    @ColumnInfo(name = "fee_millions")
    val feeMillions: Double?,
    val date: LocalDate,
    val season: Int,
    @ColumnInfo(name = "article_id")
    val articleId: Int?,
)

/**
 * A transfer with the current names, crests and photo of its clubs and player.
 */
data class PopulatedTransfer(
    @Embedded
    val transfer: TransferEntity,
    @ColumnInfo(name = "current_player_name")
    val currentPlayerName: String?,
    @ColumnInfo(name = "player_photo_url")
    val playerPhotoUrl: String?,
    @ColumnInfo(name = "player_nationality")
    val playerNationality: String? = null,
    @ColumnInfo(name = "current_from_club_name")
    val currentFromClubName: String?,
    @ColumnInfo(name = "from_club_crest_url")
    val fromClubCrestUrl: String?,
    @ColumnInfo(name = "current_to_club_name")
    val currentToClubName: String?,
    @ColumnInfo(name = "to_club_crest_url")
    val toClubCrestUrl: String?,
)

fun PopulatedTransfer.asExternalModel() = Transfer(
    id = transfer.id,
    player = PlayerRef(
        id = transfer.playerId ?: 0,
        name = currentPlayerName.orCopy(transfer.playerName),
        photoUrl = playerPhotoUrl,
        nationality = playerNationality,
    ),
    from = clubRef(transfer.fromClubId, currentFromClubName.orCopy(transfer.fromClubName), fromClubCrestUrl, transfer.fromStatus),
    to = clubRef(transfer.toClubId, currentToClubName.orCopy(transfer.toClubName), toClubCrestUrl, transfer.toStatus),
    fee = Fee(kind = FeeKind.fromApi(transfer.feeKind), label = transfer.feeLabel, millionsOfEuro = transfer.feeMillions),
    date = transfer.date,
    articleId = transfer.articleId,
)

/** The name from the clubs or players table, unless that is an empty stub. */
internal fun String?.orCopy(copy: String?): String = this?.takeIf(String::isNotBlank) ?: copy.orEmpty()

internal fun clubRef(id: Int?, name: String, crestUrl: String?, status: String?): ClubRef = if (id != null) {
    ClubRef.Club(id = id, name = name, crestUrl = crestUrl)
} else {
    ClubRef.NoClub(status = NoClubStatus.fromApi(status), name = name)
}
