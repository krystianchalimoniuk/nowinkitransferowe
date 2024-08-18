/*
 * Copyright 2024 The Android Open Source Project
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
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.model.UserData
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.ui.TransfersPreviewParameterData.transfersResources

/**
 * This [PreviewParameterProvider](https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/PreviewParameterProvider)
 * provides list of [UserNewsResource] for Composable previews.
 */
class UserTransfersResourcePreviewParameterProvider : PreviewParameterProvider<List<UserTransferResource>> {

    override val values: Sequence<List<UserTransferResource>> = sequenceOf(transfersResources)
}

object TransfersPreviewParameterData {
    private val userData: UserData = UserData(
        bookmarkedNewsResources = setOf("1", "3"),
        viewedNewsResources = setOf("1", "2", "4"),
        bookmarkedTransferResources = setOf("2", "3"),
        viewedTransferResources = setOf("2,3"),
        darkThemeConfig = DarkThemeConfig.DARK,
        useDynamicColor = false,
        isNewsNotificationsAllowed = true,
        isTransfersNotificationsAllowed = true,
        isGeneralNotificationAllowed = true,
    )
    val transfersResources = listOf(
        UserTransferResource(
            transferResource = TransferResource(
                id = "1",
                name = "Phil Bardsley",
                footballerImg = "aa11twarz.jpg",
                clubTo = "Burnley FC",
                clubToImg = "burnley_herbb.png",
                clubFrom = "Stockport",
                clubFromImg = "stockportcounty_herbb.png",
                price = "za darmo",
                url = "6592/Inne/nowinki-transferowe-na-zywo-",
            ),
            userData = userData,
        ),
        UserTransferResource(
            transferResource = TransferResource(
                id = "2",
                name = "Derek Cornelius",
                footballerImg = "aa11twarz.jpg",
                clubTo = "Vancouver",
                clubToImg = "vancouverwhitecaps_herbb.png",
                clubFrom = "Malmo FF",
                clubFromImg = "malmo_herbb.png",
                price = "nie ujawniono",
                url = "6592/Inne/nowinki-transferowe-na-zywo-",
            ),
            userData = userData,
        ),
        UserTransferResource(
            transferResource = TransferResource(
                id = "3",
                name = "aa11twarz.jpg",
                footballerImg = "AJ Auxerre",
                clubTo = "AJ Auxerre",
                clubToImg = "auxerre_herbb.png",
                clubFrom = "Saint-Etienne",
                clubFromImg = "saintetiennenw2_herbb.png",
                price = "0,5 mln \u20ac",
                url = "6592/Inne/nowinki-transferowe-na-zywo-",
            ),
            userData = userData,
        ),
    )
}
