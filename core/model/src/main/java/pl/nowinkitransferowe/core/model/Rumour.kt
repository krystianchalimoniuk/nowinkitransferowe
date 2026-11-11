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
import kotlin.time.Duration.Companion.days

/**
 * A transfer rumour: one player and one direction, moved through the stages by articles.
 */
data class Rumour(
    val id: Int,
    val player: PlayerRef,
    val from: ClubRef?,
    val to: ClubRef,
    val stage: RumourStage,
    val previousStage: RumourStage?,
    val fee: String?,
    val status: RumourStatus,
    val transferId: Int?,
    val updatedAt: Instant,
    val latestArticleId: Int?,
    val history: List<RumourStep>,
) {
    val trend: RumourTrend
        get() = when {
            previousStage == null -> RumourTrend.NEW
            stage > previousStage -> RumourTrend.UP
            stage < previousStage -> RumourTrend.DOWN
            else -> RumourTrend.SAME
        }

    /** Shown in the rumours sections: active and updated in the last 45 days, like on the website. */
    fun isFresh(now: Instant): Boolean = status == RumourStatus.ACTIVE && now - updatedAt <= FRESH_FOR

    private companion object {
        val FRESH_FOR = 45.days
    }
}

/**
 * Stages from speculation to agreement; [level] is the number the API uses.
 */
enum class RumourStage(val level: Int) {
    SPECULATION(1),
    INTEREST(2),
    TALKS(3),
    CLOSE(4),
    AGREEMENT(5),
    ;

    companion object {
        /** Levels outside 1..5 are clamped, so a new stage on the server still shows. */
        fun fromLevel(level: Int): RumourStage = entries.find { it.level == level }
            ?: if (level > AGREEMENT.level) AGREEMENT else SPECULATION
    }
}

enum class RumourStatus {
    ACTIVE,
    CONFIRMED,
    FAILED,
    EXPIRED,
    UNKNOWN,
    ;

    val isClosed: Boolean get() = this == CONFIRMED || this == FAILED

    companion object {
        fun fromApi(value: String?): RumourStatus = entries.find { it.name == value } ?: UNKNOWN
    }
}

enum class RumourTrend { UP, DOWN, SAME, NEW }

/**
 * A stage the rumour reached at [at]; [articleId] is null when it was changed without an article.
 */
data class RumourStep(
    val stage: RumourStage,
    val at: Instant,
    val articleId: Int?,
)
