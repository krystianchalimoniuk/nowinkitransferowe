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

package pl.nowinkitransferowe.sync.services

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.core.notifications.Notifier
import pl.nowinkitransferowe.sync.work.initializers.PostNotificationWithImageInitializer
import javax.inject.Inject
import kotlin.random.Random

private const val SYNC_TOPIC_SENDER = "/topics/sync"
private const val GENERAL_TOPICS_SENDER = "/topics/general"

@AndroidEntryPoint
internal class NotificationsService : FirebaseMessagingService() {

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var notifier: Notifier

    override fun onMessageReceived(message: RemoteMessage) {
        if (SYNC_TOPIC_SENDER == message.from) {
            syncManager.requestSync()
        } else if (GENERAL_TOPICS_SENDER == message.from) {
            val id = Random.nextInt()
            val title = message.data["title"] ?: ""
            val description = message.data["description"] ?: ""
            val imageUrl = message.data["image_url"] ?: ""
            val url = message.data["url"] ?: ""
            if (title.isNotEmpty() && description.isNotEmpty()) {
                PostNotificationWithImageInitializer.initialize(
                    context,
                    id,
                    title,
                    description,
                    url,
                    imageUrl,
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
