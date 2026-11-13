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

package pl.nowinkitransferowe.core.testing.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.PlayerRef
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStats
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.RumourStep
import pl.nowinkitransferowe.core.model.Season
import kotlin.time.Duration.Companion.days

/** Active rumours updated recently, so the home screen shows them; newest first. */
val rumoursTestData: List<Rumour> = listOf(
    Rumour(
        id = 5,
        player = PlayerRef(id = 3001, name = "Gabriel Martinelli"),
        from = ClubRef.Club(id = 1, name = "Arsenal"),
        to = ClubRef.Club(id = 2, name = "Al-Hilal SFC"),
        stage = RumourStage.CLOSE,
        previousStage = RumourStage.TALKS,
        fee = "ok. 70 mln €",
        status = RumourStatus.ACTIVE,
        transferId = null,
        updatedAt = Clock.System.now() - 1.days,
        latestArticleId = null,
        history = emptyList(),
    ),
    Rumour(
        id = 4,
        player = PlayerRef(id = 3002, name = "Gavi"),
        from = ClubRef.Club(id = 3, name = "FC Barcelona"),
        to = ClubRef.Club(id = 4, name = "Liverpool FC"),
        stage = RumourStage.SPECULATION,
        previousStage = null,
        fee = null,
        status = RumourStatus.ACTIVE,
        transferId = null,
        updatedAt = Clock.System.now() - 2.days,
        latestArticleId = null,
        history = emptyList(),
    ),
)

/**
 * Rumours with fixed dates for screenshots, newest first. The first one has a history whose
 * steps link to [articlesTestData], the second one was confirmed by a transfer.
 */
val fixedRumoursTestData: List<Rumour> = listOf(
    Rumour(
        id = 11,
        player = PlayerRef(id = 3001, name = "Gabriel Martinelli", nationality = "br"),
        from = ClubRef.Club(id = 1, name = "Arsenal"),
        to = ClubRef.Club(id = 2, name = "Al-Hilal SFC"),
        stage = RumourStage.CLOSE,
        previousStage = RumourStage.TALKS,
        fee = "ok. 70 mln €",
        status = RumourStatus.ACTIVE,
        transferId = null,
        updatedAt = Instant.parse("2026-10-20T12:00:00Z"),
        latestArticleId = 11284,
        history = listOf(
            RumourStep(RumourStage.INTEREST, Instant.parse("2026-10-02T12:00:00Z"), articleId = 11282),
            RumourStep(RumourStage.TALKS, Instant.parse("2026-10-12T12:00:00Z"), articleId = null),
            RumourStep(RumourStage.CLOSE, Instant.parse("2026-10-20T12:00:00Z"), articleId = 11284),
        ),
    ),
    Rumour(
        id = 12,
        player = PlayerRef(id = 3003, name = "Fermín López", nationality = "es"),
        from = ClubRef.Club(id = 3, name = "FC Barcelona"),
        to = ClubRef.Club(id = 5, name = "PSG FC"),
        stage = RumourStage.SPECULATION,
        previousStage = null,
        fee = "80 mln €",
        status = RumourStatus.ACTIVE,
        transferId = null,
        updatedAt = Instant.parse("2026-10-18T12:00:00Z"),
        latestArticleId = null,
        history = emptyList(),
    ),
    Rumour(
        id = 13,
        player = PlayerRef(id = 3004, name = "Rayan Cherki", nationality = "fr"),
        from = ClubRef.Club(id = 6, name = "Olympique Lyon"),
        to = ClubRef.Club(id = 7, name = "Real Madryt"),
        stage = RumourStage.TALKS,
        previousStage = RumourStage.CLOSE,
        fee = null,
        status = RumourStatus.ACTIVE,
        transferId = 101,
        updatedAt = Instant.parse("2026-10-15T12:00:00Z"),
        latestArticleId = null,
        history = emptyList(),
    ),
)

/** Stats of a season with enough closed rumours to show the accuracy. */
val rumourStatsTestData = RumourStats(
    season = Season(2026),
    active = 14,
    confirmed = 23,
    failed = 9,
    showAccuracy = true,
    byStage = emptyList(),
    hotClubs = emptyList(),
    daysToConfirm = 12.5,
)
