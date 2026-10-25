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

package pl.nowinkitransferowe.feature.details.transfers

import androidx.collection.floatListOf
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UtilTest {

    @Test
    fun shortcutDate() {
        val expectedList = arrayListOf("paź 22", "maj 21", "kwi 24", "gru 18")
        sampleDatesList.forEachIndexed { index, s ->
            assertEquals(expectedList[index], Util.shortcutDate(Util.dateFormatted(s)))
        }
    }

    @Test
    fun priceToFloat() {
        val expectedList = floatListOf(0f, 0f, 0f, 25f, 3f, 100f, 0.8f)
        samplePricesList.forEachIndexed { index, s ->
            assertEquals(expectedList[index], Util.priceToFloat(s))
        }
    }

    @Test
    fun hasMoreThanTwoCashTransfers() {
        val testListPositive = samplePricesList.takeLast(2)
        val testListNegative = samplePricesList.take(4)
        assertTrue(Util.hasMoreThanOrEqualTwoCashTransfers(testListPositive))
        assertFalse(Util.hasMoreThanOrEqualTwoCashTransfers(testListNegative))
    }

    @Test
    fun calculateTransferSum() {
        assertEquals(" 128.8 mln €", Util.calculateTransferSum(samplePricesList))
    }

    @Test
    fun calculateMaxTransferValue() {
        assertEquals("100.0 mln €", Util.calculateMaxTransferValue(samplePricesList))
    }

    @Test
    fun dateFormatted() {
        val expectedList = arrayListOf("7 paź 2022", "2 maj 2021", "12 kwi 2024", "22 gru 2018")
        sampleDatesList.forEachIndexed { index, s ->
            assertEquals(expectedList[index], Util.dateFormatted(s))
        }
    }

    private val samplePricesList = arrayListOf(
        "za darmo",
        "nie ujawniono",
        "wypożyczenie",
        "25 mln €",
        "3 mln €",
        "100 mln €",
        "0,8 mln €",
    )

    private val sampleDatesList = arrayListOf(
        Instant.parse("2022-10-06T23:00:00.000Z"),
        Instant.parse("2021-05-02T10:00:00.000Z"),
        Instant.parse("2024-04-12T02:00:00.000Z"),
        Instant.parse("2018-12-22T11:00:00.000Z"),
    )
}
