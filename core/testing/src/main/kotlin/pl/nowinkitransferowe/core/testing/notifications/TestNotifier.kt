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
import pl.nowinkitransferowe.core.model.NewsGroup
import pl.nowinkitransferowe.core.notifications.Notifier

/**
 * Keeps the news and general notifications that were posted.
 */
class TestNotifier : Notifier {

    private val mutableNews = mutableListOf<List<NewsGroup>>()
    private val mutableHeldNews = mutableListOf<List<NewsGroup>>()
    private val mutableAddedGeneralNotificationResource =
        mutableListOf<GeneralNotificationResource>()
    val news: List<List<NewsGroup>> = mutableNews
    val heldNews: List<List<NewsGroup>> = mutableHeldNews
    val addedGeneralNotificationResources: List<GeneralNotificationResource> =
        mutableAddedGeneralNotificationResource

    override fun postNews(groups: List<NewsGroup>, images: Map<String, Bitmap>) {
        mutableNews.add(groups)
    }

    override fun postHeldNews(groups: List<NewsGroup>) {
        mutableHeldNews.add(groups)
    }

    override fun postGeneralNotification(
        generalNotificationResource: GeneralNotificationResource,
        bitmap: Bitmap?,
    ) {
        mutableAddedGeneralNotificationResource.add(generalNotificationResource)
    }
}
