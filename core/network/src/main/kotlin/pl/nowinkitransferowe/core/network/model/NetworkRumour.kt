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

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Network representation of a transfer rumour from `GET /api/v3/rumours`.
 *
 * [stage] goes from 1 (speculation) to 5 (agreement); [status] is one of `ACTIVE`, `CONFIRMED`,
 * `FAILED` or `EXPIRED`, and a confirmed rumour points at its [transferId].
 */
@Serializable
data class NetworkRumour(
    val id: Int,
    val playerId: Int,
    val playerName: String,
    val fromClubId: Int? = null,
    val fromClubName: String? = null,
    val toClubId: Int? = null,
    val toClubName: String? = null,
    val stage: Int,
    val stageName: String,
    val previousStage: Int? = null,
    val fee: String? = null,
    val status: String,
    val transferId: Int? = null,
    val updatedAt: Instant,
    val latestArticleId: Int? = null,
    val history: List<NetworkRumourStage> = emptyList(),
)

/**
 * A stage the rumour reached; [articleId] is null when the stage was changed without an article.
 */
@Serializable
data class NetworkRumourStage(
    val stage: Int,
    val at: Instant,
    val articleId: Int? = null,
)
