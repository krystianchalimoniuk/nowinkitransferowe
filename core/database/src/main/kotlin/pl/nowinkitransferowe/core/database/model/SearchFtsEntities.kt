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
import androidx.room.Entity
import androidx.room.Fts4

// Full-text indexes over the columns of the main tables ("external content"): Room keeps them in sync
// with triggers, and the rowid of a match is the id of the record.

@Entity(tableName = "articles_fts")
@Fts4(contentEntity = ArticleEntity::class)
data class ArticleFtsEntity(
    val title: String,
    val lead: String,
)

@Entity(tableName = "transfers_fts")
@Fts4(contentEntity = TransferEntity::class)
data class TransferFtsEntity(
    @ColumnInfo(name = "player_name")
    val playerName: String,
    @ColumnInfo(name = "from_club_name")
    val fromClubName: String?,
    @ColumnInfo(name = "to_club_name")
    val toClubName: String?,
)

@Entity(tableName = "clubs_fts")
@Fts4(contentEntity = ClubEntity::class)
data class ClubFtsEntity(
    val name: String,
    @ColumnInfo(name = "full_name")
    val fullName: String?,
    val city: String?,
)

@Entity(tableName = "players_fts")
@Fts4(contentEntity = PlayerEntity::class)
data class PlayerFtsEntity(
    val name: String,
    @ColumnInfo(name = "full_name")
    val fullName: String?,
)
