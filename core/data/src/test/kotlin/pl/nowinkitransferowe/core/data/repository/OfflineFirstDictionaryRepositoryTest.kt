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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.nowinkitransferowe.core.data.testdoubles.TestNtNetworkDataSource
import pl.nowinkitransferowe.core.database.NtDatabase
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.datastoreproto.UserPreferences
import pl.nowinkitransferowe.core.datastoretest.InMemoryDataStore
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.TopicKind
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@RunWith(RobolectricTestRunner::class)
class OfflineFirstDictionaryRepositoryTest {

    private lateinit var db: NtDatabase
    private lateinit var network: TestNtNetworkDataSource
    private lateinit var subject: OfflineFirstDictionaryRepository
    private val synchronizer = TestSynchronizer(NtPreferencesDataSource(InMemoryDataStore(UserPreferences.getDefaultInstance())))

    private var now = Instant.parse("2026-11-11T10:00:00Z")

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NtDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        network = TestNtNetworkDataSource()
        subject = OfflineFirstDictionaryRepository(db.dictionaryDao(), db.statsDao(), network)
        subject.clock = object : Clock {
            override fun now(): Instant = now
        }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun first_sync_saves_topics_and_countries() = runTest {
        assertTrue(subject.syncWith(synchronizer))

        val topics = subject.observeTopics().first()
        assertEquals(12, topics.size)
        assertTrue(Topic("premier-league", "Premier League", TopicKind.LEAGUE) in topics)
        assertTrue(subject.observeCountries().first().any { it.code == "gb-eng" && it.name == "Anglia" })
        assertEquals(2, network.dictionaryRequests)
    }

    @Test
    fun dictionaries_are_downloaded_again_after_a_day() = runTest {
        subject.syncWith(synchronizer)

        advanceClock(23.hours)
        assertTrue(subject.syncWith(synchronizer))
        assertEquals(2, network.dictionaryRequests)

        advanceClock(1.hours)
        assertTrue(subject.syncWith(synchronizer))
        assertEquals(4, network.dictionaryRequests)
    }

    private fun advanceClock(by: Duration) {
        now += by
    }
}
