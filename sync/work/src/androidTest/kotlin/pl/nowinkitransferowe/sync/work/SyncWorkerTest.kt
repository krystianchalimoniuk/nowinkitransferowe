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

package pl.nowinkitransferowe.sync.work

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.sync.work.initializers.SYNC_WORK_NAME
import pl.nowinkitransferowe.sync.work.workers.PeriodicSyncWorker
import pl.nowinkitransferowe.sync.work.workers.SyncWorker
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@HiltAndroidTest
class SyncWorkerTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = InstrumentationRegistry.getInstrumentation().context

    @Inject
    lateinit var dictionaryRepository: DictionaryRepository

    @Inject
    lateinit var articleRepository: ArticleRepository

    @Inject
    lateinit var transferRepository: TransferRepository

    @Inject
    lateinit var clubRepository: ClubRepository

    @Inject
    lateinit var playerRepository: PlayerRepository

    @Before
    fun setup() {
        hiltRule.inject()

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun testSyncWork() {
        // Create request
        val request = SyncWorker.startUpSyncWork()

        val workManager = WorkManager.getInstance(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        // Enqueue and wait for result.
        workManager.enqueue(request).result.get()

        // Get WorkInfo and outputData
        val preRunWorkInfo = workManager.getWorkInfoById(request.id).get()

        // Assert
        assertEquals(WorkInfo.State.ENQUEUED, preRunWorkInfo?.state)

        // Tells the testing framework that the constraints have been met
        testDriver.setAllConstraintsMet(request.id)

        val postRequirementWorkInfo = workManager.getWorkInfoById(request.id).get()
        assertEquals(WorkInfo.State.RUNNING, postRequirementWorkInfo?.state)
    }

    @Test
    fun syncWorkSucceedsAndSavesTheDemoData() = runBlocking {
        val request = SyncWorker.startUpSyncWork()
        val workManager = WorkManager.getInstance(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        workManager.enqueue(request).result.get()
        testDriver.setAllConstraintsMet(request.id)

        awaitUntil { workManager.getWorkInfoById(request.id).get()?.state == WorkInfo.State.SUCCEEDED }
        assertEquals(false, clubRepository.observeClub(86).first()?.isStub)
        assertEquals(false, playerRepository.observePlayer(10281).first()?.isStub)
        assertEquals(12, dictionaryRepository.observeTopics().first().size)
        assertEquals(40, articleRepository.observeArticles().first().size)
        assertEquals(110, transferRepository.observeTransfers().first().size)
    }

    @Test
    fun periodicWorkEnqueuesTheUniqueSync() = runBlocking {
        val workManager = WorkManager.getInstance(context)

        val result = TestListenableWorkerBuilder<PeriodicSyncWorker>(context).build().doWork()

        assertTrue(result is androidx.work.ListenableWorker.Result.Success)
        assertEquals(1, workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get().size)
    }

    private suspend fun awaitUntil(condition: suspend () -> Boolean) {
        repeat(300) {
            if (condition()) return
            delay(100)
        }
        error("The sync did not finish in 30 s")
    }
}
