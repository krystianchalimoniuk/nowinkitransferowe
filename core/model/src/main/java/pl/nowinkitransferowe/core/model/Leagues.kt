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
 * Spending and income of the leagues in [season], in millions of euro. [seasons] are the seasons
 * that can be shown; the season switch appears only when there are two.
 */
data class LeagueSummary(
    val season: Season,
    val seasons: List<SeasonOption>,
    val leagues: List<League>,
)

data class SeasonOption(
    val season: Season,
    val current: Boolean,
)

/**
 * One league, from the topic with [topic] slug; [topArrivals] and [topDepartures] are transfer ids.
 */
data class League(
    val topic: String,
    val spent: Double,
    val earned: Double,
    val balance: Double,
    val transfers: Int,
    val clubs: List<LeagueClub>,
    val topArrivals: List<Int>,
    val topDepartures: List<Int>,
)

data class LeagueClub(
    val clubId: Int,
    val spent: Double,
    val earned: Double,
    val balance: Double,
    val arrivals: Int,
    val departures: Int,
)

/**
 * Spending and income of a club in [season], in millions of euro; [arrivals] and [departures] are
 * transfer ids, newest first.
 */
data class ClubSeason(
    val season: Season,
    val spent: Double,
    val earned: Double,
    val balance: Double,
    val arrivals: List<Int>,
    val departures: List<Int>,
)
