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
    fun after_a_loan_ends_the_player_is_back_at_the_loaning_club() {
        val loan = transfer(fee = Fee(FeeKind.LOAN, "wypożyczenie", null), loanUntil = LocalDate(2027, 6, 30))

        assertTrue(loan.isLoanOn(LocalDate(2027, 6, 30)))
        assertEquals(loan.to, loan.clubOn(LocalDate(2027, 6, 30)))
        assertFalse(loan.isLoanOn(LocalDate(2027, 7, 1)))
        assertEquals(loan.from, loan.clubOn(LocalDate(2027, 7, 1)))
    }

    @Test
    fun a_loan_without_an_end_and_other_transfers_keep_the_destination() {
        val openLoan = transfer(fee = Fee(FeeKind.LOAN, "wypożyczenie", null))
        val sale = transfer(loanUntil = LocalDate(2020, 6, 30))

        assertTrue(openLoan.isLoanOn(LocalDate(2030, 1, 1)))
        assertEquals(openLoan.to, openLoan.clubOn(LocalDate(2030, 1, 1)))
        assertFalse(sale.isLoanOn(LocalDate(2026, 1, 1)))
        assertEquals(sale.to, sale.clubOn(LocalDate(2026, 1, 1)))
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
        fee: Fee = Fee(FeeKind.AMOUNT, "5 mln €", 5.0),
        loanUntil: LocalDate? = null,
    ) = Transfer(
        id = 20568,
        player = PlayerRef(id = 10281, name = "Djylian N'Guessan"),
        from = from,
        to = to,
        fee = fee,
        date = date,
        articleId = 11276,
        loanUntil = loanUntil,
    )
}
