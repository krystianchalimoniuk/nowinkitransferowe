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

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.TopicKind

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey
    val slug: String,
    val name: String,
    val kind: String,
)

fun TopicEntity.asExternalModel() = Topic(slug = slug, name = name, kind = TopicKind.fromApi(kind))

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey
    val code: String,
    val name: String,
)

fun CountryEntity.asExternalModel() = Country(code = code, name = name)
