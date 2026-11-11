/*
 * Copyright 2026 The Android Open Source Project
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

import androidx.datastore.core.DataMigration
import pl.nowinkitransferowe.core.datastoreproto.UserPreferences
import pl.nowinkitransferowe.core.datastoreproto.copy
import javax.inject.Inject

/**
 * Brings preferences saved by older app versions up to [CURRENT_SCHEMA_VERSION].
 *
 * Proto3 fields read as false or 0 when they are missing, so switches that should be on by
 * default are turned on here for users who installed the app before they existed.
 */
class UserPreferencesMigration @Inject constructor() : DataMigration<UserPreferences> {

    override suspend fun shouldMigrate(currentData: UserPreferences): Boolean = currentData.schemaVersion < CURRENT_SCHEMA_VERSION

    override suspend fun migrate(currentData: UserPreferences): UserPreferences = currentData.copy {
        if (schemaVersion < 1) {
            // 4.0: notifications about followed clubs, players and leagues.
            notifyFollowed = true
            notifyFollowedLeagues = true
        }
        schemaVersion = CURRENT_SCHEMA_VERSION
    }

    override suspend fun cleanUp() = Unit

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}
