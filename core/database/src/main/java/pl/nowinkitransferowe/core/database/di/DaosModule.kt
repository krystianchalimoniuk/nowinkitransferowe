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

package pl.nowinkitransferowe.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.nowinkitransferowe.core.database.NtDatabase
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

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {

    @Provides
    fun providesNewsResourceDao(
        database: NtDatabase,
    ): NewsResourceDao = database.newsResourceDao()

    @Provides
    fun providesNewsResourceFtsDao(
        database: NtDatabase,
    ): NewsResourceFtsDao = database.newsResourceFtsDao()

    @Provides
    fun providesTransfersResourceDao(
        database: NtDatabase,
    ): TransferResourceDao = database.transferResourceDao()

    @Provides
    fun providesTransfersResourceFtsDao(
        database: NtDatabase,
    ): TransferResourceFtsDao = database.transferResourceFtsDao()

    @Provides
    fun providesRecentSearchQueryDao(
        database: NtDatabase,
    ): RecentSearchQueryDao = database.recentSearchQueryDao()

    @Provides
    fun providesArticleDao(
        database: NtDatabase,
    ): ArticleDao = database.articleDao()

    @Provides
    fun providesTransferDao(
        database: NtDatabase,
    ): TransferDao = database.transferDao()

    @Provides
    fun providesClubDao(
        database: NtDatabase,
    ): ClubDao = database.clubDao()

    @Provides
    fun providesPlayerDao(
        database: NtDatabase,
    ): PlayerDao = database.playerDao()

    @Provides
    fun providesRumourDao(
        database: NtDatabase,
    ): RumourDao = database.rumourDao()

    @Provides
    fun providesDictionaryDao(
        database: NtDatabase,
    ): DictionaryDao = database.dictionaryDao()

    @Provides
    fun providesStatsDao(
        database: NtDatabase,
    ): StatsDao = database.statsDao()

    @Provides
    fun providesSearchDao(
        database: NtDatabase,
    ): SearchDao = database.searchDao()
}
