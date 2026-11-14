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

package pl.nowinkitransferowe.core.testing.data

import pl.nowinkitransferowe.core.model.League
import pl.nowinkitransferowe.core.model.LeagueClub
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.model.SeasonOption

/** Two seasons, the current one first, so the season switch shows. */
val leagueSeasonsTestData = listOf(SeasonOption(Season(2026), current = true), SeasonOption(Season(2025), current = false))

/** Leagues of 26/27 in the server's order; the clubs and transfers are in the other test data. */
val leaguesTestData = listOf(
    League(
        topic = "premier-league",
        spent = 4165.25,
        earned = 2571.75,
        balance = -1593.5,
        transfers = 273,
        clubs = listOf(
            LeagueClub(clubId = 83, spent = 523.0, earned = 336.4, balance = -186.6, arrivals = 8, departures = 14),
        ),
        topArrivals = listOf(19580),
        topDepartures = emptyList(),
    ),
    League(
        topic = "serie-a",
        spent = 871.7,
        earned = 494.3,
        balance = -377.4,
        transfers = 261,
        clubs = listOf(
            LeagueClub(clubId = 86, spent = 120.0, earned = 20.0, balance = -100.0, arrivals = 6, departures = 4),
        ),
        topArrivals = listOf(14999),
        topDepartures = listOf(14999),
    ),
    League(
        topic = "ekstraklasa",
        spent = 30.2,
        earned = 51.3,
        balance = 21.1,
        transfers = 156,
        clubs = emptyList(),
        topArrivals = emptyList(),
        topDepartures = emptyList(),
    ),
)
