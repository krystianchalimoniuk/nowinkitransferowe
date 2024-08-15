package pl.nowinkitransferowe.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import pl.nowinkitransferowe.core.datastoreproto.DarkThemeConfigProto
import pl.nowinkitransferowe.core.datastoreproto.UserPreferences
import pl.nowinkitransferowe.core.datastoreproto.copy
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserData
import java.io.IOException
import javax.inject.Inject

class NtPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val userData = userPreferences.data
        .map {
            UserData(
                bookmarkedNewsResources = it.bookmarkedNewsResourceIdsMap.keys,
                viewedNewsResources = it.viewedNewsResourceIdsMap.keys,
                bookmarkedTransferResources = it.bookmarkedTransferResourceIdsMap.keys,
                viewedTransferResources = it.viewedTransferResourceIdsMap.keys,
                darkThemeConfig = when (it.darkThemeConfig) {
                    null,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED,
                    DarkThemeConfigProto.UNRECOGNIZED,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM,
                    ->
                        DarkThemeConfig.FOLLOW_SYSTEM

                    DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT ->
                        DarkThemeConfig.LIGHT

                    DarkThemeConfigProto.DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
                },
                useDynamicColor = it.useDynamicColor,
                isNewsNotificationsAllowed = it.notifyNews,
                isTransfersNotificationsAllowed = it.notifyTransfers,
                isGeneralNotificationAllowed = it.notifyGeneral
            )
        }


    suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferences.updateData {
            it.copy { this.useDynamicColor = useDynamicColor }
        }
    }

    suspend fun setNewsNotificationsAllowed(isAllowed: Boolean) {
        userPreferences.updateData {
            it.copy { this.notifyNews = isAllowed }
        }
    }

    suspend fun setTransfersNotificationsAllowed(isAllowed: Boolean) {
        userPreferences.updateData {
            it.copy { this.notifyTransfers = isAllowed }
        }
    }

    suspend fun setGeneralNotificationsAllowed(isAllowed: Boolean) {
        userPreferences.updateData {
            it.copy { this.notifyGeneral = isAllowed }
        }
    }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferences.updateData {
            it.copy {
                this.darkThemeConfig = when (darkThemeConfig) {
                    DarkThemeConfig.FOLLOW_SYSTEM ->
                        DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM

                    DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                    DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                }
            }
        }
    }

    suspend fun setNewsResourceBookmarked(newsResourceId: String, bookmarked: Boolean) {
        try {
            userPreferences.updateData {
                it.copy {
                    if (bookmarked) {
                        bookmarkedNewsResourceIds.put(newsResourceId, true)
                    } else {
                        bookmarkedNewsResourceIds.remove(newsResourceId)
                    }
                }
            }
        } catch (ioException: IOException) {
            Log.e("NtPreferences", "Failed to update user preferences", ioException)
        }
    }

    suspend fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        setNewsResourcesViewed(listOf(newsResourceId), viewed)
    }

    suspend fun setNewsResourcesViewed(newsResourceIds: List<String>, viewed: Boolean) {
        userPreferences.updateData { prefs ->
            prefs.copy {
                newsResourceIds.forEach { id ->
                    if (viewed) {
                        viewedNewsResourceIds.put(id, true)
                    } else {
                        viewedNewsResourceIds.remove(id)
                    }
                }
            }
        }
    }

    suspend fun setTransferResourceBookmarked(transferResourceId: String, bookmarked: Boolean) {
        try {
            userPreferences.updateData {
                it.copy {
                    if (bookmarked) {
                        bookmarkedTransferResourceIds.put(transferResourceId, true)
                    } else {
                        bookmarkedTransferResourceIds.remove(transferResourceId)
                    }
                }
            }
        } catch (ioException: IOException) {
            Log.e("NtPreferences", "Failed to update user preferences", ioException)
        }
    }

    suspend fun setTransferResourceViewed(transferResourceId: String, viewed: Boolean) {
        setTransferResourcesViewed(listOf(transferResourceId), viewed)
    }

    suspend fun setTransferResourcesViewed(transferResourceIds: List<String>, viewed: Boolean) {
        userPreferences.updateData { prefs ->
            prefs.copy {
                transferResourceIds.forEach { id ->
                    if (viewed) {
                        viewedTransferResourceIds.put(id, true)
                    } else {
                        viewedTransferResourceIds.remove(id)
                    }
                }
            }
        }
    }


    suspend fun getChangeListVersions() = userPreferences.data
        .map {
            ChangeListVersions(
                transferResourceVersion = it.transferResourcesChangeListVersion,
                newsResourceVersion = it.newsResourceChangeListVersion,
            )
        }
        .firstOrNull() ?: ChangeListVersions()

    /**
     * Update the [ChangeListVersions] using [update].
     */
    suspend fun updateChangeListVersion(update: ChangeListVersions.() -> ChangeListVersions) {
        try {
            userPreferences.updateData { currentPreferences ->
                val updatedChangeListVersions = update(
                    ChangeListVersions(
                        transferResourceVersion = currentPreferences.transferResourcesChangeListVersion,
                        newsResourceVersion = currentPreferences.newsResourceChangeListVersion,
                    ),
                )

                currentPreferences.copy {
                    transferResourcesChangeListVersion =
                        updatedChangeListVersions.transferResourceVersion
                    newsResourceChangeListVersion = updatedChangeListVersions.newsResourceVersion
                }
            }
        } catch (ioException: IOException) {
            Log.e("NtPreferences", "Failed to update user preferences", ioException)
        }
    }

}

