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

package pl.nowinkitransferowe.benchmarks.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.benchmarks.PACKAGE_NAME
import pl.nowinkitransferowe.benchmarks.details.news.detailsNewsScrollFeedDownUp
import pl.nowinkitransferowe.benchmarks.details.news.goToDetailsNewsScreen
import pl.nowinkitransferowe.benchmarks.news.newsWaitForContent
import pl.nowinkitransferowe.benchmarks.startActivityAndAllowNotifications

/**
 * Baseline Profile of the "DetailsNews" screen
 */
class DetailsNewsBaselineProfile {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() =
        baselineProfileRule.collect(PACKAGE_NAME) {
            startActivityAndAllowNotifications()
            newsWaitForContent()
            goToDetailsNewsScreen()
            detailsNewsScrollFeedDownUp()
        }
}