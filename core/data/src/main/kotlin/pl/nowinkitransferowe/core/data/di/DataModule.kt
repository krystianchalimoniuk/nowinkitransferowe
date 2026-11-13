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

package pl.nowinkitransferowe.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.CompositeUserArticleRepository
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.data.repository.DefaultRecentSearchRepository
import pl.nowinkitransferowe.core.data.repository.DefaultSearchContentsRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstArticleRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstClubRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstDictionaryRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstPlayerRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstRumourRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstTransferRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstUserDataRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RecentSearchRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.data.repository.UserArticleRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.data.util.ConnectivityManagerNetworkMonitor
import pl.nowinkitransferowe.core.data.util.DefaultImageDownLoader
import pl.nowinkitransferowe.core.data.util.ImageDownloader
import pl.nowinkitransferowe.core.data.util.NetworkMonitor
import pl.nowinkitransferowe.core.data.util.TimeZoneBroadcastMonitor
import pl.nowinkitransferowe.core.data.util.TimeZoneMonitor

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsArticleRepository(
        articleRepository: OfflineFirstArticleRepository,
    ): ArticleRepository

    @Binds
    internal abstract fun bindsTransferRepository(
        transferRepository: OfflineFirstTransferRepository,
    ): TransferRepository

    @Binds
    internal abstract fun bindsRumourRepository(
        rumourRepository: OfflineFirstRumourRepository,
    ): RumourRepository

    @Binds
    internal abstract fun bindsClubRepository(
        clubRepository: OfflineFirstClubRepository,
    ): ClubRepository

    @Binds
    internal abstract fun bindsPlayerRepository(
        playerRepository: OfflineFirstPlayerRepository,
    ): PlayerRepository

    @Binds
    internal abstract fun bindsDictionaryRepository(
        dictionaryRepository: OfflineFirstDictionaryRepository,
    ): DictionaryRepository

    @Binds
    internal abstract fun bindsUserArticleRepository(
        userArticleRepository: CompositeUserArticleRepository,
    ): UserArticleRepository

    @Binds
    internal abstract fun bindsUserTransferRepository(
        userTransferRepository: CompositeUserTransferRepository,
    ): UserTransferRepository

    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: OfflineFirstUserDataRepository,
    ): UserDataRepository

    @Binds
    internal abstract fun bindsRecentSearchRepository(
        recentSearchRepository: DefaultRecentSearchRepository,
    ): RecentSearchRepository

    @Binds
    internal abstract fun bindsSearchContentsRepository(
        searchContentsRepository: DefaultSearchContentsRepository,
    ): SearchContentsRepository

    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor

    @Binds
    internal abstract fun binds(impl: TimeZoneBroadcastMonitor): TimeZoneMonitor

    @Binds
    internal abstract fun bindsImageDownloader(defaultImageDownLoader: DefaultImageDownLoader): ImageDownloader
}
