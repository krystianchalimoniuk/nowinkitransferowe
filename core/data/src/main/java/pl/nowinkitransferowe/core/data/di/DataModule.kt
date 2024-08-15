package pl.nowinkitransferowe.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.nowinkitransferowe.core.data.repository.DefaultRecentSearchRepository
import pl.nowinkitransferowe.core.data.repository.DefaultSearchContentsRepository
import pl.nowinkitransferowe.core.data.repository.NewsRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstNewsRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstTransferRepository
import pl.nowinkitransferowe.core.data.repository.OfflineFirstUserDataRepository
import pl.nowinkitransferowe.core.data.repository.RecentSearchRepository
import pl.nowinkitransferowe.core.data.repository.SearchContentsRepository
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.util.ConnectivityManagerNetworkMonitor
import pl.nowinkitransferowe.core.data.util.DatabaseUpdatingMonitor
import pl.nowinkitransferowe.core.data.util.DefaultDatabaseUpdatingMonitor
import pl.nowinkitransferowe.core.data.util.NetworkMonitor
import pl.nowinkitransferowe.core.data.util.TimeZoneBroadcastMonitor
import pl.nowinkitransferowe.core.data.util.TimeZoneMonitor

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsNewsResourceRepository(
        newsRepository: OfflineFirstNewsRepository,
    ): NewsRepository

    @Binds
    internal abstract fun bindsTransferResourceRepository(
        newsRepository: OfflineFirstTransferRepository,
    ): TransferRepository

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
    internal abstract fun bindsDatabaseUpdatingMonitor(defaultDatabaseUpdatingMonitor: DefaultDatabaseUpdatingMonitor): DatabaseUpdatingMonitor
}