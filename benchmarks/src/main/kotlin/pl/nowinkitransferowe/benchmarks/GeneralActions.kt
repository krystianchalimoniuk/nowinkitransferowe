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

package pl.nowinkitransferowe.benchmarks

import androidx.test.uiautomator.untilHasChildren
import android.Manifest.permission
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

/**
 * Because the app under test is different from the one running the instrumentation test,
 * the permission has to be granted manually by either:
 *
 * - tapping the Allow button
 *    ```kotlin
 *    val obj = By.text("Allow")
 *    val dialog = device.wait(Until.findObject(obj), TIMEOUT)
 *    dialog?.let {
 *        it.click()
 *        device.wait(Until.gone(obj), 5_000)
 *    }
 *    ```
 * - or (preferred) executing the grant command on the target package.
 */
fun MacrobenchmarkScope.allowNotifications() {
    if (SDK_INT >= TIRAMISU) {
        val command = "pm grant $packageName ${permission.POST_NOTIFICATIONS}"
        device.executeShellCommand(command)
    }
}

/**
 * Wraps starting the default activity, waiting for it to start and then allowing notifications in
 * one convenient call.
 */
fun MacrobenchmarkScope.startActivityAndAllowNotifications() {
    startActivityAndWait()
    allowNotifications()
    skipClubsOnboarding()
}

/**
 * Skips "Wybierz swoje kluby", which a fresh install shows before the app.
 */
fun MacrobenchmarkScope.skipClubsOnboarding() {
    val skip = By.text("Pomiń")
    device.wait(Until.findObject(skip), 5_000)?.let {
        it.click()
        device.wait(Until.gone(skip), 5_000)
    }
}

/** Opens the top level destination labelled [label] in the navigation bar. */
fun MacrobenchmarkScope.goToTopLevel(label: String, content: String) {
    device.waitAndFindObject(By.text(label), 5_000)
    // The navigation bar is at the bottom; the home screen also has a "Plotki" tab
    device.findObjects(By.text(label)).maxBy { it.visibleBounds.bottom }.click()
    device.waitForIdle()
    device.wait(Until.gone(By.res("loadingWheel")), 5_000)
    device.waitAndFindObject(By.res(content), 30_000).wait(untilHasChildren(), 30_000)
}

/** Clicks the first item of the list [list] that is a card or row, not a chip or tab. */
fun MacrobenchmarkScope.openFirstItem(list: String) {
    // The list can be on screen before its items, so wait for one
    val deadline = System.currentTimeMillis() + 10_000
    while (true) {
        val item = device.findObject(By.res(list))
            ?.findObjects(By.clickable(true))
            ?.firstOrNull { it.visibleBounds.width() > device.displayWidth / 2 && it.visibleBounds.height() > device.displayHeight / 24 }
        if (item != null) {
            item.click()
            device.waitForIdle()
            return
        }
        check(System.currentTimeMillis() < deadline) { "No item to open in $list" }
        Thread.sleep(200)
    }
}

/**
 * Waits for and returns the `ntTopAppBar`
 */
fun MacrobenchmarkScope.getTopAppBar(): UiObject2 {
    device.wait(Until.hasObject(By.res("ntTopAppBar")), 2_000)
    return device.findObject(By.res("ntTopAppBar"))
}

/**
 * Waits for an object on the top app bar, passed in as [selector].
 */
fun MacrobenchmarkScope.waitForObjectOnTopAppBar(selector: BySelector, timeout: Long = 2_000) {
    getTopAppBar().wait(Until.hasObject(selector), timeout)
}
