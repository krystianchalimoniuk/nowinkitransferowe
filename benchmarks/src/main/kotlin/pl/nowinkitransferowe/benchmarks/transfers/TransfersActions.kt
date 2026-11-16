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

package pl.nowinkitransferowe.benchmarks.transfers

import pl.nowinkitransferowe.benchmarks.waitAndFindObject
import pl.nowinkitransferowe.benchmarks.goToTopLevel
import androidx.test.uiautomator.untilHasChildren
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import pl.nowinkitransferowe.benchmarks.flingElementDownUp
import pl.nowinkitransferowe.benchmarks.waitForObjectOnTopAppBar

fun MacrobenchmarkScope.goToTransfersScreen() {
    goToTopLevel(label = "Transfery", content = "transfers:feed")
}

fun MacrobenchmarkScope.transfersScrollTopicsDownUp() {
    device.wait(Until.hasObject(By.res("transfers:feed")), 5_000)
    val transfersList = device.findObject(By.res("transfers:feed"))
    device.flingElementDownUp(transfersList)
}

fun MacrobenchmarkScope.transfersWaitForTransfer() {
    device.waitAndFindObject(By.res("transfers:feed"), 30_000).wait(untilHasChildren(childCount = 3), 30_000)
}

/** Switches the league chip, which reloads the list, as the bookmark did in 3.x. */
fun MacrobenchmarkScope.transfersToggleBookmarked() {
    val chip = device.findObject(By.res("transfers:feed")).findObject(By.checkable(true).checked(false)) ?: return
    chip.click()
    device.waitForIdle()
}
