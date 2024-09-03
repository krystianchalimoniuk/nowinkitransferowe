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
import pl.nowinkitransferowe.core.analytics.AnalyticsHelper
import pl.nowinkitransferowe.core.datastore.NtPreferencesDataSource
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserData
import javax.inject.Inject

internal class OfflineFirstUserDataRepository @Inject constructor(
    private val ntPreferencesDataSource: NtPreferencesDataSource,
    private val analyticsHelper: AnalyticsHelper,
) : UserDataRepository {

    override val userData: Flow<UserData> =
        ntPreferencesDataSource.userData

    override suspend fun setNewsResourceBookmarked(newsResourceId: String, bookmarked: Boolean) {
        ntPreferencesDataSource.setNewsResourceBookmarked(newsResourceId, bookmarked)
        analyticsHelper.logNewsResourceBookmarkToggled(
            newsResourceId = newsResourceId,
            isBookmarked = bookmarked,
        )
    }

    override suspend fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) =
        ntPreferencesDataSource.setNewsResourceViewed(newsResourceId, viewed)

    override suspend fun setTransferResourceBookmarked(
        transferResourceId: String,
        bookmarked: Boolean,
    ) {
        ntPreferencesDataSource.setTransferResourceBookmarked(transferResourceId, bookmarked)
        analyticsHelper.logNewsResourceBookmarkToggled(
            newsResourceId = transferResourceId,
            isBookmarked = bookmarked,
        )
    }

    override suspend fun setTransferResourceViewed(transferResourceId: String, viewed: Boolean) =
        ntPreferencesDataSource.setTransferResourceViewed(transferResourceId, viewed)

    override suspend fun setTransferResourceViewed(
        transferResourceIds: List<String>,
        viewed: Boolean,
    ) {
        ntPreferencesDataSource.setTransferResourcesViewed(transferResourceIds, viewed)
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        ntPreferencesDataSource.setDarkThemeConfig(darkThemeConfig)
        analyticsHelper.logDarkThemeConfigChanged(darkThemeConfig.name)
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        ntPreferencesDataSource.setDynamicColorPreference(useDynamicColor)
        analyticsHelper.logDynamicColorPreferenceChanged(useDynamicColor)
    }

    override suspend fun setNewsNotificationsAllowed(isAllowed: Boolean) {
        ntPreferencesDataSource.setNewsNotificationsAllowed(isAllowed)
        analyticsHelper.logNewsNotificationsPreferenceChanged(isAllowed)
    }

    override suspend fun setTransfersNotificationsAllowed(isAllowed: Boolean) {
        ntPreferencesDataSource.setTransfersNotificationsAllowed(isAllowed)
        analyticsHelper.logTransfersNotificationsPreferenceChanged(isAllowed)
    }

    override suspend fun setGeneralNotificationsAllowed(isAllowed: Boolean) {
        ntPreferencesDataSource.setGeneralNotificationsAllowed(isAllowed)
        analyticsHelper.logGeneralNotificationsPreferenceChanged(isAllowed)
    }
}
