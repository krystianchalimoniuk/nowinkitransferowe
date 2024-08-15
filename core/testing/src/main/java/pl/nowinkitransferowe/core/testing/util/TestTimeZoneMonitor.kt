package pl.nowinkitransferowe.core.testing.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.TimeZone
import pl.nowinkitransferowe.core.data.util.TimeZoneMonitor

class TestTimeZoneMonitor : TimeZoneMonitor {

    private val timeZoneFlow = MutableStateFlow(defaultTimeZone)

    override val currentTimeZone: Flow<TimeZone> = timeZoneFlow

    /**
     * A test-only API to set the from tests.
     */
    fun setTimeZone(zoneId: TimeZone) {
        timeZoneFlow.value = zoneId
    }

    companion object {
        val defaultTimeZone: TimeZone = TimeZone.of("Europe/Warsaw")
    }
}
