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
import pl.nowinkitransferowe.core.model.Fee
import pl.nowinkitransferowe.core.model.FeeKind
import pl.nowinkitransferowe.core.model.NoClubStatus
import pl.nowinkitransferowe.core.model.PlayerRef
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.model.mapToUserTransfers
import pl.nowinkitransferowe.core.testing.repository.emptyUserData

/** 12 transfers, newest first like the repositories return them; the first and the last are of the same player. */
val transfersTestData: List<Transfer> = listOf(
    Transfer(
        id = 19580,
        player = PlayerRef(id = 832, name = "Łukasz Fabiański"),
        from = ClubRef.NoClub(status = NoClubStatus.FREE_AGENT, name = "Bez klubu"),
        to = ClubRef.Club(id = 83, name = "West Ham United", crestUrl = "https://nowinkitransferowe.pl/media/w/160/westham_herbb.png.webp"),
        fee = Fee(kind = FeeKind.FREE, label = "za darmo", millionsOfEuro = null),
        date = LocalDate(2026, 10, 20),
        articleId = 11284,
    ),
    Transfer(
        id = 14999,
        player = PlayerRef(id = 10281, name = "Raphael Varane"),
        from = ClubRef.Club(id = 11, name = "Manchester United", crestUrl = null),
        to = ClubRef.Club(id = 86, name = "Como 1907", crestUrl = null),
        fee = Fee(kind = FeeKind.LOAN, label = "wypożyczenie", millionsOfEuro = null),
        date = LocalDate(2024, 7, 29),
        articleId = 11283,
    ),
) + (1..9).map { day ->
    Transfer(
        id = 14000 - day,
        player = PlayerRef(id = 20000 + day, name = "Zawodnik $day"),
        from = ClubRef.Club(id = 11, name = "Manchester United", crestUrl = null),
        to = ClubRef.Club(id = 86, name = "Como 1907", crestUrl = null),
        fee = Fee(kind = FeeKind.AMOUNT, label = "$day mln €", millionsOfEuro = day.toDouble()),
        date = LocalDate(2024, 7, 28 - day),
        articleId = null,
    )
} + listOf(
    Transfer(
        id = 15000,
        player = PlayerRef(id = 832, name = "Łukasz Fabiański"),
        from = ClubRef.Club(id = 90, name = "Swansea City", crestUrl = null),
        to = ClubRef.Club(id = 83, name = "West Ham United", crestUrl = null),
        fee = Fee(kind = FeeKind.AMOUNT, label = "8 mln €", millionsOfEuro = 8.0),
        date = LocalDate(2018, 6, 20),
        articleId = null,
    ),
)

/** [transfersTestData] with nothing saved or viewed. */
val userTransfersTestData: List<UserTransfer> = transfersTestData.mapToUserTransfers(emptyUserData)

/** [transfersTestData] all saved, as the bookmarks screen shows them. */
val savedUserTransfersTestData: List<UserTransfer> = transfersTestData.mapToUserTransfers(
    emptyUserData.copy(bookmarkedTransfers = transfersTestData.map { it.id }.toSet()),
)
