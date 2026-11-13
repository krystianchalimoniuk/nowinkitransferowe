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
                isGeneralNotificationAllowed = it.notifyGeneral,
                followedTopics = it.followedTopicIdsMap.keys,
                followedClubs = it.followedClubIdsMap.keys,
                followedPlayers = it.followedPlayerIdsMap.keys,
                isFollowedNotificationsAllowed = it.notifyFollowed,
                isFollowedLeaguesNotificationsAllowed = it.notifyFollowedLeagues,
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

    suspend fun setFollowedNotificationsAllowed(isAllowed: Boolean) {
        userPreferences.updateData {
            it.copy { this.notifyFollowed = isAllowed }
        }
    }

    suspend fun setFollowedLeaguesNotificationsAllowed(isAllowed: Boolean) {
        userPreferences.updateData {
            it.copy { this.notifyFollowedLeagues = isAllowed }
        }
    }

    suspend fun setTopicFollowed(topicSlug: String, followed: Boolean) {
        userPreferences.updateData {
            it.copy {
                if (followed) {
                    followedTopicIds.put(topicSlug, true)
                } else {
                    followedTopicIds.remove(topicSlug)
                }
            }
        }
    }

    suspend fun setClubFollowed(clubId: Int, followed: Boolean) {
        userPreferences.updateData {
            it.copy {
                if (followed) {
                    followedClubIds.put(clubId, true)
                } else {
                    followedClubIds.remove(clubId)
                }
            }
        }
    }

    suspend fun setPlayerFollowed(playerId: Int, followed: Boolean) {
        userPreferences.updateData {
            it.copy {
                if (followed) {
                    followedPlayerIds.put(playerId, true)
                } else {
                    followedPlayerIds.remove(playerId)
                }
            }
        }
    }

    /**
     * Keys of the records the user was already notified about.
     */
    suspend fun getNotifiedKeys(): Set<String> = userPreferences.data.map { it.notifiedKeysList.toSet() }.firstOrNull().orEmpty()

    /**
     * Remembers that the user was notified about [keys]. Only the newest [MAX_NOTIFIED_KEYS] keys
     * are kept, which is far more than a single sync can notify about.
     */
    suspend fun addNotifiedKeys(keys: Collection<String>) {
        if (keys.isEmpty()) return
        userPreferences.updateData {
            it.copy {
                val newest = (notifiedKeys - keys.toSet() + keys.distinct()).takeLast(MAX_NOTIFIED_KEYS)
                notifiedKeys.clear()
                notifiedKeys.addAll(newest)
            }
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
                articleVersion = it.articleChangeListVersion,
                transferVersion = it.transferChangeListVersion,
                rumourVersion = it.rumourChangeListVersion,
                clubVersion = it.clubChangeListVersion,
                playerVersion = it.playerChangeListVersion,
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
                        articleVersion = currentPreferences.articleChangeListVersion,
                        transferVersion = currentPreferences.transferChangeListVersion,
                        rumourVersion = currentPreferences.rumourChangeListVersion,
                        clubVersion = currentPreferences.clubChangeListVersion,
                        playerVersion = currentPreferences.playerChangeListVersion,
                    ),
                )

                currentPreferences.copy {
                    articleChangeListVersion = updatedChangeListVersions.articleVersion
                    transferChangeListVersion = updatedChangeListVersions.transferVersion
                    rumourChangeListVersion = updatedChangeListVersions.rumourVersion
                    clubChangeListVersion = updatedChangeListVersions.clubVersion
                    playerChangeListVersion = updatedChangeListVersions.playerVersion
                }
            }
        } catch (ioException: IOException) {
            Log.e("NtPreferences", "Failed to update user preferences", ioException)
        }
    }

    companion object {
        const val MAX_NOTIFIED_KEYS = 500
    }
}
