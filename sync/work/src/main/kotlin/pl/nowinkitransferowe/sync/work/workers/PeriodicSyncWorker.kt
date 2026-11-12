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

package pl.nowinkitransferowe.sync.work.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import pl.nowinkitransferowe.sync.work.initializers.SYNC_WORK_NAME
import pl.nowinkitransferowe.sync.work.initializers.SyncConstraints
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

/**
 * Starts [SyncWorker] every [INTERVAL], for when no sync push arrives. It enqueues the same
 * unique work as the app start and the push, so a sync that is already running is kept and two
 * syncs never run at once.
 */
internal class PeriodicSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            SyncWorker.startUpSyncWork(),
        )
        return Result.success()
    }

    companion object {
        internal val INTERVAL = 6.hours

        fun periodicSyncWork() = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(INTERVAL.toJavaDuration())
            .setConstraints(SyncConstraints)
            .build()
    }
}
