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
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import pl.nowinkitransferowe.core.analytics.AnalyticsHelper
import pl.nowinkitransferowe.core.common.network.Dispatcher
import pl.nowinkitransferowe.core.common.network.NtDispatchers
import pl.nowinkitransferowe.core.data.news.EditorsNotificationLinks
import pl.nowinkitransferowe.core.data.news.EditorsPush
import pl.nowinkitransferowe.core.data.util.ImageDownloader
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.model.GeneralNotificationResource
import pl.nowinkitransferowe.core.notifications.Notifier
import pl.nowinkitransferowe.sync.work.initializers.workForegroundInfo
import kotlin.random.Random

@HiltWorker
internal class PostNotificationWithImageWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    @Dispatcher(NtDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val notifier: Notifier,
    private val analyticsHelper: AnalyticsHelper,
    private val ntPreferencesDataSource: NtPreferencesDataSource,
    private val imageDownloader: ImageDownloader,
    private val editorsNotificationLinks: EditorsNotificationLinks,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.workForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        traceAsync("PostNotificationWithImage", 0) {
            analyticsHelper.logPostNotificationWithImageStarted()
            val isGeneralNotificationAllowed =
                ntPreferencesDataSource.userData.first().isGeneralNotificationAllowed
            if (!isGeneralNotificationAllowed) {
                analyticsHelper.logDisplayingGeneralNotificationIsNotAllowed()
                Result.success()
            } else {
                val id = workerParams.inputData.getInt(KEY_NOTIFICATION_ID, Random.nextInt())
                val title = workerParams.inputData.getString(KEY_NOTIFICATION_TITLE).orEmpty()
                val description =
                    workerParams.inputData.getString(KEY_NOTIFICATION_DESCRIPTION).orEmpty()
                val url = workerParams.inputData.getString(KEY_NOTIFICATION_URL).orEmpty()
                val imageUrl = workerParams.inputData.getString(KEY_NOTIFICATION_IMAGE_URL)
                fun id(key: String) = workerParams.inputData.getInt(key, NO_ID).takeIf { it != NO_ID }
                val push = EditorsPush(url, id(KEY_NOTIFICATION_RUMOUR_ID), id(KEY_NOTIFICATION_TRANSFER_ID), id(KEY_NOTIFICATION_ARTICLE_ID))
                if (title.isNotEmpty() && description.isNotEmpty()) {
                    // The rumour card or the article in the app, when they are or come on the phone
                    val resource = GeneralNotificationResource(id, title, description, url, editorsNotificationLinks.appLink(push))
                    if (imageUrl.isNullOrEmpty()) {
                        notifier.postGeneralNotification(resource)
                    } else {
                        notifier.postGeneralNotification(resource, bitmap = imageDownloader.loadImage(imageUrl))
                    }
                    analyticsHelper.logPostNotificationWithImageFinished()
                    Result.success()
                } else {
                    analyticsHelper.logEmptyNotification()
                    Result.failure()
                }
            }
        }
    }

    companion object {
        const val KEY_NOTIFICATION_ID = "KEY_NOTIFICATION_ID"
        const val KEY_NOTIFICATION_TITLE = "KEY_NOTIFICATION_TITLE"
        const val KEY_NOTIFICATION_DESCRIPTION = "KEY_NOTIFICATION_DESCRIPTION"
        const val KEY_NOTIFICATION_URL = "KEY_NOTIFICATION_URL"
        const val KEY_NOTIFICATION_IMAGE_URL = "KEY_NOTIFICATION_IMAGE_URL"
        const val KEY_NOTIFICATION_RUMOUR_ID = "KEY_NOTIFICATION_RUMOUR_ID"
        const val KEY_NOTIFICATION_TRANSFER_ID = "KEY_NOTIFICATION_TRANSFER_ID"
        const val KEY_NOTIFICATION_ARTICLE_ID = "KEY_NOTIFICATION_ARTICLE_ID"
        private const val NO_ID = -1

        /**
         * Expedited one time work to data on app startup
         */
        fun startUpPostNotificationWithImageWorker(
            id: Int,
            title: String,
            description: String,
            url: String,
            imageUrl: String,
            rumourId: Int? = null,
            transferId: Int? = null,
            articleId: Int? = null,
        ) = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setInputData(
                PostNotificationWithImageWorker::class.delegatedData(
                    workDataOf(
                        KEY_NOTIFICATION_ID to id,
                        KEY_NOTIFICATION_TITLE to title,
                        KEY_NOTIFICATION_DESCRIPTION to description,
                        KEY_NOTIFICATION_URL to url,
                        KEY_NOTIFICATION_IMAGE_URL to imageUrl,
                        KEY_NOTIFICATION_RUMOUR_ID to (rumourId ?: NO_ID),
                        KEY_NOTIFICATION_TRANSFER_ID to (transferId ?: NO_ID),
                        KEY_NOTIFICATION_ARTICLE_ID to (articleId ?: NO_ID),
                    ),
                ),
            )
            .build()
    }
}
