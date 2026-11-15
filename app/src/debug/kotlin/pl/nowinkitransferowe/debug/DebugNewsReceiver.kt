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

package pl.nowinkitransferowe.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import pl.nowinkitransferowe.core.common.network.di.ApplicationScope
import pl.nowinkitransferowe.core.data.news.NewsNotifications
import pl.nowinkitransferowe.core.data.news.SyncNews
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.model.RumourStatus
import javax.inject.Inject

/**
 * Debug builds only: shows the news notifications with the records already on the phone, as if a
 * sync had just brought them. Each extra is a comma separated list of club ids; the clubs become
 * followed and give their newest transfer, active rumour or article:
 *
 * adb shell am broadcast -n pl.nowinkitransferowe.debug/pl.nowinkitransferowe.debug.DebugNewsReceiver \
 *   --es transfers 4 --es rumours 18 --es articles 83 --es all 31
 *
 * "all" takes one of each, and articles turn on the article notifications. The notified keys are
 * cleared first, so the same news can come again.
 */
@AndroidEntryPoint
class DebugNewsReceiver : BroadcastReceiver() {

    @Inject lateinit var userDataRepository: UserDataRepository

    @Inject lateinit var preferences: NtPreferencesDataSource

    @Inject lateinit var transferRepository: TransferRepository

    @Inject lateinit var rumourRepository: RumourRepository

    @Inject lateinit var articleRepository: ArticleRepository

    @Inject lateinit var newsNotifications: NewsNotifications

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        fun clubs(extra: String) = intent.getStringExtra(extra).orEmpty().split(',').mapNotNull { it.trim().toIntOrNull() }
        val all = clubs("all")
        val transferClubs = clubs("transfers") + all
        val rumourClubs = clubs("rumours") + all
        val articleClubs = clubs("articles") + all
        val pending = goAsync()
        scope.launch {
            try {
                (transferClubs + rumourClubs + articleClubs).distinct().forEach { userDataRepository.setClubFollowed(it, true) }
                preferences.clearNotifiedKeys()
                if (articleClubs.isNotEmpty()) userDataRepository.setFollowedArticlesNotified(true)
                val transfers = transferClubs.mapNotNull { club ->
                    transferRepository.observeTransfersOfClub(club).first().maxByOrNull { it.date }?.id
                }
                val rumours = rumourClubs.mapNotNull { club ->
                    rumourRepository.observeRumoursOfClub(club).first().firstOrNull { it.status == RumourStatus.ACTIVE }
                }
                val articles = articleClubs.mapNotNull { club ->
                    articleRepository.observeArticlesAboutClub(club).first().maxByOrNull { it.publishedAt }?.id
                }
                // At noon, so that the quiet hours do not hold the news
                val timeZone = TimeZone.currentSystemDefault()
                val noon = LocalDateTime(Clock.System.todayIn(timeZone), LocalTime(12, 0)).toInstant(timeZone)
                newsNotifications.notify(
                    SyncNews(
                        articleIds = articles.toSet(),
                        transferIds = transfers.toSet(),
                        rumourStages = rumours.associate { it.id to it.stage },
                    ),
                    now = noon,
                )
            } finally {
                pending.finish()
            }
        }
    }
}
