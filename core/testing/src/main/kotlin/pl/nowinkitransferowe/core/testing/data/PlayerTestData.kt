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

import kotlinx.datetime.LocalDate
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Country
import pl.nowinkitransferowe.core.model.Fee
import pl.nowinkitransferowe.core.model.FeeKind
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.PlayerRef
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.model.mapToUserTransfers
import pl.nowinkitransferowe.core.testing.repository.emptyUserData

/** A player with every fact filled in, for the player screen. */
val lewandowskiTestData = Player(
    id = 500, slug = "robert-lewandowski", name = "Robert Lewandowski", url = "https://nowinkitransferowe.pl/transfery/robert-lewandowski",
    photoUrl = null, fullName = "Robert Lewandowski", birthDate = LocalDate(1988, 8, 21), birthPlace = "Warszawa",
    nationality = "pl", nationality2 = null, positionGroup = null, position = "napastnik", heightCm = 185,
)

val polandTestData = Country("pl", "Polska")

private fun transfer(id: Int, date: LocalDate, from: String, to: Pair<Int, String>, fee: Fee) = Transfer(
    id = id,
    player = PlayerRef(id = 500, name = "Robert Lewandowski", nationality = "pl"),
    from = ClubRef.Club(id = id * 10, name = from),
    to = ClubRef.Club(id = to.first, name = to.second),
    fee = fee,
    date = date,
    articleId = null,
)

/** Transfers of [lewandowskiTestData], newest first, like the player screen gets them. */
val lewandowskiTransfersTestData: List<UserTransfer> = listOf(
    transfer(4, LocalDate(2026, 7, 1), "FC Barcelona", 900 to "Chicago Fire", Fee(FeeKind.FREE, "za darmo", null)),
    transfer(3, LocalDate(2022, 7, 19), "Bayern Monachium", 901 to "FC Barcelona", Fee(FeeKind.AMOUNT, "45 mln €", 45.0)),
    transfer(2, LocalDate(2014, 7, 1), "Borussia Dortmund", 902 to "Bayern Monachium", Fee(FeeKind.FREE, "za darmo", null)),
    transfer(1, LocalDate(2010, 7, 1), "Lech Poznań", 903 to "Borussia Dortmund", Fee(FeeKind.AMOUNT, "4,75 mln €", 4.75)),
    transfer(0, LocalDate(2008, 7, 1), "Znicz Pruszków", 904 to "Lech Poznań", Fee(FeeKind.AMOUNT, "380 tys. €", 0.38)),
).mapToUserTransfers(emptyUserData)
