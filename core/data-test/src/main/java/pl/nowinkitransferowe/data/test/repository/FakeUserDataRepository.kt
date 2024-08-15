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

    override suspend fun setNewsResourceBookmarked(newsResourceId: String, bookmarked: Boolean) {
        ntPreferencesDataSource.setNewsResourceBookmarked(newsResourceId, bookmarked)
    }

    override suspend fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) =
        ntPreferencesDataSource.setNewsResourceViewed(newsResourceId, viewed)

    override suspend fun setTransferResourceBookmarked(
        transferResourceId: String,
        bookmarked: Boolean,
    ) {
        ntPreferencesDataSource.setTransferResourceBookmarked(transferResourceId, bookmarked)
    }

    override suspend fun setTransferResourceViewed(transferResourceId: String, viewed: Boolean) {
        ntPreferencesDataSource.setTransferResourceViewed(transferResourceId, viewed)
    }


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