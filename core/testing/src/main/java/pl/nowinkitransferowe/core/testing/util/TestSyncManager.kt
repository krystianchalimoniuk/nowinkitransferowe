package pl.nowinkitransferowe.core.testing.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.nowinkitransferowe.core.data.util.SyncManager

class TestSyncManager : SyncManager {

    private val syncStatusFlow = MutableStateFlow(false)
    override val isSyncing: Flow<Boolean> = syncStatusFlow

    override fun requestSync(): Unit = TODO("Not yet implemented")


    /**
     * A test-only API to set the sync status from tests.
     */
    fun setSyncing(isSyncing: Boolean) {
        syncStatusFlow.value = isSyncing
    }

}