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

package pl.nowinkitransferowe.feature.club.impl

import pl.nowinkitransferowe.core.model.ClubSeason
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Season

internal val westHamSeasonsTestData = listOf(
    ClubSeason(Season(2026), spent = 313.0, earned = 30.0, balance = -283.0, arrivals = emptyList(), departures = emptyList()),
    ClubSeason(Season(2025), spent = 300.0, earned = 16.0, balance = -284.0, arrivals = emptyList(), departures = emptyList()),
    ClubSeason(Season(2024), spent = 41.0, earned = 58.0, balance = 17.0, arrivals = emptyList(), departures = emptyList()),
)

internal val countriesTestData = listOf(Country("gb-eng", "Anglia"), Country("it", "Włochy"))
