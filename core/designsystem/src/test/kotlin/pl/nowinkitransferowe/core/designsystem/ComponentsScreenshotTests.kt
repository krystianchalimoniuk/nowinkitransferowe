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

package pl.nowinkitransferowe.core.designsystem

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import pl.nowinkitransferowe.core.designsystem.component.NtBadge
import pl.nowinkitransferowe.core.designsystem.component.NtConnectedButtonGroup
import pl.nowinkitransferowe.core.designsystem.component.NtCrest
import pl.nowinkitransferowe.core.designsystem.component.NtDivergingBarRow
import pl.nowinkitransferowe.core.designsystem.component.NtFlag
import pl.nowinkitransferowe.core.designsystem.component.NtLogo
import pl.nowinkitransferowe.core.designsystem.component.NtPill
import pl.nowinkitransferowe.core.designsystem.component.NtStageProgress
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors
import pl.nowinkitransferowe.core.screenshottesting.captureMultiTheme

/**
 * The 4.0 components of the design system, each in the light and dark theme. Images (crests,
 * flags) are not loaded in tests, so the placeholders show.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class, qualifiers = "w360dp-h640dp-480dpi")
@LooperMode(LooperMode.Mode.PAUSED)
class ComponentsScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun logo() = capture("Logo") { NtLogo() }

    @Test
    fun badgesAndPills() = capture("BadgesAndPills") {
        val colors = LocalNtColors.current
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            NtBadge("Oficjalnie", colors.success, colors.onSuccess)
            NtBadge("Plotka", colors.rumourContainer, colors.rumour)
            NtPill("40 mln €", MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.surface)
            NtPill("za darmo", colors.success, colors.onSuccess)
            NtPill("wypożyczenie", colors.loan, colors.onLoan)
        }
    }

    @Test
    fun crestsAndFlags() = capture("CrestsAndFlags") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            NtCrest(imageUrl = null, name = "Real Madryt", size = 18.dp)
            NtCrest(imageUrl = null, name = "Liverpool FC", size = 36.dp)
            NtCrest(imageUrl = null, name = "Arsenal", size = 64.dp)
            NtFlag(imageUrl = "https://nowinkitransferowe.pl/static/flags/pl.svg", contentDescription = "Polska")
        }
    }

    @Test
    fun stageProgress() = capture("StageProgress") {
        Column(Modifier.width(240.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            (1..5).forEach { stage ->
                NtStageProgress(stage = stage, stages = 5, color = LocalNtColors.current.rumour)
            }
        }
    }

    @Test
    fun divergingBars() = capture("DivergingBars") {
        val colors = LocalNtColors.current
        Column(Modifier.width(320.dp)) {
            NtDivergingBarRow("26/27", 45.5, 120.0, 120.0, colors.moneyIn, colors.moneyOut, "-74,5 mln €")
            NtDivergingBarRow("25/26", 80.0, 30.0, 120.0, colors.moneyIn, colors.moneyOut, "+50 mln €", trailingColor = colors.success)
        }
    }

    @Test
    fun connectedButtonGroup() = capture("ConnectedButtonGroup") {
        NtConnectedButtonGroup(
            options = listOf("Wszystko", "Oficjalnie", "Plotki"),
            selectedIndex = 1,
            onSelect = {},
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
