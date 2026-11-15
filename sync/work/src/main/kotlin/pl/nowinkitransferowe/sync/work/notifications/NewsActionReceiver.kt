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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import pl.nowinkitransferowe.core.common.network.di.ApplicationScope
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.NewsItem
import pl.nowinkitransferowe.core.notifications.ACTION_MUTE_NEWS_SUBJECT
import pl.nowinkitransferowe.core.notifications.ACTION_SAVE_NEWS
import pl.nowinkitransferowe.core.notifications.EXTRA_NOTIFICATION_ID
import pl.nowinkitransferowe.core.notifications.EXTRA_SAVE_ID
import pl.nowinkitransferowe.core.notifications.EXTRA_SAVE_KIND
import pl.nowinkitransferowe.core.notifications.EXTRA_SUBJECT_KEY
import javax.inject.Inject

/**
 * The actions of the news notifications: "Zapisz" saves the transfer, rumour or article to
 * "Zapisane", "Wycisz na dziś" mutes the club, player or league until midnight. Both close the
 * notification.
 */
@AndroidEntryPoint
class NewsActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userDataRepository: UserDataRepository

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        NotificationManagerCompat.from(context).cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))
        val pending = goAsync()
        scope.launch {
            try {
                when (intent.action) {
                    ACTION_SAVE_NEWS -> save(intent.getStringExtra(EXTRA_SAVE_KIND), intent.getIntExtra(EXTRA_SAVE_ID, 0))
                    ACTION_MUTE_NEWS_SUBJECT -> intent.getStringExtra(EXTRA_SUBJECT_KEY)?.let { key ->
                        val timeZone = TimeZone.currentSystemDefault()
                        val tomorrow = Clock.System.todayIn(timeZone).plus(1, DateTimeUnit.DAY)
                        userDataRepository.muteNewsSubject(key, tomorrow.atStartOfDayIn(timeZone))
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun save(kind: String?, id: Int) {
        if (id == 0) return
        when (kind) {
            NewsItem.TRANSFER_KEY -> userDataRepository.setTransferBookmarked(id, true)
            NewsItem.RUMOUR_KEY -> userDataRepository.setRumourBookmarked(id, true)
            NewsItem.ARTICLE_KEY -> userDataRepository.setArticleBookmarked(id, true)
        }
    }
}
