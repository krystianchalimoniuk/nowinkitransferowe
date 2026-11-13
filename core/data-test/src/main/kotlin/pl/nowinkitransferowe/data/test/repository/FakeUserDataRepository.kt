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

package pl.nowinkitransferowe.data.test.repository

import kotlinx.coroutines.flow.Flow
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserData
import javax.inject.Inject

/**
 * Fake implementation of the [UserDataRepository] that returns hardcoded user data.
 *
 * This allows us to run the app with fake data, without needing an internet connection or working
 * backend.
 */
class FakeUserDataRepository @Inject constructor(
    private val ntPreferencesDataSource: NtPreferencesDataSource,
) : UserDataRepository {

    override val userData: Flow<UserData> =
        ntPreferencesDataSource.userData

    override suspend fun setArticleBookmarked(articleId: Int, bookmarked: Boolean) = ntPreferencesDataSource.setArticleBookmarked(articleId, bookmarked)

    override suspend fun setArticleViewed(articleId: Int, viewed: Boolean) = ntPreferencesDataSource.setArticlesViewed(listOf(articleId), viewed)

    override suspend fun setTransferBookmarked(transferId: Int, bookmarked: Boolean) = ntPreferencesDataSource.setTransferBookmarked(transferId, bookmarked)

    override suspend fun setTransferViewed(transferId: Int, viewed: Boolean) = ntPreferencesDataSource.setTransfersViewed(listOf(transferId), viewed)

    override suspend fun setTransfersViewed(transferIds: List<Int>, viewed: Boolean) = ntPreferencesDataSource.setTransfersViewed(transferIds, viewed)

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        ntPreferencesDataSource.setDarkThemeConfig(darkThemeConfig)
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        ntPreferencesDataSource.setDynamicColorPreference(useDynamicColor)
    }

    override suspend fun setNewsNotificationsAllowed(isAllowed: Boolean) {
        ntPreferencesDataSource.setNewsNotificationsAllowed(isAllowed)
    }

    override suspend fun setTransfersNotificationsAllowed(isAllowed: Boolean) {
        ntPreferencesDataSource.setTransfersNotificationsAllowed(isAllowed)
    }

    override suspend fun setGeneralNotificationsAllowed(isAllowed: Boolean) {
        ntPreferencesDataSource.setTransfersNotificationsAllowed(isAllowed)
    }
}
