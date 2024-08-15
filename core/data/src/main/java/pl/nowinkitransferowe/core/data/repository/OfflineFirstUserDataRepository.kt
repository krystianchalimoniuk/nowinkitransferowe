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
