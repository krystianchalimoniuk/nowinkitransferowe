package pl.nowinkitransferowe.sync.synctest

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.sync.di.SyncModule
import pl.nowinkitransferowe.sync.work.status.StubSubscriber
import pl.nowinkitransferowe.sync.work.status.Subscriber

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SyncModule::class],
)
internal interface TestSyncModule {
    @Binds
    fun bindsSyncStatusMonitor(
        syncStatusMonitor: NeverSyncingSyncManager,
    ): SyncManager

    @Binds
    fun bindsSyncSubscriber(
        subscriber: StubSubscriber,
    ): Subscriber
}
