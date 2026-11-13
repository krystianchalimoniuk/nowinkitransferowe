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

package pl.nowinkitransferowe.feature.player.impl

import org.junit.Test
import kotlin.test.assertEquals

class FeeChartTest {
    @Test
    fun niceMax_roundsUpToFourRoundSteps() {
        // Like the website: 45 mln € gets a grid of 0, 12.5, 25, 37.5 and 50
        assertEquals(50.0, niceMax(45.0))
        assertEquals(5.0, niceMax(4.75))
        assertEquals(0.4, niceMax(0.38), 1e-9)
        assertEquals(120.0, niceMax(105.0))
        assertEquals(1.0, niceMax(0.0))
    }
}
