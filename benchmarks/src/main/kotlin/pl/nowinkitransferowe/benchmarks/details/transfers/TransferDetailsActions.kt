/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.nowinkitransferowe.benchmarks.details.transfers

import pl.nowinkitransferowe.benchmarks.openFirstItem
import pl.nowinkitransferowe.benchmarks.goToTopLevel
import java.util.regex.Pattern
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import pl.nowinkitransferowe.benchmarks.flingElementDownUp
import pl.nowinkitransferowe.benchmarks.waitForObjectOnTopAppBar

fun MacrobenchmarkScope.goToTransfersScreen() {
    goToTopLevel(label = "Transfery", content = "transfers:feed")
}

fun MacrobenchmarkScope.goToDetailsTransferScreen() {
    openFirstItem("transfers:feed")
    device.wait(Until.hasObject(By.res(Pattern.compile("transfer:\\d+"))), 10_000)
}

fun MacrobenchmarkScope.detailsTransferScrollFeedDownUp() {
    val feedList = device.findObject(By.res(Pattern.compile("transfer:\\d+"))) ?: device.findObject(By.scrollable(true))
    device.flingElementDownUp(feedList)
}
