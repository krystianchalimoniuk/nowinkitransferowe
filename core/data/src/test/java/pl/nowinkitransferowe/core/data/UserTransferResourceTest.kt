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

package pl.nowinkitransferowe.core.data

import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.model.UserData
import pl.nowinkitransferowe.core.model.UserTransferResource

class UserTransferResourceTest {

    /**
     * Given: Some user data and transfer resources
     * When: They are combined using `UserTransferResource.from`
     * Then: The correct UserTransferResources are constructed
     */
    @Test
    fun userTransferResourcesAreConstructedFromTransferResourcesAndUserData() {
        val transferResource1 = TransferResource(
            id = "N1",
            name = "Phil Bardsley",
            footballerImg = "aa11twarz.jpg",
            clubTo = "Burnley FC",
            clubToImg = "burnley_herbb.png",
            clubFrom = "Stockport",
            clubFromImg = "stockportcounty_herbb.png",
            price = "za darmo",
            url = "6592/Inne/nowinki-transferowe-na-zywo-",
            season = "23/24",
            publishDate = "2022-10-06T23:00:00.000Z".toInstant()
        )

        val userData = UserData(
            bookmarkedNewsResources = setOf("N1"),
            viewedNewsResources = setOf("N1"),
            bookmarkedTransferResources = setOf("N1"),
            viewedTransferResources = setOf("N1"),
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            useDynamicColor = false,
            isNewsNotificationsAllowed = true,
            isTransfersNotificationsAllowed = true,
            isGeneralNotificationAllowed = true,
        )

        val userTransferResource = UserTransferResource(transferResource1, userData)

        // Check that the simple field mappings have been done correctly.
        assertEquals(transferResource1.id, userTransferResource.id)
        assertEquals(transferResource1.name, userTransferResource.name)
        assertEquals(transferResource1.clubFrom, userTransferResource.clubFrom)
        assertEquals(transferResource1.clubFromImg, userTransferResource.clubFromImg)
        assertEquals(transferResource1.clubTo, userTransferResource.clubTo)
        assertEquals(transferResource1.clubToImg, userTransferResource.clubToImg)
        assertEquals(transferResource1.price, userTransferResource.price)
        assertEquals(transferResource1.url, userTransferResource.url)

        // Check that the saved flag is set correctly.
        assertEquals(
            userTransferResource.id in userData.bookmarkedTransferResources,
            userTransferResource.isSaved,
        )
    }
}
