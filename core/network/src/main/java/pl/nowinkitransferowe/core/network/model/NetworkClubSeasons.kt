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
 * Spending and income of a club by season from `GET /api/v3/clubs/{id}/seasons`, newest season
 * first. Only seasons with transfers are listed.
 */
@Serializable
data class NetworkClubSeasons(
    val clubId: Int,
    val seasons: List<NetworkClubSeason> = emptyList(),
)

/**
 * One season of a club, amounts in millions of euro counted like in `/leagues`; [arrivals] and
 * [departures] are transfer ids, newest first. Contract extensions are left out.
 */
@Serializable
data class NetworkClubSeason(
    val season: String,
    val spent: Double,
    val earned: Double,
    val balance: Double,
    val arrivals: List<Int> = emptyList(),
    val departures: List<Int> = emptyList(),
)
