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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors
import pl.nowinkitransferowe.core.screenshottesting.captureMultiTheme

/**
 * The theme itself, in the light and dark scheme: colour roles, meaning colours, the type scale
 * with the bundled fonts and the shapes.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class, qualifiers = "w420dp-h1400dp-480dpi")
@LooperMode(LooperMode.Mode.PAUSED)
class ThemeScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theme_multipleThemes() {
        composeTestRule.captureMultiTheme("Theme", shouldCompareDynamicColor = false) {
            Surface {
                ThemeSheet()
            }
        }
    }
}

@Composable
private fun ThemeSheet() {
    val colors = MaterialTheme.colorScheme
    val ntColors = LocalNtColors.current
    val type = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Kolory", style = type.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Swatch("primary", colors.primary, colors.onPrimary)
            Swatch("primaryC", colors.primaryContainer, colors.onPrimaryContainer)
            Swatch("secondaryC", colors.secondaryContainer, colors.onSecondaryContainer)
            Swatch("tertiary", colors.tertiary, colors.onTertiary)
            Swatch("tertiaryC", colors.tertiaryContainer, colors.onTertiaryContainer)
            Swatch("surfaceLow", colors.surfaceContainerLow, colors.onSurface)
            Swatch("surfaceHigh", colors.surfaceContainerHigh, colors.onSurface)
            Swatch("error", colors.error, colors.onError)
        }
        Text("Znaczenia", style = type.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Swatch("za darmo", ntColors.success, ntColors.onSuccess)
            Swatch("wyp.", ntColors.loan, ntColors.onLoan)
            Swatch("wpływy", ntColors.moneyIn, Color.White)
            Swatch("wydatki", ntColors.moneyOut, Color.White)
            Swatch("plotka", ntColors.rumourContainer, ntColors.onRumourContainer)
        }
        Text("Typografia", style = type.titleMedium)
        TypeSample("Display", type.displaySmall)
        TypeSample("PSG ZARZUCIŁO SIEĆ", type.headlineLarge)
        TypeSample("Gavi na celowniku Liverpoolu", type.headlineSmall)
        TypeSample("Okno transferowe", type.titleLarge)
        TypeSample("Fermin Lopez znalazł się na radarze francuskiego klubu.", type.bodyLarge)
        TypeSample("29 paź · La Liga", type.labelMedium)
        Text("Kształty", style = type.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShapeSample(shapes.extraSmall, "4")
            ShapeSample(shapes.small, "8")
            ShapeSample(shapes.medium, "16")
            ShapeSample(shapes.large, "20")
            ShapeSample(shapes.extraLarge, "28")
        }
    }
}

@Composable
private fun Swatch(name: String, color: Color, onColor: Color) {
    Box(
        Modifier
            .width(84.dp)
            .background(color, MaterialTheme.shapes.small)
            .padding(horizontal = 6.dp, vertical = 10.dp),
    ) {
        Text(name, color = onColor, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TypeSample(text: String, style: TextStyle) {
    Text(text, style = style)
}

@Composable
private fun ShapeSample(shape: Shape, label: String) {
    Box(
        Modifier
            .size(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
    }
}
