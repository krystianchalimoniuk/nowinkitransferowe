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

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Network representation of a transfer from `GET /api/v3/transfers`.
 *
 * When a side has no club, its club id is null and its status is `FREE_AGENT`, `RETIRED` or
 * `UNKNOWN`.
 */
@Serializable
data class NetworkTransfer(
    val id: Int,
    val playerId: Int,
    val playerName: String,
    val fromClubId: Int? = null,
    val fromClubName: String? = null,
    val toClubId: Int? = null,
    val toClubName: String? = null,
    val fromStatus: String? = null,
    val toStatus: String? = null,
    val fee: NetworkFee,
    val date: LocalDate,
    val articleId: Int? = null,
)

/**
 * Transfer fee: [kind] is one of `AMOUNT`, `FREE`, `LOAN`, `UNDISCLOSED` or `UNKNOWN`, and
 * [millionsOfEuro] is set only for `AMOUNT`.
 */
@Serializable
data class NetworkFee(
    val kind: String,
    val label: String,
    val millionsOfEuro: Double? = null,
)
