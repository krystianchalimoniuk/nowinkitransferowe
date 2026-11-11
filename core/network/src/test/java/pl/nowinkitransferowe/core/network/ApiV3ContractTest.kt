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

package pl.nowinkitransferowe.core.network

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Test
import pl.nowinkitransferowe.core.network.model.NetworkArticle
import pl.nowinkitransferowe.core.network.model.NetworkArticleAuthor
import pl.nowinkitransferowe.core.network.model.NetworkArticleImage
import pl.nowinkitransferowe.core.network.model.NetworkChange
import pl.nowinkitransferowe.core.network.model.NetworkChanges
import pl.nowinkitransferowe.core.network.model.NetworkClub
import pl.nowinkitransferowe.core.network.model.NetworkClubSeason
import pl.nowinkitransferowe.core.network.model.NetworkClubSeasons
import pl.nowinkitransferowe.core.network.model.NetworkCountry
import pl.nowinkitransferowe.core.network.model.NetworkFee
import pl.nowinkitransferowe.core.network.model.NetworkHotClub
import pl.nowinkitransferowe.core.network.model.NetworkLeagueClub
import pl.nowinkitransferowe.core.network.model.NetworkLeagues
import pl.nowinkitransferowe.core.network.model.NetworkPlayer
import pl.nowinkitransferowe.core.network.model.NetworkRumour
import pl.nowinkitransferowe.core.network.model.NetworkRumourStage
import pl.nowinkitransferowe.core.network.model.NetworkRumourStats
import pl.nowinkitransferowe.core.network.model.NetworkSeason
import pl.nowinkitransferowe.core.network.model.NetworkStageAccuracy
import pl.nowinkitransferowe.core.network.model.NetworkTopic
import pl.nowinkitransferowe.core.network.model.NetworkTransfer
import kotlin.test.assertEquals

/**
 * Parses the examples of the API v3 contract (docs/api-v3.md in the website repository). A failure
 * here means the app and the documented API no longer agree.
 */
class ApiV3ContractTest {

    // The same configuration as NetworkModule: fields added to the API later are ignored.
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun changes_parse() {
        val changes = json.decodeFromString<NetworkChanges>(
            """{"changes":[{"id":20569,"version":20774,"deleted":false}],"latestVersion":20774,"hasMore":false}""",
        )

        assertEquals(
            NetworkChanges(
                changes = listOf(NetworkChange(id = 20569, version = 20774, deleted = false)),
                latestVersion = 20774,
                hasMore = false,
            ),
            changes,
        )
    }

    @Test
    fun article_parses_with_image_author_and_relations() {
        val article = json.decodeFromString<NetworkArticle>(
            """
            {
              "id": 4321,
              "url": "https://nowinkitransferowe.pl/4321/transfery/martinelli-w-al-hilal",
              "title": "Oficjalnie: Martinelli w Al-Hilal",
              "socialTitle": null,
              "kind": "OFFICIAL",
              "topics": ["premier-league"],
              "lead": "Pierwszy akapit…",
              "contentHtml": "<p>…</p>",
              "image": { "url": "https://nowinkitransferowe.pl/media/x.jpg", "thumbnailUrl": "https://nowinkitransferowe.pl/media/w/640/x.jpg.webp" },
              "imageSource": "Arsenal FC",
              "newsSource": "The Athletic",
              "author": { "name": "Ernest Chalimoniuk", "avatarUrl": null },
              "tags": ["Gabriel Martinelli", "Arsenal"],
              "clubIds": [12, 345],
              "playerIds": [12148],
              "featuredUntil": null,
              "publishedAt": "2026-09-07T10:00:00Z"
            }
            """,
        )

        assertEquals(
            NetworkArticle(
                id = 4321,
                url = "https://nowinkitransferowe.pl/4321/transfery/martinelli-w-al-hilal",
                title = "Oficjalnie: Martinelli w Al-Hilal",
                socialTitle = null,
                kind = "OFFICIAL",
                topics = listOf("premier-league"),
                lead = "Pierwszy akapit…",
                contentHtml = "<p>…</p>",
                image = NetworkArticleImage(
                    url = "https://nowinkitransferowe.pl/media/x.jpg",
                    thumbnailUrl = "https://nowinkitransferowe.pl/media/w/640/x.jpg.webp",
                ),
                imageSource = "Arsenal FC",
                newsSource = "The Athletic",
                author = NetworkArticleAuthor(name = "Ernest Chalimoniuk", avatarUrl = null),
                tags = listOf("Gabriel Martinelli", "Arsenal"),
                clubIds = listOf(12, 345),
                playerIds = listOf(12148),
                featuredUntil = null,
                publishedAt = Instant.parse("2026-09-07T10:00:00Z"),
            ),
            article,
        )
    }

    @Test
    fun transfer_parses_with_structured_fee() {
        val transfer = json.decodeFromString<NetworkTransfer>(
            """
            {
              "id": 20569,
              "playerId": 12148,
              "playerName": "Gabriel Martinelli",
              "fromClubId": 3410, "fromClubName": "Arsenal FC",
              "toClubId": 5120, "toClubName": "Al-Hilal SFC",
              "fromStatus": null, "toStatus": null,
              "fee": { "kind": "AMOUNT", "label": "70 mln €", "millionsOfEuro": 70.0 },
              "date": "2026-09-07",
              "articleId": 4321
            }
            """,
        )

        assertEquals(
            NetworkTransfer(
                id = 20569,
                playerId = 12148,
                playerName = "Gabriel Martinelli",
                fromClubId = 3410,
                fromClubName = "Arsenal FC",
                toClubId = 5120,
                toClubName = "Al-Hilal SFC",
                fromStatus = null,
                toStatus = null,
                fee = NetworkFee(kind = "AMOUNT", label = "70 mln €", millionsOfEuro = 70.0),
                date = LocalDate(2026, 9, 7),
                articleId = 4321,
            ),
            transfer,
        )
    }

    @Test
    fun transfer_without_a_club_parses_with_status() {
        val transfer = json.decodeFromString<NetworkTransfer>(
            """
            {
              "id": 1, "playerId": 2, "playerName": "Zawodnik",
              "fromClubId": null, "fromClubName": null, "toClubId": null, "toClubName": null,
              "fromStatus": "FREE_AGENT", "toStatus": "RETIRED",
              "fee": { "kind": "UNKNOWN", "label": "nie wiadomo", "millionsOfEuro": null },
              "date": "2026-04-01", "articleId": null
            }
            """,
        )

        assertEquals("FREE_AGENT", transfer.fromStatus)
        assertEquals("RETIRED", transfer.toStatus)
        assertEquals(null, transfer.fromClubId)
        assertEquals(null, transfer.fee.millionsOfEuro)
    }

    @Test
    fun club_parses() {
        val club = json.decodeFromString<NetworkClub>(
            """
            {
              "id": 3410, "slug": "arsenal", "name": "Arsenal FC",
              "url": "https://nowinkitransferowe.pl/klub/arsenal",
              "crestUrl": "https://nowinkitransferowe.pl/media/w/160/arsenal.png.webp",
              "fullName": "Arsenal Football Club", "country": "gb-eng", "city": "Londyn",
              "founded": 1886, "stadium": "Emirates Stadium", "stadiumCapacity": 60338, "website": "https://www.arsenal.com/",
              "topic": "premier-league"
            }
            """,
        )

        assertEquals(
            NetworkClub(
                id = 3410,
                slug = "arsenal",
                name = "Arsenal FC",
                url = "https://nowinkitransferowe.pl/klub/arsenal",
                crestUrl = "https://nowinkitransferowe.pl/media/w/160/arsenal.png.webp",
                fullName = "Arsenal Football Club",
                country = "gb-eng",
                city = "Londyn",
                founded = 1886,
                stadium = "Emirates Stadium",
                stadiumCapacity = 60338,
                website = "https://www.arsenal.com/",
                topic = "premier-league",
            ),
            club,
        )
    }

    @Test
    fun player_parses() {
        val player = json.decodeFromString<NetworkPlayer>(
            """
            {
              "id": 12148, "slug": "gabriel-martinelli", "name": "Gabriel Martinelli",
              "url": "https://nowinkitransferowe.pl/transfery/gabriel-martinelli",
              "photoUrl": null,
              "fullName": "Gabriel Teodoro Martinelli Silva", "birthDate": "2001-06-18", "birthPlace": "Guarulhos",
              "nationality": "br", "nationality2": "it", "positionGroup": "FW", "position": "napastnik", "heightCm": 180
            }
            """,
        )

        assertEquals(
            NetworkPlayer(
                id = 12148,
                slug = "gabriel-martinelli",
                name = "Gabriel Martinelli",
                url = "https://nowinkitransferowe.pl/transfery/gabriel-martinelli",
                photoUrl = null,
                fullName = "Gabriel Teodoro Martinelli Silva",
                birthDate = LocalDate(2001, 6, 18),
                birthPlace = "Guarulhos",
                nationality = "br",
                nationality2 = "it",
                positionGroup = "FW",
                position = "napastnik",
                heightCm = 180,
            ),
            player,
        )
    }

    @Test
    fun rumour_parses_with_history() {
        val rumour = json.decodeFromString<NetworkRumour>(
            """
            {
              "id": 77,
              "playerId": 12148, "playerName": "Gabriel Martinelli",
              "fromClubId": 3410, "fromClubName": "Arsenal FC",
              "toClubId": 5120, "toClubName": "Al-Hilal SFC",
              "stage": 4, "stageName": "Blisko", "previousStage": 3,
              "fee": "ok. 70 mln €",
              "status": "ACTIVE",
              "transferId": null,
              "updatedAt": "2026-10-20T10:00:00Z",
              "latestArticleId": 4321,
              "history": [
                { "stage": 2, "at": "2026-10-02T08:00:00Z", "articleId": 4300 },
                { "stage": 3, "at": "2026-10-12T19:30:00Z", "articleId": null },
                { "stage": 4, "at": "2026-10-20T10:00:00Z", "articleId": 4321 }
              ]
            }
            """,
        )

        assertEquals(
            NetworkRumour(
                id = 77,
                playerId = 12148,
                playerName = "Gabriel Martinelli",
                fromClubId = 3410,
                fromClubName = "Arsenal FC",
                toClubId = 5120,
                toClubName = "Al-Hilal SFC",
                stage = 4,
                stageName = "Blisko",
                previousStage = 3,
                fee = "ok. 70 mln €",
                status = "ACTIVE",
                transferId = null,
                updatedAt = Instant.parse("2026-10-20T10:00:00Z"),
                latestArticleId = 4321,
                history = listOf(
                    NetworkRumourStage(stage = 2, at = Instant.parse("2026-10-02T08:00:00Z"), articleId = 4300),
                    NetworkRumourStage(stage = 3, at = Instant.parse("2026-10-12T19:30:00Z"), articleId = null),
                    NetworkRumourStage(stage = 4, at = Instant.parse("2026-10-20T10:00:00Z"), articleId = 4321),
                ),
            ),
            rumour,
        )
    }

    @Test
    fun rumour_stats_parse() {
        val stats = json.decodeFromString<NetworkRumourStats>(
            """
            {
              "season": "26/27", "active": 14, "confirmed": 23, "failed": 9, "showAccuracy": true,
              "byStage": [{ "stage": 5, "confirmed": 9, "closed": 10 }, { "stage": 4, "confirmed": 7, "closed": 10 }],
              "hotClubs": [{ "clubId": 3410, "rumours": 6 }],
              "daysToConfirm": 11.5
            }
            """,
        )

        assertEquals(
            NetworkRumourStats(
                season = "26/27",
                active = 14,
                confirmed = 23,
                failed = 9,
                showAccuracy = true,
                byStage = listOf(
                    NetworkStageAccuracy(stage = 5, confirmed = 9, closed = 10),
                    NetworkStageAccuracy(stage = 4, confirmed = 7, closed = 10),
                ),
                hotClubs = listOf(NetworkHotClub(clubId = 3410, rumours = 6)),
                daysToConfirm = 11.5,
            ),
            stats,
        )
    }

    @Test
    fun leagues_parse() {
        val leagues = json.decodeFromString<NetworkLeagues>(
            """
            {
              "season": "26/27",
              "seasons": [{ "slug": "26-27", "label": "26/27", "current": true }, { "slug": "25-26", "label": "25/26", "current": false }],
              "leagues": [{
                "topic": "premier-league", "spent": 2810.5, "earned": 2181.9, "balance": -628.6, "transfers": 125,
                "clubs": [{ "clubId": 3410, "spent": 507.5, "earned": 326.4, "balance": -181.1, "arrivals": 9, "departures": 7 }],
                "topArrivals": [20567, 20412], "topDepartures": [20590]
              }]
            }
            """,
        )

        assertEquals(
            listOf(NetworkSeason(slug = "26-27", label = "26/27", current = true), NetworkSeason(slug = "25-26", label = "25/26", current = false)),
            leagues.seasons,
        )
        val league = leagues.leagues.single()
        assertEquals(-628.6, league.balance)
        assertEquals(
            NetworkLeagueClub(clubId = 3410, spent = 507.5, earned = 326.4, balance = -181.1, arrivals = 9, departures = 7),
            league.clubs.single(),
        )
        assertEquals(listOf(20567, 20412), league.topArrivals)
    }

    @Test
    fun club_seasons_parse() {
        val seasons = json.decodeFromString<NetworkClubSeasons>(
            """
            {
              "clubId": 107,
              "seasons": [{
                "season": "25/26", "spent": 107.0, "earned": 32.5, "balance": -74.5,
                "arrivals": [20431, 20302], "departures": [20455]
              }]
            }
            """,
        )

        assertEquals(
            NetworkClubSeasons(
                clubId = 107,
                seasons = listOf(
                    NetworkClubSeason(
                        season = "25/26",
                        spent = 107.0,
                        earned = 32.5,
                        balance = -74.5,
                        arrivals = listOf(20431, 20302),
                        departures = listOf(20455),
                    ),
                ),
            ),
            seasons,
        )
    }

    @Test
    fun dictionaries_parse() {
        assertEquals(
            listOf(NetworkTopic(slug = "premier-league", name = "Premier League", kind = "LEAGUE")),
            json.decodeFromString<List<NetworkTopic>>("""[{ "slug": "premier-league", "name": "Premier League", "kind": "LEAGUE" }]"""),
        )
        assertEquals(
            listOf(NetworkCountry(code = "gb-eng", name = "Anglia")),
            json.decodeFromString<List<NetworkCountry>>("""[{ "code": "gb-eng", "name": "Anglia" }]"""),
        )
    }

    @Test
    fun fields_added_later_are_ignored() {
        val topic = json.decodeFromString<NetworkTopic>(
            """{ "slug": "ekstraklasa", "name": "Ekstraklasa", "kind": "LEAGUE", "iconUrl": "https://example.com/x.svg" }""",
        )

        assertEquals(NetworkTopic(slug = "ekstraklasa", name = "Ekstraklasa", kind = "LEAGUE"), topic)
    }
}
