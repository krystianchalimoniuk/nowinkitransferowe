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
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.GeneralNotificationResource
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.notifications.Notifier

/**
 * Aggregates articles and transfers that have been notified for addition
 */
class TestNotifier : Notifier {

    private val mutableAddedArticles = mutableListOf<List<Article>>()
    private val mutableAddedTransfers = mutableListOf<List<Transfer>>()
    private val mutableAddedGeneralNotificationResource =
        mutableListOf<GeneralNotificationResource>()
    val addedArticles: List<List<Article>> = mutableAddedArticles
    val addedTransfers: List<List<Transfer>> = mutableAddedTransfers
    val addedGeneralNotificationResources: List<GeneralNotificationResource> =
        mutableAddedGeneralNotificationResource

    override fun postArticleNotifications(articles: List<Article>) {
        mutableAddedArticles.add(articles)
    }

    override fun postTransferNotifications(transfers: List<Transfer>) {
        mutableAddedTransfers.add(transfers)
    }

    override fun postGeneralNotification(
        generalNotificationResource: GeneralNotificationResource,
        bitmap: Bitmap?,
    ) {
        mutableAddedGeneralNotificationResource.add(generalNotificationResource)
    }
}
