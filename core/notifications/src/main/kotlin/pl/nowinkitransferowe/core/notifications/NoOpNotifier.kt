/*
 * Copyright 2023 The Android Open Source Project
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

package pl.nowinkitransferowe.core.notifications

import android.graphics.Bitmap
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.GeneralNotificationResource
import pl.nowinkitransferowe.core.model.Transfer
import javax.inject.Inject

/**
 * Implementation of [Notifier] which does nothing. Useful for tests and previews.
 */
internal class NoOpNotifier @Inject constructor() : Notifier {
    override fun postArticleNotifications(articles: List<Article>) = Unit
    override fun postTransferNotifications(transfers: List<Transfer>) = Unit
    override fun postGeneralNotification(generalNotificationResource: GeneralNotificationResource, bitmap: Bitmap?) = Unit
}
