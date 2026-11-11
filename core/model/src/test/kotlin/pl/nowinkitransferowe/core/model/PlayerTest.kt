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

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerTest {

    @Test
    fun player_age_counts_full_years() {
        val player = Player(
            id = 12148, slug = "gabriel-martinelli", name = "Gabriel Martinelli", url = null, photoUrl = null,
            fullName = null, birthDate = LocalDate(2001, 6, 18), birthPlace = null, nationality = "br",
            nationality2 = null, positionGroup = PositionGroup.fromApi("FW"), position = null, heightCm = 180,
        )

        assertEquals(24, player.ageOn(LocalDate(2026, 6, 17)))
        assertEquals(25, player.ageOn(LocalDate(2026, 6, 18)))
        assertEquals(PositionGroup.FW, player.positionGroup)
        assertFalse(player.isStub)
        assertNull(player.copy(birthDate = null).ageOn(LocalDate(2026, 6, 18)))
    }

    @Test
    fun a_club_or_player_without_slug_is_a_stub() {
        val stub = Club(
            id = 86, slug = null, name = "Liverpool FC", url = null, crestUrl = null, fullName = null, country = null,
            city = null, founded = null, stadium = null, stadiumCapacity = null, website = null, topic = null,
        )

        assertTrue(stub.isStub)
        assertFalse(stub.copy(slug = "liverpool").isStub)
    }
}
