package pl.nowinkitransferowe.sync.work.status

import android.util.Log
import javax.inject.Inject

private const val TAG = "StubSyncSubscriber"

/**
 * Stub implementation of [Subscriber]
 */
class StubSubscriber @Inject constructor() : Subscriber {
    override suspend fun subscribeToSync() {
        Log.d(TAG, "Subscribing to sync")
    }

    override suspend fun subscribeToGeneral() {
        Log.d(TAG, "Subscribing to general")
    }
}
