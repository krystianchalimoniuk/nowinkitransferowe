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

/**
 * One side of a transfer or rumour: a club, or a player without one.
 */
sealed interface ClubRef {
    val name: String

    /** A club; its current name and crest come from the clubs table when it is there. */
    data class Club(
        val id: Int,
        override val name: String,
        val crestUrl: String? = null,
    ) : ClubRef

    /** No club on this side, for example a free agent; [name] is the website's text for it. */
    data class NoClub(
        val status: NoClubStatus,
        override val name: String,
    ) : ClubRef

    companion object {
        /**
         * Builds a side from the API fields: a club when there is an id, otherwise its status.
         */
        fun fromApi(id: Int?, name: String?, status: String?): ClubRef = if (id != null) {
            Club(id = id, name = name.orEmpty())
        } else {
            NoClub(status = NoClubStatus.fromApi(status), name = name.orEmpty())
        }
    }
}

enum class NoClubStatus {
    FREE_AGENT,
    RETIRED,
    UNKNOWN,
    ;

    companion object {
        fun fromApi(value: String?): NoClubStatus = entries.find { it.name == value } ?: UNKNOWN
    }
}

/** The crest of the club on this side, or null when there is no club or no crest. */
val ClubRef.crestUrl: String? get() = (this as? ClubRef.Club)?.crestUrl
