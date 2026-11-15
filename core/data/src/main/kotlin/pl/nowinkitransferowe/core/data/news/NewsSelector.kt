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

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import pl.nowinkitransferowe.core.model.Article
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.FeeKind
import pl.nowinkitransferowe.core.model.NewsChannel
import pl.nowinkitransferowe.core.model.NewsGroup
import pl.nowinkitransferowe.core.model.NewsItem
import pl.nowinkitransferowe.core.model.NewsSubject
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.Transfer
import pl.nowinkitransferowe.core.model.UserData

/** Official transfers from this fee up are important news of a league. */
internal const val LEAGUE_TRANSFER_MILLIONS = 20.0

/** What the clubs and leagues of the news are called and look like. */
internal data class NewsDirectory(
    val clubs: Map<Int, NewsSubject.Club>,
    val players: Map<Int, NewsSubject.Player>,
    val clubTopics: Map<Int, String>,
    val leagueNames: Map<String, String>,
)

/**
 * Picks the news the user wants to hear about and groups it by subject.
 *
 * Followed clubs and players come first: new transfers, rumours that are new or moved up a stage
 * and new articles, each kind only when its switch is on; the system setting of the channel
 * turns all of them off. The rest counts for a followed league
 * only when it is important: articles the editors feature, official transfers from
 * [LEAGUE_TRANSFER_MILLIONS] and rumours that reached an agreement. Items in [notifiedKeys] were
 * already notified and are left out, and so are subjects muted for the day.
 */
internal fun selectNews(
    transfers: List<Transfer>,
    rumours: List<Rumour>,
    articles: List<Article>,
    userData: UserData,
    notifiedKeys: Set<String>,
    directory: NewsDirectory,
    now: Instant,
): List<NewsGroup> {
    val leagues = userData.isFollowedLeaguesNotificationsAllowed && userData.followedTopics.isNotEmpty()

    // A club before a player, so that several news about one club make one notification
    fun followedSubject(clubIds: List<Int>, playerIds: List<Int>): NewsSubject? = clubIds.firstOrNull { it in userData.followedClubs }?.let { directory.clubs[it] }
        ?: playerIds.firstOrNull { it in userData.followedPlayers }?.let { directory.players[it] }

    fun List<ClubRef?>.ids() = filterIsInstance<ClubRef.Club>().map { it.id }

    fun leagueSubject(topics: Collection<String?>): NewsSubject? = topics.filterNotNull()
        .firstOrNull { it in userData.followedTopics }
        ?.let { NewsSubject.League(it, directory.leagueNames[it] ?: it) }

    fun ClubRef.topic(): String? = (this as? ClubRef.Club)?.let { directory.clubTopics[it.id] }

    val picked = mutableListOf<Triple<NewsSubject, NewsChannel, Pair<NewsItem, Instant>>>()
    fun add(item: NewsItem, time: Instant, followedSubject: NewsSubject?, isFollowedKind: Boolean, leagueSubject: () -> NewsSubject?) {
        if (item.key in notifiedKeys) return
        when {
            isFollowedKind && followedSubject != null -> picked += Triple(followedSubject, NewsChannel.FOLLOWED, item to time)
            leagues -> leagueSubject()?.let { picked += Triple(it, NewsChannel.LEAGUES, item to time) }
        }
    }

    transfers.forEach { transfer ->
        add(
            item = NewsItem.TransferNews(transfer),
            time = transfer.date.atStartOfDayIn(TimeZone.UTC),
            followedSubject = followedSubject(listOf(transfer.to, transfer.from).ids(), listOf(transfer.player.id)),
            isFollowedKind = userData.notifyFollowedTransfers,
        ) {
            val fee = transfer.fee
            if (fee.kind == FeeKind.AMOUNT && (fee.millionsOfEuro ?: 0.0) >= LEAGUE_TRANSFER_MILLIONS) {
                leagueSubject(listOf(transfer.to.topic(), transfer.from.topic()))
            } else {
                null
            }
        }
    }
    rumours.forEach { rumour ->
        add(
            item = NewsItem.RumourNews(rumour),
            time = rumour.updatedAt,
            followedSubject = followedSubject(listOf(rumour.to, rumour.from).ids(), listOf(rumour.player.id)),
            isFollowedKind = userData.notifyFollowedRumours,
        ) {
            if (rumour.stage == RumourStage.AGREEMENT) leagueSubject(listOf(rumour.to.topic(), rumour.from?.topic())) else null
        }
    }
    articles.forEach { article ->
        add(
            item = NewsItem.ArticleNews(article),
            time = article.publishedAt,
            followedSubject = followedSubject(article.clubIds, article.playerIds),
            isFollowedKind = userData.notifyFollowedArticles,
        ) {
            if ((article.featuredUntil ?: Instant.DISTANT_PAST) > now) leagueSubject(article.topics) else null
        }
    }

    return picked
        .filter { (subject, _, _) -> (userData.mutedNewsSubjects[subject.key] ?: 0L) <= now.toEpochMilliseconds() }
        .groupBy { it.first to it.second }
        .map { (subjectAndChannel, items) ->
            NewsGroup(
                subject = subjectAndChannel.first,
                channel = subjectAndChannel.second,
                items = items.map { it.third }.sortedByDescending { it.second }.map { it.first },
            )
        }
}
