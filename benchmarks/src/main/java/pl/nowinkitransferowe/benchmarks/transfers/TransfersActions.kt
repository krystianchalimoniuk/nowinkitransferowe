package pl.nowinkitransferowe.benchmarks.transfers

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import pl.nowinkitransferowe.benchmarks.flingElementDownUp
import pl.nowinkitransferowe.benchmarks.waitForObjectOnTopAppBar

fun MacrobenchmarkScope.goToTransfersScreen() {
    device.findObject(By.text("Transfery")).click()
    device.waitForIdle()
    // Wait until transfers are shown on screen
    waitForObjectOnTopAppBar(By.text("Transfery"))

    // Wait until content is loaded by checking if transfers are loaded
    device.wait(Until.gone(By.res("loadingWheel")), 5_000)
}
fun MacrobenchmarkScope.transfersScrollTopicsDownUp() {
    device.wait(Until.hasObject(By.res("transfers:feed")), 5_000)
    val transfersList = device.findObject(By.res("transfers:feed"))
    device.flingElementDownUp(transfersList)
}

fun MacrobenchmarkScope.transfersWaitForTransfer() {
    device.wait(Until.hasObject(By.text("Raphael Varane")), 30_000)
}

fun MacrobenchmarkScope.transfersToggleBookmarked() {
    val topicsList = device.findObject(By.res("transfers:feed"))
    val checkable = topicsList.findObject(By.checkable(true))
    checkable.click()
    device.waitForIdle()
}
