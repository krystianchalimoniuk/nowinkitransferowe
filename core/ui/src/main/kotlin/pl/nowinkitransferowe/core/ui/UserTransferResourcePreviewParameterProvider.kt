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

package pl.nowinkitransferowe.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.LocalDate
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Fee
import pl.nowinkitransferowe.core.model.FeeKind
import pl.nowinkitransferowe.core.model.NoClubStatus
import pl.nowinkitransferowe.core.model.PlayerRef
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.model.mapToUserTransfers
import pl.nowinkitransferowe.core.ui.TransfersPreviewParameterData.transfersResources

/**
 * This [PreviewParameterProvider](https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/PreviewParameterProvider)
 * provides list of [UserTransfer] for Composable previews.
 */
class UserTransfersResourcePreviewParameterProvider : PreviewParameterProvider<List<UserTransfer>> {

    override val values: Sequence<List<UserTransfer>> = sequenceOf(transfersResources)
}

object TransfersPreviewParameterData {
    val transfersResources: List<UserTransfer> = listOf(
        Transfer(
            id = 1,
            player = PlayerRef(id = 1, name = "Phil Bardsley"),
            from = ClubRef.Club(id = 1, name = "Stockport"),
            to = ClubRef.Club(id = 2, name = "Burnley FC"),
            fee = Fee(kind = FeeKind.FREE, label = "za darmo", millionsOfEuro = null),
            date = LocalDate(2024, 7, 1),
            articleId = null,
        ),
        Transfer(
            id = 2,
            player = PlayerRef(id = 2, name = "Raphael Varane"),
            from = ClubRef.NoClub(status = NoClubStatus.FREE_AGENT, name = "Bez klubu"),
            to = ClubRef.Club(id = 3, name = "Como 1907"),
            fee = Fee(kind = FeeKind.AMOUNT, label = "1,5 mln €", millionsOfEuro = 1.5),
            date = LocalDate(2024, 7, 29),
            articleId = null,
        ),
        Transfer(
            id = 3,
            player = PlayerRef(id = 3, name = "Mason Greenwood"),
            from = ClubRef.Club(id = 4, name = "Manchester United"),
            to = ClubRef.Club(id = 5, name = "Olympique Marsylia"),
            fee = Fee(kind = FeeKind.LOAN, label = "wypożyczenie", millionsOfEuro = null),
            date = LocalDate(2024, 7, 18),
            articleId = null,
        ),
    ).mapToUserTransfers(PreviewParameterData.userData)
}
