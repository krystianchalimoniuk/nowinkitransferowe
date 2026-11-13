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
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.PlayerRef
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourStatus
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
