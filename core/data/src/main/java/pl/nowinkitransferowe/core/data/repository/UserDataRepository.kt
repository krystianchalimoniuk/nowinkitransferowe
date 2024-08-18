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
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserData

interface UserDataRepository {

    /**
     * Stream of [UserData]
     */
    val userData: Flow<UserData>

    /**
     * Updates the bookmarked status for a news resource
     */
    suspend fun setNewsResourceBookmarked(newsResourceId: String, bookmarked: Boolean)

    /**
     * Updates the viewed status for a news resource
     */
    suspend fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean)

    /**
     * Updates the bookmarked status for a transfer resource
     */
    suspend fun setTransferResourceBookmarked(transferResourceId: String, bookmarked: Boolean)

    /**
     * Updates the viewed status for a transfer resource
     */
    suspend fun setTransferResourceViewed(transferResourceId: String, viewed: Boolean)

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
