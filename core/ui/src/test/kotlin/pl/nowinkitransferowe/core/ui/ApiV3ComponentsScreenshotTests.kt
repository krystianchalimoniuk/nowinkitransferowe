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

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Fee
import pl.nowinkitransferowe.core.model.FeeKind
import pl.nowinkitransferowe.core.model.NoClubStatus
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourTrend
import pl.nowinkitransferowe.core.model.Season
import pl.nowinkitransferowe.core.screenshottesting.captureMultiTheme

/**
 * The components that show API v3 data, each in the light and dark theme.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class, qualifiers = "w360dp-h640dp-480dpi")
@LooperMode(LooperMode.Mode.PAUSED)
class ApiV3ComponentsScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun articleKinds() = capture("ArticleKindBadge") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ArticleKind.entries.forEach { ArticleKindBadge(it) }
        }
    }

    @Test
    fun fees() = capture("FeeLabel") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FeeLabel(Fee(FeeKind.AMOUNT, "45,5 mln €", 45.5))
            FeeLabel(Fee(FeeKind.FREE, "za darmo", null))
            FeeLabel(Fee(FeeKind.LOAN, "wypożyczenie", null))
            FeeLabel(Fee(FeeKind.UNDISCLOSED, "nie ujawniono", null))
        }
    }

    @Test
    fun clubCrests() = capture("ClubCrest") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ClubCrest(ClubRef.Club(id = 83, name = "West Ham United"))
            ClubCrest(ClubRef.NoClub(NoClubStatus.FREE_AGENT, "Bez klubu"))
            ClubCrest(ClubRef.Club(id = 1, name = "Arsenal"), size = 64.dp)
            CountryFlag("gb-eng", contentDescription = "Anglia")
        }
    }

    @Test
    fun rumourStages() = capture("RumourStageIndicator") {
        Column(Modifier.width(260.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RumourStageIndicator(RumourStage.SPECULATION, RumourTrend.NEW)
            RumourStageIndicator(RumourStage.INTEREST, RumourTrend.UP)
            RumourStageIndicator(RumourStage.TALKS, RumourTrend.DOWN)
            RumourStageIndicator(RumourStage.CLOSE, RumourTrend.UP)
            RumourStageIndicator(RumourStage.AGREEMENT, RumourTrend.SAME)
        }
    }

    @Test
    fun spending() = capture("SpendingBars") {
        SpendingBars(
            rows = listOf(
                SpendingRow(Season.of(LocalDate(2026, 7, 1)).label, moneyIn = 45.5, moneyOut = 120.0),
                SpendingRow(Season.of(LocalDate(2025, 7, 1)).label, moneyIn = 80.0, moneyOut = 30.0),
                SpendingRow(Season.of(LocalDate(2024, 7, 1)).label, moneyIn = 12.0, moneyOut = 12.0),
            ),
            modifier = Modifier.width(320.dp),
        )
    }

    private fun capture(name: String, content: @Composable () -> Unit) {
        composeTestRule.captureMultiTheme(name, shouldCompareDynamicColor = false) {
            Surface {
                Column(Modifier.padding(12.dp)) { content() }
            }
        }
    }
}
