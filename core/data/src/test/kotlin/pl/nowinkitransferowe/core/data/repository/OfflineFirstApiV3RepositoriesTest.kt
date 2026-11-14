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

package pl.nowinkitransferowe.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.testdoubles.TestNtNetworkDataSource
import pl.nowinkitransferowe.core.database.NtDatabase
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.datastoreproto.UserPreferences
import pl.nowinkitransferowe.core.datastoretest.InMemoryDataStore
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.NoClubStatus
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.network.MAX_IDS_PER_REQUEST
import pl.nowinkitransferowe.core.network.NetworkRecordType
import pl.nowinkitransferowe.core.testing.notifications.TestNotifier
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The API v3 repositories against an in-memory database, so the foreign keys and stubs are
 * checked as on a device.
 */
@RunWith(RobolectricTestRunner::class)
class OfflineFirstApiV3RepositoriesTest {

    private lateinit var db: NtDatabase
    private lateinit var network: TestNtNetworkDataSource
    private lateinit var preferences: NtPreferencesDataSource
    private lateinit var synchronizer: Synchronizer
    private lateinit var notifier: TestNotifier

    private lateinit var articles: OfflineFirstArticleRepository
    private lateinit var transfers: OfflineFirstTransferRepository
    private lateinit var rumours: OfflineFirstRumourRepository
    private lateinit var clubs: OfflineFirstClubRepository
    private lateinit var players: OfflineFirstPlayerRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NtDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        network = TestNtNetworkDataSource()
        preferences = NtPreferencesDataSource(InMemoryDataStore(UserPreferences.getDefaultInstance()))
        synchronizer = TestSynchronizer(preferences)

        notifier = TestNotifier()
        articles = OfflineFirstArticleRepository(db.articleDao(), db.clubDao(), db.playerDao(), network, preferences, notifier)
        transfers = OfflineFirstTransferRepository(db.transferDao(), db.articleDao(), db.clubDao(), db.playerDao(), network, preferences, notifier)
        rumours = OfflineFirstRumourRepository(db.rumourDao(), db.transferDao(), db.articleDao(), db.clubDao(), db.playerDao(), network)
        clubs = OfflineFirstClubRepository(db.clubDao(), network)
        players = OfflineFirstPlayerRepository(db.playerDao(), network)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun syncAll(): List<Boolean> = listOf(articles, transfers, rumours, clubs, players).map { it.syncWith(synchronizer) }

    @Test
    fun full_sync_saves_every_record_and_the_latest_versions() = runTest {
        assertEquals(List(5) { true }, syncAll())

        assertEquals(40, articles.observeArticles().first().size)
        assertEquals(110, transfers.observeTransfers().first().size)
        assertEquals(5, rumours.observeRumours(RumourStatus.ACTIVE).first().size + rumours.observeRumours(RumourStatus.CONFIRMED).first().size + rumours.observeRumours(RumourStatus.FAILED).first().size)
        val versions = preferences.getChangeListVersions()
        assertEquals(network.latestVersion(NetworkRecordType.ARTICLES), versions.articleVersion)
        assertEquals(network.latestVersion(NetworkRecordType.TRANSFERS), versions.transferVersion)
        assertEquals(network.latestVersion(NetworkRecordType.RUMOURS), versions.rumourVersion)
        assertEquals(network.latestVersion(NetworkRecordType.CLUBS), versions.clubVersion)
        assertEquals(network.latestVersion(NetworkRecordType.PLAYERS), versions.playerVersion)
    }

    @Test
    fun records_read_back_as_the_api_sent_them() = runTest {
        syncAll()

        val article = assertNotNull(articles.observeArticle(11276).first())
        assertEquals(ArticleKind.OFFICIAL, article.kind)
        assertEquals(listOf("premier-league", "ligue-1"), article.topics)
        assertEquals(listOf(86, 44, 726), article.clubIds)
        assertEquals(listOf(10281), article.playerIds)
        assertTrue(article in articles.observeArticlesAboutClub(86).first())
        assertTrue(article in articles.observeArticles(topic = "ligue-1").first())

        val transfer = assertNotNull(transfers.observeTransfer(19580).first())
        assertEquals("Łukasz Fabiański", transfer.player.name)
        assertEquals(ClubRef.NoClub(NoClubStatus.FREE_AGENT, "Bez klubu"), transfer.from)
        assertEquals(83, (transfer.to as ClubRef.Club).id)
        assertEquals(LocalDate(2025, 9, 11), transfer.date)
        assertTrue(transfer in transfers.observeTransfers(Season(2025)).first())
        assertFalse(transfer in transfers.observeTransfers(Season(2026)).first())

        val liverpool = assertNotNull(clubs.observeClub(86).first())
        assertEquals("Anfield", liverpool.stadium)
        assertEquals(liverpool, clubs.getClubBySlug("liverpool"))
    }

    @Test
    fun records_saved_before_their_clubs_and_players_use_stubs() = runTest {
        articles.syncWith(synchronizer)
        transfers.syncWith(synchronizer)
        rumours.syncWith(synchronizer)

        // The transfer shows the names copied into it.
        val transfer = assertNotNull(transfers.observeTransfer(19580).first())
        assertEquals("West Ham United", (transfer.to as ClubRef.Club).name)
        assertTrue(assertNotNull(clubs.observeClub(83).first()).isStub)
        assertTrue(assertNotNull(clubs.observeClub(86).first()).isStub)
        assertEquals(emptyList(), clubs.observeClubs(setOf(83, 86)).first())

        clubs.syncWith(synchronizer)
        players.syncWith(synchronizer)

        assertFalse(assertNotNull(clubs.observeClub(83).first()).isStub)
        assertEquals("Liverpool FC", assertNotNull(clubs.observeClub(86).first()).name)
        assertFalse(assertNotNull(players.observePlayer(10281).first()).isStub)
        // The full club data stays when another record refers to the club again.
        network.editRecord(NetworkRecordType.TRANSFERS, 19580)
        transfers.syncWith(synchronizer)
        assertEquals("https://nowinkitransferowe.pl/klub/liverpool", assertNotNull(clubs.observeClub(86).first()).url)
    }

    @Test
    fun references_to_records_that_are_not_saved_are_cleared() = runTest {
        // Transfers before articles: their article ids point to nothing yet.
        assertTrue(transfers.syncWith(synchronizer))
        assertTrue(rumours.syncWith(synchronizer))

        assertTrue(transfers.observeTransfers().first().all { it.articleId == null })
        assertTrue(rumours.observeRumour(5).first()!!.history.all { it.articleId == null })
    }

    @Test
    fun changes_are_read_page_by_page() = runTest {
        network.maxChangesPerPage = 30

        assertTrue(transfers.syncWith(synchronizer))

        assertEquals(110, transfers.observeTransfers().first().size)
        assertEquals(network.latestVersion(NetworkRecordType.TRANSFERS), preferences.getChangeListVersions().transferVersion)
    }

    @Test
    fun records_are_fetched_newest_first_in_batches_of_100() = runTest {
        transfers.syncWith(synchronizer)

        val batches = network.requestedIds.getValue(NetworkRecordType.TRANSFERS)
        assertEquals(listOf(MAX_IDS_PER_REQUEST, 10), batches.map { it.size })
        val ids = batches.flatten()
        assertEquals(ids.sortedDescending(), ids)
    }

    @Test
    fun next_sync_fetches_only_changed_records_and_removes_deleted_ones() = runTest {
        syncAll()
        network.requestedIds.clear()

        network.editRecord(NetworkRecordType.TRANSFERS, 19580)
        network.editRecord(NetworkRecordType.ARTICLES, 11276, deleted = true)
        network.editRecord(NetworkRecordType.CLUBS, 83, deleted = true)
        assertEquals(List(5) { true }, syncAll())

        assertEquals(mapOf(NetworkRecordType.TRANSFERS to mutableListOf(listOf(19580))), network.requestedIds)
        assertNull(articles.observeArticle(11276).first())
        assertNull(clubs.observeClub(83).first())
        // The transfer keeps the club name it was saved with.
        val transfer = assertNotNull(transfers.observeTransfer(19580).first())
        assertEquals("West Ham United", transfer.to.name)
    }

    @Test
    fun interrupted_sync_starts_again_from_the_same_version() = runTest {
        network.failingRecordType = NetworkRecordType.TRANSFERS

        assertFalse(transfers.syncWith(synchronizer))
        assertEquals(0, preferences.getChangeListVersions().transferVersion)

        network.failingRecordType = null
        assertTrue(transfers.syncWith(synchronizer))
        assertEquals(110, transfers.observeTransfers().first().size)
    }

    @Test
    fun nothing_changed_means_no_record_requests() = runTest {
        syncAll()
        network.requestedIds.clear()

        assertEquals(List(5) { true }, syncAll())
        assertEquals(emptyMap(), network.requestedIds)
    }

    @Test
    fun first_sync_posts_no_notifications() = runTest {
        preferences.setNewsNotificationsAllowed(true)
        preferences.setTransfersNotificationsAllowed(true)

        syncAll()

        assertEquals(emptyList(), notifier.addedArticles)
        assertEquals(emptyList(), notifier.addedTransfers)
    }

    @Test
    fun new_records_are_notified_but_edited_ones_are_not() = runTest {
        preferences.setNewsNotificationsAllowed(true)
        preferences.setTransfersNotificationsAllowed(true)
        syncAll()

        // Removed locally, so the next sync gets them as new records.
        db.articleDao().deleteArticles(listOf(11276))
        db.transferDao().deleteTransfers(listOf(19580))
        network.editRecord(NetworkRecordType.ARTICLES, 11276)
        network.editRecord(NetworkRecordType.TRANSFERS, 19580)
        network.editRecord(NetworkRecordType.TRANSFERS, 20741)
        syncAll()

        assertEquals(listOf(listOf(11276)), notifier.addedArticles.map { articles -> articles.map { it.id } })
        assertEquals(listOf(listOf(19580)), notifier.addedTransfers.map { transfers -> transfers.map { it.id } })
    }

    @Test
    fun new_records_are_not_notified_when_notifications_are_off() = runTest {
        syncAll()
        db.articleDao().deleteArticles(listOf(11276))
        network.editRecord(NetworkRecordType.ARTICLES, 11276)

        syncAll()

        assertEquals(emptyList(), notifier.addedArticles)
    }

    @Test
    fun rumour_stats_are_fetched_once_an_hour_and_kept_when_the_fetch_fails() = runTest {
        val stats = OfflineFirstRumourStatsRepository(db.statsDao(), network)
        assertNull(stats.observeRumourStats().first())

        assertTrue(stats.refresh())
        assertTrue(stats.refresh())
        assertEquals(1, network.rumourStatsRequests)

        val saved = assertNotNull(stats.observeRumourStats().first())
        assertEquals(Season(2026), saved.season)
        assertEquals(5, saved.active)
        assertNull(saved.accuracy)

        db.statsDao().upsertFetchTime(pl.nowinkitransferowe.core.database.model.FetchTimeEntity("rumour_stats", kotlinx.datetime.Instant.DISTANT_PAST))
        network.failRumourStats = true
        assertFalse(stats.refresh())
        assertEquals(saved, stats.observeRumourStats().first())
    }

    @Test
    fun club_seasons_are_saved_even_before_the_club_is_synced() = runTest {
        val seasons = OfflineFirstClubSeasonsRepository(db.statsDao(), db.clubDao(), network)

        assertTrue(seasons.refresh(1298))

        val saved = seasons.observeClubSeasons(1298).first()
        assertEquals(listOf(Season(2026)), saved.map { it.season })
        assertEquals(64.5, saved.single().spent)
        assertEquals(listOf(20540), saved.single().arrivals)
    }

    @Test
    fun first_sync_marks_everything_as_read_and_later_records_stay_new() = runTest {
        assertEquals(0, preferences.userData.first().transfersReadUpToId)

        syncAll()

        val newestTransfer = transfers.observeTransfers().first().maxOf { it.id }
        val newestArticle = articles.observeArticles().first().maxOf { it.id }
        val userData = preferences.userData.first()
        assertEquals(newestTransfer, userData.transfersReadUpToId)
        assertEquals(newestArticle, userData.articlesReadUpToId)

        // A later sync keeps the markers, so what it brings counts as new
        network.editRecord(NetworkRecordType.TRANSFERS, 19580)
        syncAll()
        assertEquals(newestTransfer, preferences.userData.first().transfersReadUpToId)
    }

    @Test
    fun marking_all_read_moves_the_marker_and_forgets_the_opened_ids_below_it() = runTest {
        preferences.setTransfersViewed(listOf(5, 50, 500), viewed = true)

        preferences.setTransfersReadUpTo(100)

        val userData = preferences.userData.first()
        assertEquals(100, userData.transfersReadUpToId)
        assertEquals(setOf(500), userData.viewedTransfers)
    }
}
