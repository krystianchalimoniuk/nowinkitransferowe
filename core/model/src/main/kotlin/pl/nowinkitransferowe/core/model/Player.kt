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

import kotlinx.datetime.LocalDate

/**
 * A player with facts from Wikidata. A stub (only id and name) has no [slug].
 */
data class Player(
    val id: Int,
    val slug: String?,
    val name: String,
    val url: String?,
    val photoUrl: String?,
    val fullName: String?,
    val birthDate: LocalDate?,
    val birthPlace: String?,
    val nationality: String?,
    val nationality2: String?,
    val positionGroup: PositionGroup?,
    val position: String?,
    val heightCm: Int?,
) {
    val isStub: Boolean get() = slug == null

    /** Age in full years on [date], or null when the birth date is not known. */
    fun ageOn(date: LocalDate): Int? {
        val birth = birthDate ?: return null
        val hadBirthday = date.monthNumber > birth.monthNumber ||
            (date.monthNumber == birth.monthNumber && date.dayOfMonth >= birth.dayOfMonth)
        return date.year - birth.year - if (hadBirthday) 0 else 1
    }
}

enum class PositionGroup {
    GK,
    DF,
    MF,
    FW,
    ;

    companion object {
        fun fromApi(value: String?): PositionGroup? = entries.find { it.name == value }
    }
}
