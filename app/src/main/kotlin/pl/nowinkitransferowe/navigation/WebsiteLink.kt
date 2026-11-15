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

package pl.nowinkitransferowe.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.Clock
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.feature.articledetails.api.navigation.ArticleNavKey
import pl.nowinkitransferowe.feature.articles.api.navigation.ArticlesNavKey
import pl.nowinkitransferowe.feature.articles.api.navigation.PendingHomeTopic
import pl.nowinkitransferowe.feature.club.api.navigation.ClubNavKey
import pl.nowinkitransferowe.feature.leagues.api.navigation.LeagueNavKey
import pl.nowinkitransferowe.feature.leagues.api.navigation.LeaguesNavKey
import pl.nowinkitransferowe.feature.player.api.navigation.PlayerNavKey
import pl.nowinkitransferowe.feature.rumours.api.navigation.RumoursNavKey
import pl.nowinkitransferowe.feature.search.api.navigation.SearchNavKey
import pl.nowinkitransferowe.feature.transfers.api.navigation.TransfersNavKey
import java.net.URI
import java.net.URLDecoder

/** A page of nowinkitransferowe.pl that the app shows itself. */
sealed interface WebsiteLink {
    /** /{id}/{kategoria}/{slug}: only the id counts. */
    data class Article(val id: Int) : WebsiteLink

    /** /transfery/{slug}: the player's page. */
    data class Player(val slug: String) : WebsiteLink

    data class Club(val slug: String) : WebsiteLink

    data object Transfers : WebsiteLink

    data object Rumours : WebsiteLink

    data object Leagues : WebsiteLink

    data class League(val slug: String) : WebsiteLink

    /** /temat/{slug}: the home screen with the topic picked. */
    data class Topic(val slug: String) : WebsiteLink

    data class Search(val query: String?) : WebsiteLink

    companion object {
        private val HOSTS = setOf("nowinkitransferowe.pl", "www.nowinkitransferowe.pl")

        /** The page at [url], or null for another site or a page the app does not have. */
        fun parse(url: String): WebsiteLink? {
            val uri = runCatching { URI(url) }.getOrNull() ?: return null
            if (uri.scheme !in setOf("http", "https") || uri.host?.lowercase() !in HOSTS) return null
            val path = uri.rawPath.orEmpty().split('/').filter { it.isNotEmpty() }.map { URLDecoder.decode(it, "UTF-8") }
            val first = path.firstOrNull() ?: return null
            val second = path.getOrNull(1)
            return when {
                first.all(Char::isDigit) -> first.toIntOrNull()?.let(::Article)
                first == "transfery" -> if (second != null) Player(second) else Transfers
                first == "klub" && second != null -> Club(second)
                first == "plotki" -> Rumours
                first == "ligi" -> if (second != null) League(second) else Leagues
                first == "temat" && second != null -> Topic(second)
                first == "szukaj" -> Search(query(uri.rawQuery, "q"))
                else -> null
            }
        }

        private fun query(rawQuery: String?, name: String): String? = rawQuery.orEmpty().split('&')
            .map { it.split('=', limit = 2) }
            .firstOrNull { it.first() == name }
            ?.getOrNull(1)
            ?.let { URLDecoder.decode(it, "UTF-8") }
            ?.takeIf { it.isNotBlank() }
    }
}

/**
 * The screens to open for [link], the top level destination first, so that going back shows the
 * section. Clubs and players are looked up by their slug; one not on the phone opens the section.
 */
internal suspend fun WebsiteLink.navKeys(
    clubRepository: ClubRepository,
    playerRepository: PlayerRepository,
    clock: Clock = Clock.System,
): List<NavKey> = when (this) {
    is WebsiteLink.Article -> listOf(ArticlesNavKey, ArticleNavKey(id))
    is WebsiteLink.Player -> listOfNotNull(ArticlesNavKey, playerRepository.getPlayerBySlug(slug)?.let { PlayerNavKey(it.id) })
    is WebsiteLink.Club -> listOfNotNull(ArticlesNavKey, clubRepository.getClubBySlug(slug)?.let { ClubNavKey(it.id) })
    WebsiteLink.Transfers -> listOf(TransfersNavKey)
    WebsiteLink.Rumours -> listOf(RumoursNavKey)
    WebsiteLink.Leagues -> listOf(LeaguesNavKey)
    is WebsiteLink.League -> listOf(LeaguesNavKey, LeagueNavKey(slug, Season.of(clock.now()).startYear))
    is WebsiteLink.Topic -> {
        PendingHomeTopic.request(slug)
        listOf(ArticlesNavKey)
    }
    is WebsiteLink.Search -> listOf(SearchNavKey(query))
}
