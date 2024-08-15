package pl.nowinkitransferowe.benchmarks.transfers

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.nowinkitransferowe.benchmarks.PACKAGE_NAME
import pl.nowinkitransferowe.benchmarks.startActivityAndAllowNotifications
import pl.nowinkitransferowe.benchmarks.waitAndFindObject

@RunWith(AndroidJUnit4::class)
class TransferScreenRecompositionBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun benchmarkStateChangeCompilationBaselineProfile() =
        benchmarkStateChange(CompilationMode.Partial())

    private fun benchmarkStateChange(compilationMode: CompilationMode) =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            iterations = 10,
            startupMode = StartupMode.WARM,
            setupBlock = {
                // Start the app
                pressHome()
                startActivityAndAllowNotifications()
                // Navigate to interests screen
                device.waitAndFindObject(By.text("Transfery"), timeout = 5_000).click()
                device.waitForIdle()
            },
        ) {
            transfersWaitForTransfer()
            repeat(3) {
                transfersToggleBookmarked()
            }
        }
}
