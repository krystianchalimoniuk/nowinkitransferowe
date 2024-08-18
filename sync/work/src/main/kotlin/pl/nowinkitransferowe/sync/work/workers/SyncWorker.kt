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

package pl.nowinkitransferowe.sync.work.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.tracing.traceAsync
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import pl.nowinkitransferowe.core.analytics.AnalyticsHelper
import pl.nowinkitransferowe.core.common.network.Dispatcher
import pl.nowinkitransferowe.core.common.network.NtDispatchers
import pl.nowinkitransferowe.core.data.Synchronizer
import pl.nowinkitransferowe.core.data.repository.NewsRepository
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.datastore.ChangeListVersions
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.sync.work.initializers.SyncConstraints
import pl.nowinkitransferowe.sync.work.initializers.workForegroundInfo
import pl.nowinkitransferowe.sync.work.status.Subscriber

/**
 * Syncs the data layer by delegating to the appropriate repository instances with
 * sync functionality.
 */
@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ntPreferences: NtPreferencesDataSource,
    private val transferRepository: TransferRepository,
    private val newsRepository: NewsRepository,
    private val searchContentsRepository: SearchContentsRepository,
    @Dispatcher(NtDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val analyticsHelper: AnalyticsHelper,
    private val subscriber: Subscriber,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo =
        appContext.workForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        traceAsync("Sync", 0) {
            analyticsHelper.logSyncStarted()

            subscriber.subscribeToSync()
            subscriber.subscribeToGeneral()

            // First sync the repositories in parallel
            val syncedSuccessfully = awaitAll(
                async { transferRepository.sync() },
                async { newsRepository.sync() },
            ).all { it }

            analyticsHelper.logSyncFinished(syncedSuccessfully)

            if (syncedSuccessfully) {
                searchContentsRepository.populateFtsData()
                Result.success()
            } else {
                Result.retry()
            }
        }
    }

    override suspend fun getChangeListVersions(): ChangeListVersions =
        ntPreferences.getChangeListVersions()

    override suspend fun updateChangeListVersions(
        update: ChangeListVersions.() -> ChangeListVersions,
    ) = ntPreferences.updateChangeListVersion(update)

    companion object {
        /**
         * Expedited one time work to sync data on app startup
         */
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .setInputData(SyncWorker::class.delegatedData(workDataOf()))
            .build()
    }
}
