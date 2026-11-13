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

import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.Player

val clubsTestData: List<Club> = listOf(
    Club(
        id = 83, slug = "west-ham-united", name = "West Ham United", url = "https://nowinkitransferowe.pl/klub/west-ham-united",
        crestUrl = null, fullName = null, country = "gb-eng", city = "Londyn", founded = 1895, stadium = null,
        stadiumCapacity = null, website = null, topic = "premier-league",
    ),
    Club(
        id = 86, slug = "como-1907", name = "Como 1907", url = "https://nowinkitransferowe.pl/klub/como-1907",
        crestUrl = null, fullName = null, country = "it", city = "Como", founded = 1907, stadium = null,
        stadiumCapacity = null, website = null, topic = "serie-a",
    ),
)

val playersTestData: List<Player> = listOf(
    Player(
        id = 10281, slug = "raphael-varane", name = "Raphael Varane", url = "https://nowinkitransferowe.pl/transfery/raphael-varane",
        photoUrl = null, fullName = null, birthDate = null, birthPlace = null, nationality = "fr", nationality2 = null,
        positionGroup = null, position = null, heightCm = null,
    ),
)
