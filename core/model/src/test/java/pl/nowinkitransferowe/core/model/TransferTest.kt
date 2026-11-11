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
import kotlin.test.assertTrue

class TransferTest {

    @Test
    fun only_amounts_count_towards_spending() {
        assertEquals(70.0, Fee(FeeKind.AMOUNT, "70 mln €", 70.0).countedMillions)
        assertEquals(0.0, Fee(FeeKind.LOAN, "wypożyczenie", null).countedMillions)
        assertEquals(0.0, Fee(FeeKind.FREE, "za darmo", 0.0).countedMillions)
        assertEquals(0.0, Fee(FeeKind.UNDISCLOSED, "nie ujawniono", null).countedMillions)
    }

    @Test
    fun a_side_without_a_club_keeps_its_status() {
        assertEquals(ClubRef.Club(id = 86, name = "Liverpool FC"), ClubRef.fromApi(86, "Liverpool FC", null))
        assertEquals(
            ClubRef.NoClub(NoClubStatus.FREE_AGENT, "Bez klubu"),
            ClubRef.fromApi(null, "Bez klubu", "FREE_AGENT"),
        )
        assertEquals(NoClubStatus.UNKNOWN, (ClubRef.fromApi(null, null, "SOMETHING_NEW") as ClubRef.NoClub).status)
    }

    @Test
    fun the_same_club_on_both_sides_is_an_extension() {
        val club = ClubRef.Club(id = 86, name = "Liverpool FC")

        assertTrue(transfer(from = club, to = club).isExtension)
        assertFalse(transfer(from = club, to = ClubRef.Club(id = 44, name = "AS Saint-Etienne")).isExtension)
        assertFalse(transfer(from = ClubRef.NoClub(NoClubStatus.FREE_AGENT, "Bez klubu"), to = club).isExtension)
    }

    @Test
    fun the_season_comes_from_the_date() {
        assertEquals(Season(2026), transfer(date = LocalDate(2027, 1, 2)).season)
    }

    @Test
    fun unknown_api_values_do_not_break_parsing() {
        assertEquals(FeeKind.UNKNOWN, FeeKind.fromApi("BARTER"))
        assertEquals(ArticleKind.UNKNOWN, ArticleKind.fromApi("PODCAST"))
        assertEquals(TopicKind.UNKNOWN, TopicKind.fromApi(null))
        assertEquals(ArticleKind.OFFICIAL, ArticleKind.fromApi("OFFICIAL"))
    }

    private fun transfer(
        from: ClubRef = ClubRef.Club(id = 44, name = "AS Saint-Etienne"),
        to: ClubRef = ClubRef.Club(id = 86, name = "Liverpool FC"),
        date: LocalDate = LocalDate(2026, 10, 26),
    ) = Transfer(
        id = 20568,
        player = PlayerRef(id = 10281, name = "Djylian N'Guessan"),
        from = from,
        to = to,
        fee = Fee(FeeKind.AMOUNT, "5 mln €", 5.0),
        date = date,
        articleId = 11276,
    )
}
