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
 * Rumours of a season. Accuracy is shown only from 20 closed rumours ([showAccuracy]); before
 * that the numbers are chance.
 */
data class RumourStats(
    val season: Season,
    val active: Int,
    val confirmed: Int,
    val failed: Int,
    val showAccuracy: Boolean,
    val byStage: List<StageAccuracy>,
    val hotClubs: List<HotClub>,
    val daysToConfirm: Double?,
) {
    /** Share of closed rumours that were confirmed, 0.0 to 1.0, or null when it is not shown. */
    val accuracy: Double?
        get() = if (showAccuracy && confirmed + failed > 0) confirmed.toDouble() / (confirmed + failed) else null
}

/**
 * Rumours closed in the season that reached at most [stage], and how many of them were confirmed.
 */
data class StageAccuracy(
    val stage: RumourStage,
    val confirmed: Int,
    val closed: Int,
) {
    val accuracy: Double? get() = if (closed > 0) confirmed.toDouble() / closed else null
}

data class HotClub(
    val clubId: Int,
    val rumours: Int,
)
