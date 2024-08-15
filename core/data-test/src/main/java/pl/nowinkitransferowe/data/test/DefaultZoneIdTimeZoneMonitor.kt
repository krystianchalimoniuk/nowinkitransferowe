package pl.nowinkitransferowe.data.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.TimeZone
import pl.nowinkitransferowe.core.data.util.TimeZoneMonitor
import javax.inject.Inject

class DefaultZoneIdTimeZoneMonitor @Inject constructor() : TimeZoneMonitor {
    override val currentTimeZone: Flow<TimeZone> = flowOf(TimeZone.of("Europe/Warsaw"))
}
