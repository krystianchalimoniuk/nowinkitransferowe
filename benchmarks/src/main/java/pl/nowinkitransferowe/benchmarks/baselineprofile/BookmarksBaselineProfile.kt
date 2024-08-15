package pl.nowinkitransferowe.benchmarks.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.benchmarks.PACKAGE_NAME
import pl.nowinkitransferowe.benchmarks.bookmarks.goToBookmarksScreen
import pl.nowinkitransferowe.benchmarks.startActivityAndAllowNotifications

/**
 * Baseline Profile of the "Bookmarks" screen
 */
class BookmarksBaselineProfile {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() =
        baselineProfileRule.collect(PACKAGE_NAME) {
            startActivityAndAllowNotifications()

            // Navigate to saved screen
            goToBookmarksScreen()
        }
}
