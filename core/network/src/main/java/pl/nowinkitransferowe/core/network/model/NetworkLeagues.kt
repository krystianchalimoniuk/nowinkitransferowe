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
 * Spending and income of the leagues in a season from `GET /api/v3/leagues`, in millions of euro.
 *
 * [seasons] lists the seasons that can be requested; the previous one appears only once its
 * summary is saved.
 */
@Serializable
data class NetworkLeagues(
    val season: String,
    val seasons: List<NetworkSeason>,
    val leagues: List<NetworkLeague>,
)

@Serializable
data class NetworkSeason(
    val slug: String,
    val label: String,
    val current: Boolean,
)

/**
 * One league; [topArrivals] and [topDepartures] are transfer ids.
 */
@Serializable
data class NetworkLeague(
    val topic: String,
    val spent: Double,
    val earned: Double,
    val balance: Double,
    val transfers: Int,
    val clubs: List<NetworkLeagueClub> = emptyList(),
    val topArrivals: List<Int> = emptyList(),
    val topDepartures: List<Int> = emptyList(),
)

@Serializable
data class NetworkLeagueClub(
    val clubId: Int,
    val spent: Double,
    val earned: Double,
    val balance: Double,
    val arrivals: Int,
    val departures: Int,
)
