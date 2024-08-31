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

package pl.nowinkitransferowe.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.nowinkitransferowe.core.database.dao.NewsResourceDao
import pl.nowinkitransferowe.core.database.dao.NewsResourceFtsDao
import pl.nowinkitransferowe.core.database.dao.RecentSearchQueryDao
import pl.nowinkitransferowe.core.database.dao.TransferResourceDao
import pl.nowinkitransferowe.core.database.dao.TransferResourceFtsDao
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.database.model.NewsResourceFtsEntity
import pl.nowinkitransferowe.core.database.model.RecentSearchQueryEntity
import pl.nowinkitransferowe.core.database.model.TransferResourceEntity
import pl.nowinkitransferowe.core.database.model.TransferResourceFtsEntity
import pl.nowinkitransferowe.core.database.util.InstantConverter

@Database(
    entities = [
        NewsResourceEntity::class,
        NewsResourceFtsEntity::class,
        TransferResourceEntity::class,
        TransferResourceFtsEntity::class,
        RecentSearchQueryEntity::class,
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = DatabaseMigrations.Schema1to2::class),
    ],
    exportSchema = true,
)
@TypeConverters(
    InstantConverter::class,
)
internal abstract class NtDatabase : RoomDatabase() {
    abstract fun newsResourceDao(): NewsResourceDao
    abstract fun newsResourceFtsDao(): NewsResourceFtsDao
    abstract fun transferResourceDao(): TransferResourceDao
    abstract fun transferResourceFtsDao(): TransferResourceFtsDao
    abstract fun recentSearchQueryDao(): RecentSearchQueryDao
}
