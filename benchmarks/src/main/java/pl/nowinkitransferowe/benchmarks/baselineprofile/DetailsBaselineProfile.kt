package pl.nowinkitransferowe.benchmarks.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.benchmarks.PACKAGE_NAME
import pl.nowinkitransferowe.benchmarks.details.detailsScrollFeedDownUp
import pl.nowinkitransferowe.benchmarks.details.goToDetailsScreen
import pl.nowinkitransferowe.benchmarks.news.newsWaitForContent
import pl.nowinkitransferowe.benchmarks.startActivityAndAllowNotifications

/**
 * Baseline Profile of the "Interests" screen
 */
class DetailsBaselineProfile {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() =
        baselineProfileRule.collect(PACKAGE_NAME) {
            startActivityAndAllowNotifications()
            newsWaitForContent()
            goToDetailsScreen()
            detailsScrollFeedDownUp()
        }
}
