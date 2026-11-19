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

package pl.nowinkitransferowe.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import pl.nowinkitransferowe.core.model.UserArticle
import java.time.format.DateTimeFormatter

/**
 * "29 paź, 14:46": the day, short month and time of an article from this year, in the app's time
 * zone. An older one shows the year instead of the time, "7 wrz 2025", so it is clear it is old.
 */
@Composable
fun articleDateTime(instant: Instant, now: Instant = Clock.System.now()): String {
    val zone = LocalTimeZone.current
    val thisYear = instant.toLocalDateTime(zone).year == now.toLocalDateTime(zone).year
    return DateTimeFormatter
        .ofPattern(if (thisYear) "d MMM, HH:mm" else "d MMM yyyy", LocalConfiguration.current.locales[0])
        .withZone(zone.toJavaZoneId())
        .format(instant.toJavaInstant())
}

/**
 * "paź 2026": the month and year of a transfer. The day is often only nominal (the date of the
 * announcement or a guess), so the lists and details leave it out; the database keeps it.
 */
@Composable
fun transferMonth(date: LocalDate): String = DateTimeFormatter
    .ofPattern("LLL yyyy", LocalConfiguration.current.locales[0])
    .format(date.toJavaLocalDate())

/** The topics of an article and its date, as in the line above a title: "La Liga · 29 paź, 14:46". */
@Composable
fun articleOverline(article: UserArticle): String = (article.topics.map { it.name } + articleDateTime(article.publishedAt)).joinToString(" · ")
