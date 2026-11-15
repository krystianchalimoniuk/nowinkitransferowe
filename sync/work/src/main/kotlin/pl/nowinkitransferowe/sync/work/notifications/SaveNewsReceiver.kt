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

package pl.nowinkitransferowe.sync.work.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.common.network.di.ApplicationScope
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.NewsItem
import pl.nowinkitransferowe.core.notifications.EXTRA_NOTIFICATION_ID
import pl.nowinkitransferowe.core.notifications.EXTRA_SAVE_ID
import pl.nowinkitransferowe.core.notifications.EXTRA_SAVE_KIND
import javax.inject.Inject

/** Saves the transfer, rumour or article of a notification to "Zapisane" and closes it. */
@AndroidEntryPoint
class SaveNewsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userDataRepository: UserDataRepository

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_SAVE_ID, 0).takeIf { it != 0 } ?: return
        val kind = intent.getStringExtra(EXTRA_SAVE_KIND)
        NotificationManagerCompat.from(context).cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))
        val pending = goAsync()
        scope.launch {
            try {
                when (kind) {
                    NewsItem.TRANSFER_KEY -> userDataRepository.setTransferBookmarked(id, true)
                    NewsItem.RUMOUR_KEY -> userDataRepository.setRumourBookmarked(id, true)
                    NewsItem.ARTICLE_KEY -> userDataRepository.setArticleBookmarked(id, true)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
