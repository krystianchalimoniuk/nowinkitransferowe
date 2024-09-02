package pl.nowinkitransferowe.benchmarks.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.benchmarks.PACKAGE_NAME
import pl.nowinkitransferowe.benchmarks.details.transfers.detailsTransferScrollFeedDownUp
import pl.nowinkitransferowe.benchmarks.details.transfers.goToDetailsTransferScreen
import pl.nowinkitransferowe.benchmarks.details.transfers.goToTransfersScreen
import pl.nowinkitransferowe.benchmarks.news.newsWaitForContent
import pl.nowinkitransferowe.benchmarks.startActivityAndAllowNotifications

/**
 * Baseline Profile of the "DetailsTransfer" screen
 */
class DetailsTransferBaselineProfile {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() =
        baselineProfileRule.collect(PACKAGE_NAME) {
            startActivityAndAllowNotifications()
            newsWaitForContent()
            goToTransfersScreen()
            goToDetailsTransferScreen()
            detailsTransferScrollFeedDownUp()
        }
}
