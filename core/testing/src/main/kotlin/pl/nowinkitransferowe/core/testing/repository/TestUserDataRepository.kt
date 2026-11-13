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
    bookmarkedArticles = emptySet(),
    bookmarkedTransfers = emptySet(),
    viewedArticles = emptySet(),
    viewedTransfers = emptySet(),
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

    override suspend fun setArticleBookmarked(articleId: Int, bookmarked: Boolean) {
        currentUserData.let { current ->
            val id = articleId
            val bookmarkedArticles = if (bookmarked) current.bookmarkedArticles + id else current.bookmarkedArticles - id
            _userData.tryEmit(current.copy(bookmarkedArticles = bookmarkedArticles))
        }
    }

    override suspend fun setArticleViewed(articleId: Int, viewed: Boolean) {
        currentUserData.let { current ->
            val id = articleId
            val viewedArticles = if (viewed) current.viewedArticles + id else current.viewedArticles - id
            _userData.tryEmit(current.copy(viewedArticles = viewedArticles))
        }
    }

    override suspend fun setTransferBookmarked(transferId: Int, bookmarked: Boolean) {
        currentUserData.let { current ->
            val id = transferId
            val bookmarkedTransfers = if (bookmarked) current.bookmarkedTransfers + id else current.bookmarkedTransfers - id
            _userData.tryEmit(current.copy(bookmarkedTransfers = bookmarkedTransfers))
        }
    }

    override suspend fun setRumourBookmarked(rumourId: Int, bookmarked: Boolean) {
        currentUserData.let { current ->
            val bookmarkedRumours = if (bookmarked) current.bookmarkedRumours + rumourId else current.bookmarkedRumours - rumourId
            _userData.tryEmit(current.copy(bookmarkedRumours = bookmarkedRumours))
        }
    }

    override suspend fun setTransferViewed(transferId: Int, viewed: Boolean) = setTransfersViewed(listOf(transferId), viewed)

    override suspend fun setTransfersViewed(transferIds: List<Int>, viewed: Boolean) {
        currentUserData.let { current ->
            val ids = transferIds.toSet()
            val viewedTransfers = if (viewed) current.viewedTransfers + ids else current.viewedTransfers - ids
            _userData.tryEmit(current.copy(viewedTransfers = viewedTransfers))
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
