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

package pl.nowinkitransferowe.core.model

/**
 * The "Obserwowane" filter of the lists: only records about followed [clubs], [players] and, for
 * articles, [topics].
 */
data class FollowedFilter(
    val clubs: Set<Int>,
    val players: Set<Int>,
    val topics: Set<String> = emptySet(),
) {
    fun isEmpty() = clubs.isEmpty() && players.isEmpty() && topics.isEmpty()

    fun matches(article: Article) = article.clubIds.any { it in clubs } || article.playerIds.any { it in players } || article.topics.any { it in topics }

    fun matches(transfer: Transfer) = matches(listOf(transfer.from, transfer.to), transfer.player.id)

    fun matches(rumour: Rumour) = matches(listOf(rumour.from, rumour.to), rumour.player.id)

    private fun matches(sides: List<ClubRef?>, playerId: Int) = playerId in players || sides.any { it is ClubRef.Club && it.id in clubs }
}

/** The filter of what the user follows. */
val UserData.followedFilter: FollowedFilter get() = FollowedFilter(followedClubs, followedPlayers, followedTopics)
