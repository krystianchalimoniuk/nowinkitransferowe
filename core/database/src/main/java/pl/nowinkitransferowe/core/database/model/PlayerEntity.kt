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
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.PositionGroup

/**
 * A player. Like [ClubEntity], a stub has only [id] and [name] until the player is synced.
 */
@Entity(
    tableName = "players",
    indices = [
        Index(value = ["slug"], unique = true),
    ],
)
data class PlayerEntity(
    @PrimaryKey
    val id: Int,
    val slug: String? = null,
    val name: String,
    val url: String? = null,
    @ColumnInfo(name = "photo_url")
    val photoUrl: String? = null,
    @ColumnInfo(name = "full_name")
    val fullName: String? = null,
    @ColumnInfo(name = "birth_date")
    val birthDate: LocalDate? = null,
    @ColumnInfo(name = "birth_place")
    val birthPlace: String? = null,
    val nationality: String? = null,
    val nationality2: String? = null,
    @ColumnInfo(name = "position_group")
    val positionGroup: String? = null,
    val position: String? = null,
    @ColumnInfo(name = "height_cm")
    val heightCm: Int? = null,
)

fun PlayerEntity.asExternalModel() = Player(
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
    positionGroup = PositionGroup.fromApi(positionGroup),
    position = position,
    heightCm = heightCm,
)
