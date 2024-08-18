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

package pl.nowinkitransferowe.core.testing.notifications

import android.graphics.Bitmap
import pl.nowinkitransferowe.core.model.GeneralNotificationResource
import pl.nowinkitransferowe.core.model.NewsResource
import pl.nowinkitransferowe.core.model.TransferResource
import pl.nowinkitransferowe.core.notifications.Notifier

/**
 * Aggregates news resources that have been notified for addition
 */
class TestNotifier : Notifier {

    private val mutableAddedNewsResources = mutableListOf<List<NewsResource>>()
    private val mutableAddedTransferResources = mutableListOf<List<TransferResource>>()
    private val mutableAddedGeneralNotificationResource =
        mutableListOf<GeneralNotificationResource>()
    val addedNewsResources: List<List<NewsResource>> = mutableAddedNewsResources
    val addedTransferResources: List<List<TransferResource>> = mutableAddedTransferResources
    val addedGeneralNotificationResources: List<GeneralNotificationResource> =
        mutableAddedGeneralNotificationResource

    override fun postNewsNotifications(newsResources: List<NewsResource>) {
        mutableAddedNewsResources.add(newsResources)
    }

    override fun postTransferNotifications(transferResource: List<TransferResource>) {
        mutableAddedTransferResources.add(transferResource)
    }

    override fun postGeneralNotification(
        generalNotificationResource: GeneralNotificationResource,
        bitmap: Bitmap?,
    ) {
        mutableAddedGeneralNotificationResource.add(generalNotificationResource)
    }
}
