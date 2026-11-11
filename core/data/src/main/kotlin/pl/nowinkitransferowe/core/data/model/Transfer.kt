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
import pl.nowinkitransferowe.core.database.model.TransferResourceEntity
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun NetworkTransferResource.asEntity() = TransferResourceEntity(
    id = id.toString(),
    name = name,
    footballerImg = footballerImg,
    clubTo = clubTo,
    clubToImg = clubToImg,
    clubFrom = clubFrom,
    clubFromImg = clubFromImg,
    price = price,
    url = link,
    season = timestampToSeason(publishDate),
    publishDate = dateAndTimeAsInstant(publishDate),
)

fun dateAndTimeAsInstant(timestamp: String): Instant {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val localDateTime = java.time.LocalDateTime.parse(timestamp, formatter)
    val zone = ZoneId.of("Europe/Warsaw")
    return localDateTime.atZone(zone).toInstant().toKotlinInstant()
}
fun timestampToSeason(timestamp: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val data = java.time.LocalDateTime.parse(timestamp, formatter)

    val year = data.year
    val month = data.monthValue

    return if (month >= 7) {
        "${year % 100}/${(year + 1) % 100}"
    } else {
        "${(year - 1) % 100}/${year % 100}"
    }
}
