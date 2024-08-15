package pl.nowinkitransferowe.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.nowinkitransferowe.core.database.NtDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesNTDatabase(
        @ApplicationContext context: Context,
    ): NtDatabase = Room.databaseBuilder(
        context,
        NtDatabase::class.java,
        "nt-database",
    ).build()

}