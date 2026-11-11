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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RumourTest {

    private val updatedAt = Instant.parse("2026-10-20T10:00:00Z")

    @Test
    fun trend_compares_with_the_previous_stage() {
        assertEquals(RumourTrend.UP, rumour(RumourStage.CLOSE, RumourStage.TALKS).trend)
        assertEquals(RumourTrend.DOWN, rumour(RumourStage.TALKS, RumourStage.CLOSE).trend)
        assertEquals(RumourTrend.SAME, rumour(RumourStage.TALKS, RumourStage.TALKS).trend)
        assertEquals(RumourTrend.NEW, rumour(RumourStage.SPECULATION, null).trend)
    }

    @Test
    fun only_active_rumours_updated_in_45_days_are_fresh() {
        assertTrue(rumour().isFresh(Instant.parse("2026-12-04T10:00:00Z")))
        assertFalse(rumour().isFresh(Instant.parse("2026-12-04T10:00:01Z")))
        assertFalse(rumour(status = RumourStatus.CONFIRMED).isFresh(updatedAt))
    }

    @Test
    fun stage_levels_outside_the_range_are_clamped() {
        assertEquals(RumourStage.CLOSE, RumourStage.fromLevel(4))
        assertEquals(RumourStage.AGREEMENT, RumourStage.fromLevel(6))
        assertEquals(RumourStage.SPECULATION, RumourStage.fromLevel(0))
    }

    @Test
    fun accuracy_is_shown_only_when_allowed() {
        val stats = RumourStats(
            season = Season(2026),
            active = 14,
            confirmed = 23,
            failed = 9,
            showAccuracy = true,
            byStage = listOf(StageAccuracy(RumourStage.AGREEMENT, confirmed = 9, closed = 10)),
            hotClubs = emptyList(),
            daysToConfirm = 11.5,
        )

        assertEquals(23.0 / 32, stats.accuracy)
        assertEquals(0.9, stats.byStage.single().accuracy)
        assertNull(stats.copy(showAccuracy = false).accuracy)
    }

    private fun rumour(
        stage: RumourStage = RumourStage.CLOSE,
        previousStage: RumourStage? = RumourStage.TALKS,
        status: RumourStatus = RumourStatus.ACTIVE,
    ) = Rumour(
        id = 77,
        player = PlayerRef(id = 12148, name = "Gabriel Martinelli"),
        from = ClubRef.Club(id = 3410, name = "Arsenal FC"),
        to = ClubRef.Club(id = 5120, name = "Al-Hilal SFC"),
        stage = stage,
        previousStage = previousStage,
        fee = "ok. 70 mln €",
        status = status,
        transferId = null,
        updatedAt = updatedAt,
        latestArticleId = 4321,
        history = emptyList(),
    )
}
