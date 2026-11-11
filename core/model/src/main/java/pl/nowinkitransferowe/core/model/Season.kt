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

package pl.nowinkitransferowe.core.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * A transfer season. It starts on 1 April and ends on 31 March, like on the website, and is shown
 * as "26/27".
 */
data class Season(val startYear: Int) : Comparable<Season> {

    /** "26/27" */
    val label: String get() = "${twoDigits(startYear)}/${twoDigits(startYear + 1)}"

    /** "26-27", as the API takes it in `?season=` */
    val slug: String get() = "${twoDigits(startYear)}-${twoDigits(startYear + 1)}"

    val start: LocalDate get() = LocalDate(startYear, SEASON_START_MONTH, 1)

    operator fun contains(date: LocalDate): Boolean = of(date) == this

    fun previous(): Season = Season(startYear - 1)

    override fun compareTo(other: Season): Int = startYear.compareTo(other.startYear)

    companion object {
        private const val SEASON_START_MONTH = 4

        /** The website counts seasons in Polish time. */
        val TIME_ZONE: TimeZone = TimeZone.of("Europe/Warsaw")

        fun of(date: LocalDate): Season = Season(if (date.monthNumber >= SEASON_START_MONTH) date.year else date.year - 1)

        fun of(instant: Instant): Season = of(instant.toLocalDateTime(TIME_ZONE).date)

        /**
         * Parses "26/27" or "26-27"; two-digit years above 90 are read as 19xx, like on the website.
         * Returns null for anything else.
         */
        fun parse(text: String): Season? {
            val parts = text.trim().split('/', '-')
            if (parts.size != 2) return null
            val start = parts[0].toIntOrNull()?.takeIf { it in 0..99 } ?: return null
            val end = parts[1].toIntOrNull() ?: return null
            if ((start + 1) % 100 != end) return null
            return Season(if (start > 90) 1900 + start else 2000 + start)
        }

        private fun twoDigits(year: Int) = (year % 100).toString().padStart(2, '0')
    }
}
