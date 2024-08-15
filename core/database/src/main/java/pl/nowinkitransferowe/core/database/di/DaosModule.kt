package pl.nowinkitransferowe.core.database.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.nowinkitransferowe.core.database.NtDatabase
import pl.nowinkitransferowe.core.database.dao.NewsResourceDao
import pl.nowinkitransferowe.core.database.dao.NewsResourceFtsDao
import pl.nowinkitransferowe.core.database.dao.RecentSearchQueryDao
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

}
