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

package pl.nowinkitransferowe.core.network

import JvmUnitTestDemoAssetManager
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.network.demo.DemoNtNetworkDataSource
import pl.nowinkitransferowe.core.network.model.NetworkClub
import pl.nowinkitransferowe.core.network.model.NetworkFee
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.network.model.NetworkRumourStage
import pl.nowinkitransferowe.core.network.model.NetworkTransfer
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DemoNtNetworkDataSourceTest {

    private lateinit var subject: DemoNtNetworkDataSource

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        subject = DemoNtNetworkDataSource(
            ioDispatcher = testDispatcher,
            networkJson = Json { ignoreUnknownKeys = true },
            assets = JvmUnitTestDemoAssetManager,
        )
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun testDeserializationOfTransfers() = runTest(testDispatcher) {
        kotlin.test.assertEquals(
            NetworkTransferResource(
                id = 15813,
                name = "Raphael Varane",
                footballerImg = "aa11twarz.jpg",
                clubFrom = "Manchester United",
                clubFromImg = "manchesterunited_herbb.png",
                clubTo = "Como 1907",
                clubToImg = "como_herbb.png",
                price = "za darmo",
                link = "11233/Transfery/oficjalnie-raphael-varane-w-como-1907",
                publishDate = "2024-08-30 15:22:04",
            ),
            subject.getTransferResources().first(),
        )
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun testDeserializationOfNewsResources() = runTest(testDispatcher) {
        kotlin.test.assertEquals(
            NetworkNewsResource(
                id = 11233,
                title = "Oficjalnie: Raphael Varane w Como 1907",
                category = NewsCategory.TRANSFERS,
                isImportant = "Tak",
                description = "<p><strong>Raphael Varane zgodnie z przewidywaniami trafił do beniaminka Serie A.</strong></p>\r\n<p>Como 1907 za pośrednictwem swojej oficjalnej strony internetowej poinformowało o sprowadzeniu na zasadzie wolnego transferu Raphaela Varane'a. Środkowy obrońca po pozytywnym przejściu test&oacute;w medycznych związał się z nowym pracodawcą dwuletnią umową, obowiązującą do 30 czerwca 2026 roku. Co ważne, kontrakt między stronami zawiera opcję przedłużenia o kolejny sezon.</p>\r\n<p>93-krotny reprezentant Francji był do wzięcia za darmo, ponieważ wcześniej rozstał się z Manchesterem United. Wychowanek RC Lens koszulkę drużyny popularnych <em>Czerwonych Diabł&oacute;w</em> przywdziewał od sierpnia 2021 roku, gdy przeni&oacute;sł się na Old Trafford za 40 milion&oacute;w euro z Realu Madryt.</p>\r\n<p>Raphael Varane w 32 spotkaniach poprzedniej kampanii zdobył 1 bramkę. Fachowy serwis \"Transfermarkt\" wycenia doświadczonego zawodnika na 20 milion&oacute;w euro. Warto dodać, że 31-letni defensor w 2018 roku został mistrzem świata, a z <em>Kr&oacute;lewskimi</em> cztery razy sięgnął po Ligę Mistrz&oacute;w.</p>\r\n<p>&nbsp;</p>\r\n<center>\r\n<blockquote class=\"twitter-tweet\">\r\n<p dir=\"ltr\" lang=\"fr\">Bienvenue Rapha 💙 <a href=\"https://t.co/BGQZdvrSYN\">pic.twitter.com/BGQZdvrSYN</a></p>\r\n&mdash; Como1907 (@Como_1907) <a href=\"https://twitter.com/Como_1907/status/1817642881332768853?ref_src=twsrc%5Etfw\">July 28, 2024</a></blockquote>\r\n</center>",
                newsSrc = "Como 1907",
                photoSrc = "Como 1907",
                date = "2024-07-29",
                time = "13:36:00",
                picture = "comonew.jpg",
                author = "Redakcja ",
                authorTwitter = "https://twitter.com/Nowinkitransfer",
                authorPicture = "123.jpg",
                link = "Transfery/oficjalnie-raphael-varane-w-como-1907",
                topics = "Raphael Varane, Como 1907, Manchester United",
            ),
            subject.getNewsResources().find { it.id == 11233 },
        )
    }

    // API v3: the assets in v3/ were recorded from the website on 2026-11-11.

    @Test
    fun v3_transfer_without_a_club_keeps_its_status() = runTest(testDispatcher) {
        assertEquals(
            NetworkTransfer(
                id = 19580,
                playerId = 832,
                playerName = "Łukasz Fabiański",
                fromClubId = null,
                fromClubName = "Bez klubu",
                toClubId = 83,
                toClubName = "West Ham United",
                fromStatus = "FREE_AGENT",
                toStatus = null,
                fee = NetworkFee(kind = "FREE", label = "za darmo", millionsOfEuro = 0.0),
                date = LocalDate(2025, 9, 11),
                articleId = null,
            ),
            subject.getTransfers(listOf(19580)).single(),
        )
    }

    @Test
    fun v3_club_has_facts_from_wikidata() = runTest(testDispatcher) {
        assertEquals(
            NetworkClub(
                id = 86,
                slug = "liverpool",
                name = "Liverpool FC",
                url = "https://nowinkitransferowe.pl/klub/liverpool",
                crestUrl = "https://nowinkitransferowe.pl/media/w/160/liverpoolnowe_herbb.png.webp",
                fullName = "The Liverpool Football Club and Athletic Grounds Limited",
                country = "gb-eng",
                city = "Liverpool",
                founded = 1892,
                stadium = "Anfield",
                stadiumCapacity = 61276,
                website = "https://www.liverpoolfc.com",
                topic = "premier-league",
            ),
            subject.getClubs(listOf(86)).single(),
        )
    }

    @Test
    fun v3_rumour_dates_keep_microseconds() = runTest(testDispatcher) {
        val rumour = subject.getRumours(listOf(5)).single()

        assertEquals(Instant.parse("2026-10-29T13:46:38.543203Z"), rumour.updatedAt)
        assertEquals(
            listOf(NetworkRumourStage(stage = 1, at = Instant.parse("2026-10-29T13:46:38.543203Z"), articleId = 11284)),
            rumour.history,
        )
    }

    @Test
    fun v3_article_links_clubs_and_players() = runTest(testDispatcher) {
        val article = subject.getArticles(listOf(11276)).single()

        assertEquals("OFFICIAL", article.kind)
        assertEquals(listOf("premier-league", "ligue-1"), article.topics)
        assertEquals(listOf(86, 44, 726), article.clubIds)
        assertEquals(listOf(10281), article.playerIds)
        assertEquals("https://nowinkitransferowe.pl/media/w/640/liverpoolnowy.jpg.webp", article.image?.thumbnailUrl)
    }

    @Test
    fun v3_records_skip_unknown_ids() = runTest(testDispatcher) {
        assertEquals(listOf(86), subject.getClubs(listOf(86, -1)).map(NetworkClub::id))
        assertEquals(emptyList(), subject.getArticles(emptyList()))
    }

    @Test
    fun v3_changes_are_paged_in_ascending_order() = runTest(testDispatcher) {
        val first = subject.getChanges(NetworkRecordType.TRANSFERS, after = 0, limit = 100)
        val second = subject.getChanges(NetworkRecordType.TRANSFERS, after = first.changes.last().version, limit = 100)

        assertEquals(100, first.changes.size)
        assertTrue(first.hasMore)
        assertEquals(first.changes.sortedBy { it.version }, first.changes)
        assertEquals(10, second.changes.size)
        assertFalse(second.hasMore)
        assertEquals(20741, second.latestVersion)
        assertEquals(second.changes.last().version, second.latestVersion)
    }

    @Test
    fun v3_club_seasons_come_from_the_demo_transfers() = runTest(testDispatcher) {
        val liverpool = subject.getClubSeasons(86)
        val transfers = subject.getTransfers(liverpool.seasons.flatMap { it.arrivals + it.departures })

        assertEquals("26/27", liverpool.seasons.first().season)
        assertTrue(transfers.all { it.toClubId == 86 || it.fromClubId == 86 })
        assertEquals(
            liverpool.seasons.first().spent,
            transfers.filter { it.id in liverpool.seasons.first().arrivals }.sumOf { it.fee.millionsOfEuro ?: 0.0 },
        )
        assertEquals(emptyList(), subject.getClubSeasons(-1).seasons)
    }

    @Test
    fun v3_every_record_referenced_by_leagues_is_in_the_demo_data() = runTest(testDispatcher) {
        val leagues = subject.getLeagues()
        val clubIds = leagues.leagues.flatMap { league -> league.clubs.map { it.clubId } }
        val transferIds = leagues.leagues.flatMap { it.topArrivals + it.topDepartures }

        assertEquals(clubIds.toSet(), subject.getClubs(clubIds.distinct()).map(NetworkClub::id).toSet())
        assertEquals(transferIds.toSet(), subject.getTransfers(transferIds.distinct()).map(NetworkTransfer::id).toSet())
        assertEquals(12, subject.getTopics().size)
        assertTrue(subject.getCountries().any { it.code == "gb-eng" && it.name == "Anglia" })
    }
}
