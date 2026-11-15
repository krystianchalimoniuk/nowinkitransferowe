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

package pl.nowinkitransferowe.core.data.news

import android.graphics.Bitmap
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.data.util.ImageDownloader
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.NewsGroup
import pl.nowinkitransferowe.core.model.NewsItem
import pl.nowinkitransferowe.core.model.NewsSubject
import pl.nowinkitransferowe.core.model.crestUrl
import pl.nowinkitransferowe.core.notifications.Notifier
import javax.inject.Inject

/**
 * Turns the news of a sync into notifications about followed clubs, players and leagues.
 *
 * During the quiet hours the news waits in the preferences and comes in one summary at
 * [QUIET_HOURS_END]; the editors' push notifications are not held.
 */
class NewsNotifications @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val transferRepository: TransferRepository,
    private val rumourRepository: RumourRepository,
    private val clubRepository: ClubRepository,
    private val playerRepository: PlayerRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val preferences: NtPreferencesDataSource,
    private val notifier: Notifier,
    private val imageDownloader: ImageDownloader,
) {

    /**
     * Notifies about [news], or holds it when it comes in the quiet hours. Returns true when news
     * is held, so that the caller schedules [postHeldNews] for the morning.
     */
    suspend fun notify(
        news: SyncNews,
        now: Instant = Clock.System.now(),
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): Boolean {
        if (news.isEmpty()) return false
        val groups = select(
            transferIds = news.transferIds,
            rumourIds = news.rumourStages.keys,
            articleIds = news.articleIds,
            now = now,
        ) { rumour -> news.rumourStages[rumour.id] == rumour.stage }
        if (groups.isEmpty()) return false
        val keys = groups.flatMap { group -> group.items.map { it.key } }
        if (preferences.userData.first().isQuietHoursEnabled && isQuietHour(now, timeZone)) {
            preferences.addPendingNotificationKeys(keys)
            return true
        }
        notifier.postNews(groups, loadImages(groups))
        preferences.addNotifiedKeys(keys)
        return false
    }

    /** Posts the news held during the quiet hours in one summary. */
    suspend fun postHeldNews(now: Instant = Clock.System.now()) {
        val held = preferences.getPendingNotificationKeys()
        if (held.isEmpty()) return
        val ids = held.groupBy({ it.substringBefore(':') }, { it.split(':')[1].toInt() })
        // A rumour is still news when it did not move down since
        val heldRumourKeys = held.filter { it.startsWith(NewsItem.RUMOUR_KEY) }.toSet()
        val groups = select(
            transferIds = ids[NewsItem.TRANSFER_KEY].orEmpty().toSet(),
            rumourIds = ids[NewsItem.RUMOUR_KEY].orEmpty().toSet(),
            articleIds = ids[NewsItem.ARTICLE_KEY].orEmpty().toSet(),
            now = now,
        ) { rumour -> NewsItem.RumourNews(rumour).key in heldRumourKeys }
        notifier.postHeldNews(groups)
        preferences.addNotifiedKeys(groups.flatMap { group -> group.items.map { it.key } })
        preferences.clearPendingNotificationKeys()
    }

    private suspend fun select(
        transferIds: Set<Int>,
        rumourIds: Set<Int>,
        articleIds: Set<Int>,
        now: Instant,
        isRumourNews: (pl.nowinkitransferowe.core.model.Rumour) -> Boolean,
    ): List<NewsGroup> {
        val userData = preferences.userData.first()
        val transfers = if (transferIds.isEmpty()) emptyList() else transferRepository.observeTransfers(transferIds).first()
        val rumours = if (rumourIds.isEmpty()) emptyList() else rumourRepository.observeRumours(rumourIds).first().filter(isRumourNews)
        val articles = if (articleIds.isEmpty()) emptyList() else articleRepository.observeArticles(articleIds).first()

        val clubIds = (transfers.flatMap { listOf(it.from, it.to) } + rumours.flatMap { listOfNotNull(it.from, it.to) })
            .filterIsInstance<ClubRef.Club>()
            .mapTo(mutableSetOf()) { it.id } + articles.flatMap { it.clubIds } + userData.followedClubs
        val clubs = clubRepository.observeClubs(clubIds).first()
        val directory = NewsDirectory(
            clubs = clubs.associate { it.id to NewsSubject.Club(it.id, it.name, it.crestUrl) },
            players = playerRepository.observePlayers(userData.followedPlayers).first()
                .associate { it.id to NewsSubject.Player(it.id, it.name, it.photoUrl) },
            clubTopics = clubs.mapNotNull { club -> club.topic?.let { club.id to it } }.toMap(),
            leagueNames = dictionaryRepository.observeTopics().first().associate { it.slug to it.name },
        )
        return selectNews(transfers, rumours, articles, userData, preferences.getNotifiedKeys(), directory, now)
    }

    /** The crests and photos the notifications show, by url. */
    private suspend fun loadImages(groups: List<NewsGroup>): Map<String, Bitmap> = groups
        .flatMap { group ->
            val subject = when (val subject = group.subject) {
                is NewsSubject.Club -> subject.crestUrl
                is NewsSubject.Player -> subject.photoUrl
                is NewsSubject.League -> null
            }
            listOf(subject) + group.items.flatMap { item ->
                when (item) {
                    is NewsItem.TransferNews -> listOf(item.transfer.player.photoUrl, item.transfer.from.crestUrl, item.transfer.to.crestUrl)
                    is NewsItem.RumourNews -> listOf(item.rumour.player.photoUrl)
                    is NewsItem.ArticleNews -> listOf(item.article.thumbnailUrl ?: item.article.imageUrl)
                }
            }
        }
        .filterNotNull()
        .distinct()
        .take(MAX_IMAGES)
        .mapNotNull { url -> imageDownloader.loadImage(url)?.let { url to it } }
        .toMap()

    companion object {
        /** The quiet hours: from 23:00 until 7:00. */
        const val QUIET_HOURS_START = 23
        const val QUIET_HOURS_END = 7
        private const val MAX_IMAGES = 12

        /** Milliseconds from [now] until the next [QUIET_HOURS_END] o'clock. */
        fun untilQuietHoursEnd(now: Instant, timeZone: TimeZone): Long {
            val local = now.toLocalDateTime(timeZone)
            val todayEnd = LocalDateTime(local.date, LocalTime(QUIET_HOURS_END, 0))
            val end = if (local < todayEnd) todayEnd else LocalDateTime(local.date.plus(1, DateTimeUnit.DAY), todayEnd.time)
            return (end.toInstant(timeZone) - now).inWholeMilliseconds
        }

        fun isQuietHour(now: Instant, timeZone: TimeZone): Boolean {
            val hour = now.toLocalDateTime(timeZone).hour
            return hour >= QUIET_HOURS_START || hour < QUIET_HOURS_END
        }
    }
}
