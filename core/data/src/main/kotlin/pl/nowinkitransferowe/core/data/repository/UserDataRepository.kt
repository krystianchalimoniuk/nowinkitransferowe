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

package pl.nowinkitransferowe.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserData

interface UserDataRepository {

    /**
     * Stream of [UserData]
     */
    val userData: Flow<UserData>

    suspend fun setArticleBookmarked(articleId: Int, bookmarked: Boolean)

    suspend fun setArticleViewed(articleId: Int, viewed: Boolean)

    suspend fun setTransferBookmarked(transferId: Int, bookmarked: Boolean)

    suspend fun setRumourBookmarked(rumourId: Int, bookmarked: Boolean)

    suspend fun setClubFollowed(clubId: Int, followed: Boolean)

    suspend fun setPlayerFollowed(playerId: Int, followed: Boolean)

    /** Follows the league with the topic [topicSlug] for its important news. */
    suspend fun setTopicFollowed(topicSlug: String, followed: Boolean)

    /** Turns the notifications about followed clubs and players on or off. */
    suspend fun setFollowedNotificationsAllowed(isAllowed: Boolean)

    /** Turns the notifications about important news of followed leagues on or off. */
    suspend fun setFollowedLeaguesNotificationsAllowed(isAllowed: Boolean)

    suspend fun setFollowedTransfersNotified(isNotified: Boolean)

    suspend fun setFollowedRumoursNotified(isNotified: Boolean)

    suspend fun setFollowedArticlesNotified(isNotified: Boolean)

    suspend fun setQuietHours(isEnabled: Boolean)

    /**
     * Follows [clubIds] picked on the "Wybierz swoje kluby" screen and marks the screen as shown,
     * so that it does not come again; an empty list is a skip.
     */
    suspend fun finishClubsOnboarding(clubIds: Set<Int>)

    /** Mutes the notifications about the news subject [subjectKey] until [until]. */
    suspend fun muteNewsSubject(subjectKey: String, until: Instant)

    /** Marks every transfer up to [id] as read. */
    suspend fun setTransfersReadUpTo(id: Int)

    /** Marks every article up to [id] as read. */
    suspend fun setArticlesReadUpTo(id: Int)

    suspend fun setTransferViewed(transferId: Int, viewed: Boolean)

    suspend fun setTransfersViewed(transferIds: List<Int>, viewed: Boolean)

    /**
     * Sets the desired dark theme config.
     */
    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    /**
     * Sets the preferred dynamic color config.
     */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    /**
     * Sets the news notifications allowed config.
     */
    suspend fun setNewsNotificationsAllowed(isAllowed: Boolean)

    /**
     * Sets the transfers notifications allowed config.
     */
    suspend fun setTransfersNotificationsAllowed(isAllowed: Boolean)

    /**
     * Sets the general notifications allowed config.
     */
    suspend fun setGeneralNotificationsAllowed(isAllowed: Boolean)
}
