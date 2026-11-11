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
import pl.nowinkitransferowe.core.database.dao.ArticleDao
import pl.nowinkitransferowe.core.database.dao.ClubDao
import pl.nowinkitransferowe.core.database.dao.DictionaryDao
import pl.nowinkitransferowe.core.database.dao.NewsResourceDao
import pl.nowinkitransferowe.core.database.dao.NewsResourceFtsDao
import pl.nowinkitransferowe.core.database.dao.PlayerDao
import pl.nowinkitransferowe.core.database.dao.RecentSearchQueryDao
import pl.nowinkitransferowe.core.database.dao.RumourDao
import pl.nowinkitransferowe.core.database.dao.SearchDao
import pl.nowinkitransferowe.core.database.dao.StatsDao
import pl.nowinkitransferowe.core.database.dao.TransferDao
import pl.nowinkitransferowe.core.database.dao.TransferResourceDao
import pl.nowinkitransferowe.core.database.dao.TransferResourceFtsDao
import pl.nowinkitransferowe.core.database.model.ArticleClubEntity
import pl.nowinkitransferowe.core.database.model.ArticleEntity
import pl.nowinkitransferowe.core.database.model.ArticleFtsEntity
import pl.nowinkitransferowe.core.database.model.ArticlePlayerEntity
import pl.nowinkitransferowe.core.database.model.ArticleTagEntity
import pl.nowinkitransferowe.core.database.model.ArticleTopicEntity
import pl.nowinkitransferowe.core.database.model.ClubEntity
import pl.nowinkitransferowe.core.database.model.ClubFtsEntity
import pl.nowinkitransferowe.core.database.model.ClubSeasonEntity
import pl.nowinkitransferowe.core.database.model.CountryEntity
import pl.nowinkitransferowe.core.database.model.FetchTimeEntity
import pl.nowinkitransferowe.core.database.model.HotClubEntity
import pl.nowinkitransferowe.core.database.model.LeagueClubEntity
import pl.nowinkitransferowe.core.database.model.LeagueEntity
import pl.nowinkitransferowe.core.database.model.LeagueSeasonEntity
import pl.nowinkitransferowe.core.database.model.NewsResourceEntity
import pl.nowinkitransferowe.core.database.model.NewsResourceFtsEntity
import pl.nowinkitransferowe.core.database.model.PlayerEntity
import pl.nowinkitransferowe.core.database.model.PlayerFtsEntity
import pl.nowinkitransferowe.core.database.model.RecentSearchQueryEntity
import pl.nowinkitransferowe.core.database.model.RumourEntity
import pl.nowinkitransferowe.core.database.model.RumourStageEntity
import pl.nowinkitransferowe.core.database.model.RumourStatsEntity
import pl.nowinkitransferowe.core.database.model.StageAccuracyEntity
import pl.nowinkitransferowe.core.database.model.TopicEntity
import pl.nowinkitransferowe.core.database.model.TransferEntity
import pl.nowinkitransferowe.core.database.model.TransferFtsEntity
import pl.nowinkitransferowe.core.database.model.TransferResourceEntity
import pl.nowinkitransferowe.core.database.model.TransferResourceFtsEntity
import pl.nowinkitransferowe.core.database.util.InstantConverter
import pl.nowinkitransferowe.core.database.util.IntListConverter
import pl.nowinkitransferowe.core.database.util.LocalDateConverter

@Database(
    entities = [
        NewsResourceEntity::class,
        NewsResourceFtsEntity::class,
        TransferResourceEntity::class,
        TransferResourceFtsEntity::class,
        RecentSearchQueryEntity::class,
        // API v3 (the v2 tables above go away once the repositories use v3)
        ArticleEntity::class,
        ArticleTopicEntity::class,
        ArticleClubEntity::class,
        ArticlePlayerEntity::class,
        ArticleTagEntity::class,
        ArticleFtsEntity::class,
        TransferEntity::class,
        TransferFtsEntity::class,
        ClubEntity::class,
        ClubFtsEntity::class,
        PlayerEntity::class,
        PlayerFtsEntity::class,
        RumourEntity::class,
        RumourStageEntity::class,
        TopicEntity::class,
        CountryEntity::class,
        LeagueSeasonEntity::class,
        LeagueEntity::class,
        LeagueClubEntity::class,
        ClubSeasonEntity::class,
        RumourStatsEntity::class,
        StageAccuracyEntity::class,
        HotClubEntity::class,
        FetchTimeEntity::class,
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = DatabaseMigrations.Schema1to2::class),
        AutoMigration(from = 2, to = 3),
    ],
    exportSchema = true,
)
@TypeConverters(
    InstantConverter::class,
    IntListConverter::class,
    LocalDateConverter::class,
)
internal abstract class NtDatabase : RoomDatabase() {
    abstract fun newsResourceDao(): NewsResourceDao
    abstract fun newsResourceFtsDao(): NewsResourceFtsDao
    abstract fun transferResourceDao(): TransferResourceDao
    abstract fun transferResourceFtsDao(): TransferResourceFtsDao
    abstract fun recentSearchQueryDao(): RecentSearchQueryDao
    abstract fun articleDao(): ArticleDao
    abstract fun transferDao(): TransferDao
    abstract fun clubDao(): ClubDao
    abstract fun playerDao(): PlayerDao
    abstract fun rumourDao(): RumourDao
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun statsDao(): StatsDao
    abstract fun searchDao(): SearchDao
}
