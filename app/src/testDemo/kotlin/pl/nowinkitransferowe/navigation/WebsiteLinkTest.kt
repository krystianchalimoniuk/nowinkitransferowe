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

package pl.nowinkitransferowe.navigation

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Test
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.playersTestData
import pl.nowinkitransferowe.core.testing.repository.TestArticleRepository
import pl.nowinkitransferowe.core.testing.repository.TestClubRepository
import pl.nowinkitransferowe.core.testing.repository.TestPlayerRepository
import pl.nowinkitransferowe.feature.articledetails.api.navigation.ArticleNavKey
import pl.nowinkitransferowe.feature.articles.api.navigation.ArticlesNavKey
import pl.nowinkitransferowe.feature.club.api.navigation.ClubNavKey
import pl.nowinkitransferowe.feature.leagues.api.navigation.LeagueNavKey
import pl.nowinkitransferowe.feature.leagues.api.navigation.LeaguesNavKey
import pl.nowinkitransferowe.feature.player.api.navigation.PlayerNavKey
import pl.nowinkitransferowe.feature.rumours.api.navigation.RumoursNavKey
import pl.nowinkitransferowe.feature.search.api.navigation.SearchNavKey
import pl.nowinkitransferowe.feature.topic.api.navigation.TopicNavKey
import pl.nowinkitransferowe.feature.transfers.api.navigation.TransfersNavKey
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WebsiteLinkTest {

    @Test
    fun websitePages_areRecognised() {
        val site = "https://nowinkitransferowe.pl"
        assertEquals(WebsiteLink.Article(11285), WebsiteLink.parse("$site/11285/transfery/oskar-pietuszewski-obserwowany-przez-bayern-monachium"))
        assertEquals(WebsiteLink.Article(11285), WebsiteLink.parse("https://www.nowinkitransferowe.pl/11285"))
        assertEquals(WebsiteLink.Player("robert-lewandowski"), WebsiteLink.parse("$site/transfery/robert-lewandowski"))
        assertEquals(WebsiteLink.Transfers, WebsiteLink.parse("$site/transfery"))
        assertEquals(WebsiteLink.Club("chelsea"), WebsiteLink.parse("$site/klub/chelsea"))
        assertEquals(WebsiteLink.Rumours, WebsiteLink.parse("$site/plotki"))
        assertEquals(WebsiteLink.Leagues, WebsiteLink.parse("$site/ligi/"))
        assertEquals(WebsiteLink.League("premier-league"), WebsiteLink.parse("$site/ligi/premier-league"))
        assertEquals(WebsiteLink.Topic("ekstraklasa"), WebsiteLink.parse("$site/temat/ekstraklasa"))
        assertEquals(WebsiteLink.Tag("bayern-monachium"), WebsiteLink.parse("$site/tag/bayern-monachium"))
        assertEquals(WebsiteLink.Search("Fermín López"), WebsiteLink.parse("$site/szukaj?q=Ferm%C3%ADn+L%C3%B3pez"))
        assertEquals(WebsiteLink.Search(null), WebsiteLink.parse("$site/szukaj"))
    }

    @Test
    fun otherSitesAndPages_areNotRecognised() {
        assertNull(WebsiteLink.parse("https://example.com/11285/x"))
        assertNull(WebsiteLink.parse("https://nowinkitransferowe.pl/"))
        assertNull(WebsiteLink.parse("https://nowinkitransferowe.pl/polityka-prywatnosci-aplikacja"))
        assertNull(WebsiteLink.parse("https://nowinkitransferowe.pl/12abc/x"))
        assertNull(WebsiteLink.parse("not a url"))
    }

    @Test
    fun links_openTheirScreens_afterTheirSection() = runTest {
        val clubs = TestClubRepository().apply { sendClubs(clubsTestData) }
        val players = TestPlayerRepository().apply { sendPlayers(playersTestData) }
        val articles = TestArticleRepository().apply { sendArticles(articlesTestData.take(1).map { it.copy(tags = listOf("Gabriel Martinelli")) }) }
        val clock = object : Clock {
            override fun now() = Instant.parse("2026-11-15T12:00:00Z")
        }
        suspend fun keys(link: WebsiteLink) = link.navKeys(clubs, players, articles, clock)

        assertEquals(listOf(ArticlesNavKey, ArticleNavKey(11285)), keys(WebsiteLink.Article(11285)))
        assertEquals(listOf(ArticlesNavKey, ClubNavKey(83)), keys(WebsiteLink.Club("west-ham-united")))
        assertEquals(listOf(ArticlesNavKey, PlayerNavKey(10281)), keys(WebsiteLink.Player("raphael-varane")))
        assertEquals(listOf(ArticlesNavKey), keys(WebsiteLink.Club("not-on-the-phone")))
        assertEquals(listOf(TransfersNavKey), keys(WebsiteLink.Transfers))
        assertEquals(listOf(RumoursNavKey), keys(WebsiteLink.Rumours))
        assertEquals(listOf(LeaguesNavKey, LeagueNavKey("la-liga", 2026)), keys(WebsiteLink.League("la-liga")))
        assertEquals(listOf(SearchNavKey("chelsea")), keys(WebsiteLink.Search("chelsea")))

        assertEquals(listOf(ArticlesNavKey, TopicNavKey(topic = "ekstraklasa")), keys(WebsiteLink.Topic("ekstraklasa")))
        assertEquals(listOf(ArticlesNavKey, TopicNavKey(tag = "Gabriel Martinelli")), keys(WebsiteLink.Tag("gabriel-martinelli")))
        assertEquals(listOf(ArticlesNavKey), keys(WebsiteLink.Tag("not-a-tag")))
    }
}
