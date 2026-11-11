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

package pl.nowinkitransferowe.core.network.model

import kotlinx.serialization.Serializable

/**
 * Season summary of rumours from `GET /api/v3/rumours/stats`.
 *
 * Accuracy is shown only when [showAccuracy] is true, from 20 closed rumours in the season.
 */
@Serializable
data class NetworkRumourStats(
    val season: String,
    val active: Int,
    val confirmed: Int,
    val failed: Int,
    val showAccuracy: Boolean,
    val byStage: List<NetworkStageAccuracy> = emptyList(),
    val hotClubs: List<NetworkHotClub> = emptyList(),
    val daysToConfirm: Double? = null,
)

/**
 * Rumours closed in the season grouped by the highest [stage] they reached.
 */
@Serializable
data class NetworkStageAccuracy(
    val stage: Int,
    val confirmed: Int,
    val closed: Int,
)

@Serializable
data class NetworkHotClub(
    val clubId: Int,
    val rumours: Int,
)
