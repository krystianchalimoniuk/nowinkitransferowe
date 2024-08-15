package pl.nowinkitransferowe.sync.work.status

/**
 * Subscribes to backend requested synchronization
 */
interface Subscriber {
    suspend fun subscribeToSync()
    suspend fun subscribeToGeneral()
}
