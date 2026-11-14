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

package pl.nowinkitransferowe.core.model

data class UserData(
    val bookmarkedArticles: Set<Int>,
    val viewedArticles: Set<Int>,
    val bookmarkedTransfers: Set<Int>,
    val viewedTransfers: Set<Int>,
    val darkThemeConfig: DarkThemeConfig,
    val useDynamicColor: Boolean,
    val isNewsNotificationsAllowed: Boolean,
    val isTransfersNotificationsAllowed: Boolean,
    val isGeneralNotificationAllowed: Boolean,
    // API v3. Defaults keep the 3.x screens compiling until they are replaced.
    val followedTopics: Set<String> = emptySet(),
    val followedClubs: Set<Int> = emptySet(),
    val followedPlayers: Set<Int> = emptySet(),
    val isFollowedNotificationsAllowed: Boolean = false,
    val isFollowedLeaguesNotificationsAllowed: Boolean = false,
    val bookmarkedRumours: Set<Int> = emptySet(),
    /** Articles up to this id count as read; 0 before the first sync, when all of them do. */
    val articlesReadUpToId: Int = 0,
    /** Transfers up to this id count as read; 0 before the first sync, when all of them do. */
    val transfersReadUpToId: Int = 0,
) {
    /** New since the last visit and not opened yet. */
    fun isArticleRead(id: Int): Boolean = id in viewedArticles || articlesReadUpToId == 0 || id <= articlesReadUpToId

    /** New since the last visit and not opened yet. */
    fun isTransferRead(id: Int): Boolean = id in viewedTransfers || transfersReadUpToId == 0 || id <= transfersReadUpToId
}
