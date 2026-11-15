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

package pl.nowinkitransferowe.core.data.news

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Test
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Fee
import pl.nowinkitransferowe.core.model.FeeKind
import pl.nowinkitransferowe.core.model.NewsChannel
import pl.nowinkitransferowe.core.model.NewsItem
import pl.nowinkitransferowe.core.model.NewsSubject
import pl.nowinkitransferowe.core.model.PlayerRef
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.model.UserData
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NewsSelectorTest {

    private val now = Instant.parse("2026-11-15T12:00:00Z")

    private val liverpool = NewsSubject.Club(4, "Liverpool FC", null)
    private val directory = NewsDirectory(
        clubs = mapOf(4 to liverpool, 9 to NewsSubject.Club(9, "Real Madryt", null)),
        players = mapOf(3001 to NewsSubject.Player(3001, "Gabriel Martinelli", null)),
        clubTopics = mapOf(4 to "premier-league", 9 to "la-liga"),
        leagueNames = mapOf("premier-league" to "Premier League", "la-liga" to "La Liga"),
    )

    private val followingLiverpool: UserData = emptyUserData.copy(
        followedClubs = setOf(4),
        followedPlayers = setOf(3001),
        notifyFollowedTransfers = true,
        notifyFollowedRumours = true,
        notifyFollowedArticles = false,
    )

    private fun transfer(id: Int, toId: Int, millions: Double?, kind: FeeKind = FeeKind.AMOUNT) = Transfer(
        id = id,
        player = PlayerRef(100 + id, "Zawodnik $id"),
        from = ClubRef.Club(50, "Inny klub"),
        to = ClubRef.Club(toId, directory.clubs[toId]?.name ?: "Klub"),
        fee = Fee(kind, millions?.let { "$it mln €" } ?: "za darmo", millions),
        date = LocalDate(2026, 11, id),
        articleId = null,
    )

    private fun select(
        transfers: List<Transfer> = emptyList(),
        rumours: List<pl.nowinkitransferowe.core.model.Rumour> = emptyList(),
        articles: List<pl.nowinkitransferowe.core.model.Article> = emptyList(),
        userData: UserData = followingLiverpool,
        notified: Set<String> = emptySet(),
    ) = selectNews(transfers, rumours, articles, userData, notified, directory, now)

    @Test
    fun severalNewsAboutAFollowedClub_makeOneGroup_newestFirst() {
        val gavi = rumoursTestData[1] // Barcelona -> Liverpool
        val groups = select(transfers = listOf(transfer(1, 4, 5.0), transfer(2, 4, null, FeeKind.FREE)), rumours = listOf(gavi))

        assertEquals(1, groups.size)
        assertEquals(liverpool, groups[0].subject)
        assertEquals(NewsChannel.FOLLOWED, groups[0].channel)
        assertEquals(listOf("rumour:4:1", "transfer:2", "transfer:1"), groups[0].items.map { it.key })
    }

    @Test
    fun followedPlayer_isTheSubjectWhenNoClubIsFollowed() {
        val martinelli = rumoursTestData[0]
        val groups = select(rumours = listOf(martinelli), userData = followingLiverpool.copy(followedClubs = emptySet()))

        assertEquals<NewsSubject?>(directory.players[3001], groups.single().subject)
    }

    @Test
    fun switchedOffKinds_andAlreadyNotifiedItems_areLeftOut() {
        val article = articlesTestData[0].copy(clubIds = listOf(4))
        val noRumours = followingLiverpool.copy(notifyFollowedRumours = false)

        val groups = select(
            transfers = listOf(transfer(1, 4, 5.0)),
            rumours = listOf(rumoursTestData[1]),
            articles = listOf(article),
            userData = noRumours,
            notified = setOf("transfer:1"),
        )

        assertTrue(groups.isEmpty())
    }

    @Test
    fun followedLeague_getsOnlyImportantNews() {
        val leagues = emptyUserData.copy(isFollowedLeaguesNotificationsAllowed = true, followedTopics = setOf("la-liga"))
        val agreement = rumoursTestData[1].copy(id = 7, to = ClubRef.Club(9, "Real Madryt"), stage = RumourStage.AGREEMENT)
        val featured = articlesTestData[0].copy(id = 70, topics = listOf("la-liga"), featuredUntil = Instant.parse("2026-11-16T00:00:00Z"))
        val notFeatured = featured.copy(id = 71, featuredUntil = null)

        val groups = select(
            transfers = listOf(transfer(1, 9, 25.0), transfer(2, 9, 10.0)),
            rumours = listOf(agreement, agreement.copy(id = 8, stage = RumourStage.TALKS)),
            articles = listOf(featured, notFeatured),
            userData = leagues,
        )

        val group = groups.single()
        assertEquals(NewsSubject.League("la-liga", "La Liga"), group.subject)
        assertEquals(NewsChannel.LEAGUES, group.channel)
        assertEquals(setOf("transfer:1", "rumour:7:5", "article:70"), group.items.map { it.key }.toSet())
    }

    @Test
    fun followedClub_winsOverItsLeague() {
        val both = followingLiverpool.copy(isFollowedLeaguesNotificationsAllowed = true, followedTopics = setOf("premier-league"))

        val groups = select(transfers = listOf(transfer(1, 4, 30.0)), userData = both)

        assertEquals(NewsChannel.FOLLOWED, groups.single().channel)
    }

    @Test
    fun leaguesSwitchedOff_notifyNothing() {
        val off = emptyUserData.copy(isFollowedLeaguesNotificationsAllowed = false, followedTopics = setOf("la-liga"))

        assertTrue(select(transfers = listOf(transfer(1, 9, 25.0)), userData = off).isEmpty())
    }

    @Test
    fun rumourKey_changesWithTheStage() {
        assertEquals("rumour:4:1", NewsItem.RumourNews(rumoursTestData[1]).key)
        assertEquals("rumour:4:3", NewsItem.RumourNews(rumoursTestData[1].copy(stage = RumourStage.TALKS)).key)
    }

    @Test
    fun quietHours_areFrom23To7_andEndAtSevenTheNextMorning() {
        val warsaw = TimeZone.of("Europe/Warsaw")
        assertTrue(NewsNotifications.isQuietHour(Instant.parse("2026-11-15T22:30:00Z"), warsaw))
        assertTrue(NewsNotifications.isQuietHour(Instant.parse("2026-11-16T05:59:00Z"), warsaw))
        assertFalse(NewsNotifications.isQuietHour(Instant.parse("2026-11-16T06:00:00Z"), warsaw))
        assertFalse(NewsNotifications.isQuietHour(Instant.parse("2026-11-15T21:59:00Z"), warsaw))

        // 23:30 in Warsaw -> 7:00 is 7.5 hours later; 3:00 -> 4 hours later
        assertEquals(7.5 * 3_600_000, NewsNotifications.untilQuietHoursEnd(Instant.parse("2026-11-15T22:30:00Z"), warsaw).toDouble())
        assertEquals(4.0 * 3_600_000, NewsNotifications.untilQuietHoursEnd(Instant.parse("2026-11-16T02:00:00Z"), warsaw).toDouble())
    }

    @Test
    fun mutedSubject_isLeftOutUntilTheMuteEnds() {
        val muted = followingLiverpool.copy(mutedNewsSubjects = mapOf("club:4" to Instant.parse("2026-11-16T00:00:00Z").toEpochMilliseconds()))

        assertTrue(select(transfers = listOf(transfer(1, 4, 5.0)), userData = muted).isEmpty())
        val tomorrow = selectNews(listOf(transfer(1, 4, 5.0)), emptyList(), emptyList(), muted, emptySet(), directory, Instant.parse("2026-11-16T08:00:00Z"))
        assertEquals(1, tomorrow.size)
    }
}
