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

package pl.nowinkitransferowe.core.model

/**
 * Type for news
 */
enum class NewsCategory(
    val serializedName: String,
    private val emojiString: String = "",
) {
    CHAMPIONS_LEAGUE("Champions League", "⚽"),
    FRANCE("Francja", "\uD83C\uDDEB\uD83C\uDDF7"),
    ENGLAND("Anglia", "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F"),
    EUROPA_LEAGUE("Europa League", "\uD83C\uDFC6"),
    GERMANY("Niemcy", "\uD83C\uDDE9\uD83C\uDDEA"),
    ITALY("Włochy", "\uD83C\uDDEE\uD83C\uDDF9"),
    NATIONAL_TEAMS("Reprezentacje", "\uD83C\uDFDF"),
    OTHER("Inne", "\uD83C\uDFC1"),
    POLAND("Polska", "\uD83C\uDDF5\uD83C\uDDF1"),
    SPAIN("Hiszpania", "\uD83C\uDDEA\uD83C\uDDF8"),
    TRANSFERS("Transfery", "\uD83D\uDD01"),
    UNKNOWN("Unknown", ""),
    ;

    fun combineNameWithEmoji(): String {
        return if (emojiString.isNotEmpty()) {
            this.serializedName + " " + this.emojiString
        } else {
            this.serializedName
        }
    }
}

fun String?.asNewsCategoryType() = when (this) {
    null -> NewsCategory.UNKNOWN
    else -> NewsCategory.values()
        .firstOrNull { type -> type.serializedName == this }
        ?: NewsCategory.UNKNOWN
}
