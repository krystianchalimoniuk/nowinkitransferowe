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

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.model.UserData
import pl.nowinkitransferowe.core.model.UserNewsResource

class UserNewsResourceTest {

    /**
     * Given: Some user data and news resources
     * When: They are combined using `UserNewsResource.from`
     * Then: The correct UserNewsResources are constructed
     */
    @Test
    fun userNewsResourcesAreConstructedFromNewsResourcesAndUserData() {
        val newsResource1 = NewsResource(
            id = "N1",
            title = "Oficjalnie: Luis Suarez w Interze Miami",
            description = "<p><strong>Imię i nazwisko:<\\/strong> Vitor Roque<\\/p>\\r\\n<p><strong>Poprzedni klub:<\\/strong> Athletico Paranaense<\\/p>\\r\\n<p><strong>Nowy klub:<\\/strong> FC Barcelona<\\/p>\\r\\n<p><strong>Kwota transferu:<\\/strong> 40 mln euro<\\/p>\\r\\n<p><strong>Długość kontraktu:<\\/strong> do 30 czerwca 2027 roku<\\/p>\\r\\n<p>&nbsp;<\\/p>\\r\\n<center>\\r\\n<blockquote class=\\\"twitter-tweet\\\">\\r\\n<p dir=\\\"ltr\\\" lang=\\\"en\\\">Lookin good, Vitor \uD83D\uDC4D <a href=\\\"https:\\/\\/t.co\\/qkFbK9sdBD\\\">pic.twitter.com\\/qkFbK9sdBD<\\/a><\\/p>\\r\\n&mdash; FC Barcelona (@FCBarcelona) <a href=\\\"https:\\/\\/twitter.com\\/FCBarcelona\\/status\\/1740002656180216260?ref_src=twsrc%5Etfw\\\">December 27, 2023<\\/a><\\/blockquote>\\r\\n<\\/center>",
            category = NewsCategory.ITALY,
            isImportant = true,
            publishDate = Instant.parse("2022-10-06T23:00:00.000Z"),
            author = "Redakcja",
            photoSrc = "Inter Miami CF",
            src = "Inter Miami CF",
            imageUrl = "intermiaminw.jpg",
            authPic = "123.jpg",
            authTwitter = "https:\\/\\/twitter.com\\/Nowinkitransfer",
            link = "Transfery\\/oficjalnie-luis-suarez-w-interze-miami",
            topics = listOf("Luis Suarez", "Inter Miami CF", "Gremio"),
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

        val userNewsResource = UserNewsResource(newsResource1, userData)

        // Check that the simple field mappings have been done correctly.
        assertEquals(newsResource1.id, userNewsResource.id)
        assertEquals(newsResource1.title, userNewsResource.title)
        assertEquals(newsResource1.description, userNewsResource.description)
        assertEquals(newsResource1.link, userNewsResource.link)
        assertEquals(newsResource1.imageUrl, userNewsResource.imageUrl)
        assertEquals(newsResource1.publishDate, userNewsResource.publishDate)

        // Check that each Topic has been converted to a FollowedTopic correctly.
        assertEquals(newsResource1.topics.size, userNewsResource.topics.size)
        for (topic in newsResource1.topics) {
            // Construct the expected FollowableTopic.
            assertTrue(userNewsResource.topics.contains(topic))
        }

        // Check that the saved flag is set correctly.
        assertEquals(
            newsResource1.id in userData.bookmarkedNewsResources,
            userNewsResource.isSaved,
        )
    }
}
