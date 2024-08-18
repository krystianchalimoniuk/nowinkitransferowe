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

package pl.nowinkitransferowe.core.data.model

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun NetworkNewsResource.asEntity() = NewsResourceEntity(
    id = id.toString(),
    title = title,
    description = description,
    category = category,
    isImportant = isImportant == "Tak",
    time = time,
    author = author,
    photoSrc = photoSrc,
    src = newsSrc ?: "",
    publishDate = dateAndTimeAsInstant(date, time),
    authTwitter = authorTwitter,
    authPic = authorPicture,
    link = link,
    topics = topics,
    imageUrl = picture,
)

fun dateAndTimeAsInstant(date: String, time: String): Instant {
    val dateAndTime = "$date $time"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val localDateTime = java.time.LocalDateTime.parse(dateAndTime, formatter)
    val zone = ZoneId.of("Europe/Warsaw")
    return localDateTime.atZone(zone).toInstant().toKotlinInstant()
}
