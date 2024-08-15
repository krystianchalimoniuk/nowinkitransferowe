package pl.nowinkitransferowe.sync.synctest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.nowinkitransferowe.core.data.util.SyncManager
import javax.inject.Inject

internal class NeverSyncingSyncManager @Inject constructor() : SyncManager {
    override val isSyncing: Flow<Boolean> = flowOf(false)
    override fun requestSync() = Unit
}
