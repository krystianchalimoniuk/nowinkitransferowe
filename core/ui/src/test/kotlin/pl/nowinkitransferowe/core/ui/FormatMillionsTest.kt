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

package pl.nowinkitransferowe.core.ui

import org.junit.Test
import kotlin.test.assertEquals

class FormatMillionsTest {
    @Test
    fun millionsUseADecimalCommaAndDropZeroDecimals() {
        assertEquals("45,5", formatMillions(45.5))
        assertEquals("40", formatMillions(40.0))
        assertEquals("-74,5", formatMillions(-74.5))
        assertEquals("1,3", formatMillions(1.25))
    }
}
