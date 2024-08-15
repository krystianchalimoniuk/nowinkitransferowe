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
