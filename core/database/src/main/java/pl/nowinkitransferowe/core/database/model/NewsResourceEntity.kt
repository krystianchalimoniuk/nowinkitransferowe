/*
 * Copyright 2024 The Android Open Source Project
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
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.model.NewsResource

/**
 * Defines an NT news resource.
 */
@Entity(
    tableName = "news_resources",
)
data class NewsResourceEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: NewsCategory,
    val isImportant: Boolean,
    val time: String,
    val author: String,
    @ColumnInfo(name = "photo_src")
    val photoSrc: String,
    val src: String,
    @ColumnInfo(name = "publish_date")
    val publishDate: Instant,
    @ColumnInfo(name = "auth_twitter")
    val authTwitter: String,
    @ColumnInfo(name = "auth_picture")
    val authPic: String,
    val link: String,
    val topics: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
)

fun NewsResourceEntity.asExternalModel() = NewsResource(
    id = id,
    title = title,
    description = description,
    category = category,
    isImportant = isImportant,
    author = author,
    photoSrc = photoSrc,
    src = src,
    publishDate = publishDate,
    authPic = authPic,
    authTwitter = authTwitter,
    link = link,
    topics = topics.split(", "),
    imageUrl = imageUrl,
)
