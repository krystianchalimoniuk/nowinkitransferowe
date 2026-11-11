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

package pl.nowinkitransferowe.core.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeasonTest {

    @Test
    fun a_season_starts_on_1_april() {
        assertEquals(Season(2025), Season.of(LocalDate(2026, 3, 31)))
        assertEquals(Season(2026), Season.of(LocalDate(2026, 4, 1)))
    }

    @Test
    fun the_boundary_is_counted_in_polish_time() {
        // 22:30 UTC on 31 March is already 1 April in Warsaw (summer time, UTC+2).
        assertEquals(Season(2026), Season.of(Instant.parse("2026-03-31T22:30:00Z")))
        assertEquals(Season(2025), Season.of(Instant.parse("2026-03-31T21:30:00Z")))
    }

    @Test
    fun label_and_slug_use_two_digit_years() {
        assertEquals("26/27", Season(2026).label)
        assertEquals("26-27", Season(2026).slug)
        assertEquals("99/00", Season(1999).label)
        assertEquals("09/10", Season(2009).label)
    }

    @Test
    fun parses_labels_and_slugs() {
        assertEquals(Season(2026), Season.parse("26/27"))
        assertEquals(Season(2025), Season.parse("25-26"))
        assertEquals(Season(1999), Season.parse("99/00"))
        assertNull(Season.parse("26/28"))
        assertNull(Season.parse("sezon"))
    }

    @Test
    fun contains_and_order() {
        assertTrue(LocalDate(2027, 1, 15) in Season(2026))
        assertFalse(LocalDate(2027, 4, 1) in Season(2026))
        assertEquals(Season(2025), Season(2026).previous())
        assertEquals(listOf(Season(2026), Season(2025)), listOf(Season(2025), Season(2026)).sortedDescending())
    }
}
