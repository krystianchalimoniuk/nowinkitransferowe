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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferResourceRepository
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.model.mapToUserTransferResources
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import kotlin.test.assertEquals

class CompositeUserTransferResourceRepositoryTest {

    private val transferRepository = TestTransferRepository()
    private val userDataRepository = TestUserDataRepository()

    private val userTransferResourceRepository = CompositeUserTransferResourceRepository(
        transferRepository = transferRepository,
        userDataRepository = userDataRepository,
    )

    @Test
    fun whenNoFilters_allTransferResourcesAreReturned() = runTest {
        // Obtain the user transfer resources flow.
        val userTransferResources = userTransferResourceRepository.observeAll()

        // Send some transfer resources and user data into the data repositories.
        transferRepository.sendTransferResources(sampleTransferResources)

        // Construct the test user data with bookmarks.
        val userData = emptyUserData.copy(
            bookmarkedTransferResources = setOf(sampleTransferResources[0].id, sampleTransferResources[2].id),
        )

        userDataRepository.setUserData(userData)

        // Check that the correct transfer resources are returned with their bookmarked state.
        assertEquals(
            sampleTransferResources.mapToUserTransferResources(userData),
            userTransferResources.first(),
        )
    }

    @Test
    fun whenFilteredByBookmarkedResources_matchingTransferResourcesAreReturned() = runTest {
        // Obtain the bookmarked user transfer resources flow.
        val userTransferResources = userTransferResourceRepository.observeAllBookmarked()

        // Send some transfer resources and user data into the data repositories.
        transferRepository.sendTransferResources(sampleTransferResources)

        // Construct the test user data with bookmarks.
        val userData = emptyUserData.copy(
            bookmarkedTransferResources = setOf(sampleTransferResources[0].id, sampleTransferResources[2].id),
        )

        userDataRepository.setUserData(userData)

        // Check that the correct transfer resources are returned with their bookmarked state.
        assertEquals(
            listOf(sampleTransferResources[0], sampleTransferResources[2]).mapToUserTransferResources(userData),
            userTransferResources.first(),
        )
    }
}

val sampleTransferResources: List<TransferResource> = listOf(
    TransferResource(
        id = "1",
        name = "Phil Bardsley",
        footballerImg = "aa11twarz.jpg",
        clubTo = "Burnley FC",
        clubToImg = "burnley_herbb.png",
        clubFrom = "Stockport",
        clubFromImg = "stockportcounty_herbb.png",
        price = "za darmo",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "2",
        name = "Derek Cornelius",
        footballerImg = "aa11twarz.jpg",
        clubTo = "Vancouver",
        clubToImg = "vancouverwhitecaps_herbb.png",
        clubFrom = "Malmo FF",
        clubFromImg = "malmo_herbb.png",
        price = "nie ujawniono",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
    TransferResource(
        id = "3",
        name = "Allan Saint-Maximeu",
        footballerImg = "aa11twarz.jpg",
        clubTo = "AJ Auxerre",
        clubToImg = "auxerre_herbb.png",
        clubFrom = "Saint-Etienne",
        clubFromImg = "saintetiennenw2_herbb.png",
        price = "0,5 mln \u20ac",
        url = "6592/Inne/nowinki-transferowe-na-zywo-",
        season = "23/24",
        publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
    ),
)
