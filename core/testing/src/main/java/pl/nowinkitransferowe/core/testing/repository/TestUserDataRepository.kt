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

package pl.nowinkitransferowe.core.testing.repository

import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserData

val emptyUserData = UserData(
    bookmarkedNewsResources = emptySet(),
    bookmarkedTransferResources = emptySet(),
    viewedNewsResources = emptySet(),
    viewedTransferResources = emptySet(),
    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    useDynamicColor = false,
    isNewsNotificationsAllowed = true,
    isTransfersNotificationsAllowed = true,
    isGeneralNotificationAllowed = true,

)

class TestUserDataRepository : UserDataRepository {

    /**
     * The backing hot flow for the list of followed topic ids for testing.
     */
    private val _userData = MutableSharedFlow<UserData>(replay = 1, onBufferOverflow = DROP_OLDEST)

    override val userData: Flow<UserData>
        get() = _userData.filterNotNull()

    private val currentUserData get() = _userData.replayCache.firstOrNull() ?: emptyUserData

    override suspend fun setNewsResourceBookmarked(newsResourceId: String, bookmarked: Boolean) {
        currentUserData.let { current ->
            val bookmarkedNews = if (bookmarked) {
                current.bookmarkedNewsResources + newsResourceId
            } else {
                current.bookmarkedNewsResources - newsResourceId
            }

            _userData.tryEmit(current.copy(bookmarkedNewsResources = bookmarkedNews))
        }
    }

    override suspend fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    viewedNewsResources =
                    if (viewed) {
                        current.viewedNewsResources + newsResourceId
                    } else {
                        current.viewedNewsResources - newsResourceId
                    },
                ),
            )
        }
    }

    override suspend fun setTransferResourceBookmarked(
        transferResourceId: String,
        bookmarked: Boolean,
    ) {
        currentUserData.let { current ->
            val bookmarkedTransfers = if (bookmarked) {
                current.bookmarkedTransferResources + transferResourceId
            } else {
                current.bookmarkedTransferResources - transferResourceId
            }

            _userData.tryEmit(current.copy(bookmarkedTransferResources = bookmarkedTransfers))
        }
    }

    override suspend fun setTransferResourceViewed(transferResourceId: String, viewed: Boolean) {
        currentUserData.let { current ->
            _userData.tryEmit(
                current.copy(
                    viewedTransferResources =
                    if (viewed) {
                        current.viewedTransferResources + transferResourceId
                    } else {
                        current.viewedTransferResources - transferResourceId
                    },
                ),
            )
        }
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(darkThemeConfig = darkThemeConfig))
        }
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(useDynamicColor = useDynamicColor))
        }
    }

    override suspend fun setNewsNotificationsAllowed(isAllowed: Boolean) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(isNewsNotificationsAllowed = isAllowed))
        }
    }

    override suspend fun setTransfersNotificationsAllowed(isAllowed: Boolean) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(isTransfersNotificationsAllowed = isAllowed))
        }
    }

    override suspend fun setGeneralNotificationsAllowed(isAllowed: Boolean) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(isGeneralNotificationAllowed = isAllowed))
        }
    }

    /**
     * A test-only API to allow setting of user data directly.
     */
    fun setUserData(userData: UserData) {
        _userData.tryEmit(userData)
    }
}
