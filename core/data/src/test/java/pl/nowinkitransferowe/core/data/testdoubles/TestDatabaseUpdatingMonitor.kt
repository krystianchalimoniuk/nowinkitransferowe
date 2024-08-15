package pl.nowinkitransferowe.core.data.testdoubles

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.nowinkitransferowe.core.data.util.DatabaseUpdatingMonitor

class TestDatabaseUpdatingMonitor : DatabaseUpdatingMonitor {
    private val _newsDataChanged: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val newsDataChanged: Flow<Boolean>
        get() = _newsDataChanged

    private val _transferDataChanged: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val transferDataChanged: Flow<Boolean>
        get() = _transferDataChanged

    override suspend fun notifyNewsDataChanged() {
        _newsDataChanged.value = true

    }

    override suspend fun notifyTransfersDataChanged() {
        _transferDataChanged.value = true
    }

}