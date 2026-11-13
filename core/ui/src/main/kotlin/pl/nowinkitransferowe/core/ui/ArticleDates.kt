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
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaZoneId
import pl.nowinkitransferowe.core.model.UserArticle
import java.time.format.DateTimeFormatter

/** "29 paź, 14:46": the day, short month and time of an article, in the app's time zone. */
@Composable
fun articleDateTime(instant: Instant): String = DateTimeFormatter
    .ofPattern("d MMM, HH:mm", LocalConfiguration.current.locales[0])
    .withZone(LocalTimeZone.current.toJavaZoneId())
    .format(instant.toJavaInstant())

/** The topics of an article and its date, as in the line above a title: "La Liga · 29 paź, 14:46". */
@Composable
fun articleOverline(article: UserArticle): String = (article.topics.map { it.name } + articleDateTime(article.publishedAt)).joinToString(" · ")
