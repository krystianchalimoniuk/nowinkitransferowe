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

package pl.nowinkitransferowe.feature.leagues.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import pl.nowinkitransferowe.core.navigation.Navigator

/** The leagues compared by spending and income. */
@Serializable
object LeaguesNavKey : NavKey

/** One league in a season: its clubs, the most expensive transfers and the other leagues. */
@Serializable
data class LeagueNavKey(val topic: String, val seasonStartYear: Int) : NavKey

fun Navigator.navigateToLeague(topic: String, seasonStartYear: Int) {
    navigate(LeagueNavKey(topic, seasonStartYear))
}
