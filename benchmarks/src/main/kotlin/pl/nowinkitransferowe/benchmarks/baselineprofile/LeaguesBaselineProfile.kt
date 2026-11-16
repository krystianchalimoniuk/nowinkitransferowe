/*
 * Copyright 2026 The Android Open Source Project
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

package pl.nowinkitransferowe.benchmarks.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.benchmarks.PACKAGE_NAME
import pl.nowinkitransferowe.benchmarks.flingElementDownUp
import pl.nowinkitransferowe.benchmarks.goToTopLevel
import pl.nowinkitransferowe.benchmarks.openFirstItem
import pl.nowinkitransferowe.benchmarks.startActivityAndAllowNotifications
import java.util.regex.Pattern

/**
 * Baseline Profile of the leagues and a league with its clubs.
 */
class LeaguesBaselineProfile {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() =
        baselineProfileRule.collect(PACKAGE_NAME) {
            startActivityAndAllowNotifications()

            goToTopLevel(label = "Ligi", content = "leagues:list")
            device.flingElementDownUp(device.findObject(By.res("leagues:list")))
            openFirstItem("leagues:list")
            device.wait(Until.hasObject(By.res(Pattern.compile("league:.+:details"))), 10_000)
        }
}
