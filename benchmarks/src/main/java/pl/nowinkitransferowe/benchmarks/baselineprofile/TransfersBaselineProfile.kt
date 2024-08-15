package pl.nowinkitransferowe.benchmarks.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.benchmarks.PACKAGE_NAME

import pl.nowinkitransferowe.benchmarks.startActivityAndAllowNotifications
import pl.nowinkitransferowe.benchmarks.transfers.goToTransfersScreen
import pl.nowinkitransferowe.benchmarks.transfers.transfersScrollTopicsDownUp

/**
 * Baseline Profile of the "Transfers" screen
 */
class TransfersBaselineProfile {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() =
        baselineProfileRule.collect(PACKAGE_NAME) {
            startActivityAndAllowNotifications()

            // Navigate to transfers screen
            goToTransfersScreen()
            transfersScrollTopicsDownUp()
        }
}
