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

package pl.nowinkitransferowe.data.test

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import pl.nowinkitransferowe.core.data.di.DataModule
import pl.nowinkitransferowe.core.data.repository.ArticleRepository
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.NewsRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.RecentSearchRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.data.repository.TransferResourceRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.util.ImageDownloader
import pl.nowinkitransferowe.core.data.util.NetworkMonitor
import pl.nowinkitransferowe.core.data.util.TimeZoneMonitor
import pl.nowinkitransferowe.data.test.repository.FakeArticleRepository
import pl.nowinkitransferowe.data.test.repository.FakeClubRepository
import pl.nowinkitransferowe.data.test.repository.FakeDictionaryRepository
import pl.nowinkitransferowe.data.test.repository.FakeNewsRepository
import pl.nowinkitransferowe.data.test.repository.FakePlayerRepository
import pl.nowinkitransferowe.data.test.repository.FakeRecentSearchRepository
import pl.nowinkitransferowe.data.test.repository.FakeRumourRepository
import pl.nowinkitransferowe.data.test.repository.FakeSearchContentsRepository
import pl.nowinkitransferowe.data.test.repository.FakeTransferRepository
import pl.nowinkitransferowe.data.test.repository.FakeTransferResourceRepository
import pl.nowinkitransferowe.data.test.repository.FakeUserDataRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
internal interface TestDataModule {
    @Binds
    fun bindsTransferResourceRepository(
        fakeTransferResourceRepository: FakeTransferResourceRepository,
    ): TransferResourceRepository

    @Binds
    fun bindsNewsResourceRepository(
        fakeNewsRepository: FakeNewsRepository,
    ): NewsRepository

    @Binds
    fun bindsUserDataRepository(
        userDataRepository: FakeUserDataRepository,
    ): UserDataRepository

    @Binds
    fun bindsRecentSearchRepository(
        recentSearchRepository: FakeRecentSearchRepository,
    ): RecentSearchRepository

    @Binds
    fun bindsSearchContentsRepository(
        searchContentsRepository: FakeSearchContentsRepository,
    ): SearchContentsRepository

    @Binds
    fun bindsDictionaryRepository(repository: FakeDictionaryRepository): DictionaryRepository

    @Binds
    fun bindsArticleRepository(repository: FakeArticleRepository): ArticleRepository

    @Binds
    fun bindsTransferRepository(repository: FakeTransferRepository): TransferRepository

    @Binds
    fun bindsRumourRepository(repository: FakeRumourRepository): RumourRepository

    @Binds
    fun bindsClubRepository(repository: FakeClubRepository): ClubRepository

    @Binds
    fun bindsPlayerRepository(repository: FakePlayerRepository): PlayerRepository

    @Binds
    fun bindsNetworkMonitor(
        networkMonitor: AlwaysOnlineNetworkMonitor,
    ): NetworkMonitor

    @Binds
    fun binds(impl: DefaultZoneIdTimeZoneMonitor): TimeZoneMonitor

    @Binds
    fun bindsImageDownloader(impl: FakeImageDownloader): ImageDownloader
}
