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
import pl.nowinkitransferowe.core.model.Club

/**
 * A club. A stub, inserted before a transfer, rumour or article that refers to the club when the club
 * itself is not synced yet, has only [id] and [name] (empty when it comes from an article). The
 * full record replaces it with an upsert.
 */
@Entity(
    tableName = "clubs",
    indices = [
        Index(value = ["slug"], unique = true),
        Index(value = ["topic"]),
    ],
)
data class ClubEntity(
    @PrimaryKey
    val id: Int,
    val slug: String? = null,
    val name: String,
    val url: String? = null,
    @ColumnInfo(name = "crest_url")
    val crestUrl: String? = null,
    @ColumnInfo(name = "full_name")
    val fullName: String? = null,
    val country: String? = null,
    val city: String? = null,
    val founded: Int? = null,
    val stadium: String? = null,
    @ColumnInfo(name = "stadium_capacity")
    val stadiumCapacity: Int? = null,
    val website: String? = null,
    val topic: String? = null,
)

fun ClubEntity.asExternalModel() = Club(
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
