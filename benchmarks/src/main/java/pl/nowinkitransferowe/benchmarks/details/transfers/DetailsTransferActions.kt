package pl.nowinkitransferowe.benchmarks.details.transfers

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

fun MacrobenchmarkScope.goToDetailsTransferScreen() {
    device.findObject(By.text("Raphael Varane")).click()
    device.waitForIdle()
    // Wait until transfers are shown on screen
    waitForObjectOnTopAppBar(By.text("Sezon"))

    // Wait until content is loaded by checking if transfers are loaded
    device.wait(Until.gone(By.res("loadingWheel")), 5_000)
}

fun MacrobenchmarkScope.detailsTransferScrollFeedDownUp() {
    val feedList = device.findObject(By.res("content"))
    device.flingElementDownUp(feedList)
}