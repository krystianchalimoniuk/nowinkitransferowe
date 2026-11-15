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
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import pl.nowinkitransferowe.core.data.news.NewsNotifications
import java.util.concurrent.TimeUnit

/** Posts the news held during the quiet hours, in the morning. */
@HiltWorker
internal class HeldNewsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val newsNotifications: NewsNotifications,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        newsNotifications.postHeldNews()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "HeldNewsWorkName"

        /** Schedules the summary for the end of the quiet hours; once, however many syncs hold news. */
        fun schedule(context: Context, now: Instant = Clock.System.now(), timeZone: TimeZone = TimeZone.currentSystemDefault()) {
            val delay = NewsNotifications.untilQuietHoursEnd(now, timeZone)
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<DelegatingWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(HeldNewsWorker::class.delegatedData(workDataOf()))
                    .build(),
            )
        }
    }
}
