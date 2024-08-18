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

package pl.nowinkitransferowe.core.designsystem

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.nowinkitransferowe.core.designsystem.theme.BackgroundTheme
import pl.nowinkitransferowe.core.designsystem.theme.DarkDefaultColorScheme
import pl.nowinkitransferowe.core.designsystem.theme.GradientColors
import pl.nowinkitransferowe.core.designsystem.theme.LightDefaultColorScheme
import pl.nowinkitransferowe.core.designsystem.theme.LocalBackgroundTheme
import pl.nowinkitransferowe.core.designsystem.theme.LocalGradientColors
import pl.nowinkitransferowe.core.designsystem.theme.LocalTintTheme
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.designsystem.theme.TintTheme
import pl.nowinkitransferowe.core.designsystem.theme.darkScheme
import pl.nowinkitransferowe.core.designsystem.theme.lightScheme

/**
 * Tests [NtTheme] using different combinations of the theme mode parameters:
 * darkTheme, disableDynamicTheming, and androidTheme.
 *
 * It verifies that the various composition locals — [MaterialTheme], [LocalGradientColors] and
 * [LocalBackgroundTheme] — have the expected values for a given theme mode, as specified by the
 * design system.
 */
@RunWith(RobolectricTestRunner::class)
class ThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun darkThemeFalse_dynamicColorFalse_androidThemeFalse() {
        composeTestRule.setContent {
            NtTheme(
                darkTheme = false,
                disableDynamicTheming = true,
            ) {
                val colorScheme = lightScheme
                assertColorSchemesEqual(colorScheme, MaterialTheme.colorScheme)
                val gradientColors = defaultGradientColors(colorScheme)
                kotlin.test.assertEquals(gradientColors, LocalGradientColors.current)
                val backgroundTheme = defaultBackgroundTheme(colorScheme)
                kotlin.test.assertEquals(backgroundTheme, LocalBackgroundTheme.current)
                val tintTheme = dynamicTintThemeWithFallback(colorScheme)
                kotlin.test.assertEquals(tintTheme, LocalTintTheme.current)
            }
        }
    }

    @Test
    fun darkThemeTrue_dynamicColorFalse_androidThemeFalse() {
        composeTestRule.setContent {
            NtTheme(
                darkTheme = true,
                disableDynamicTheming = true,
            ) {
                val colorScheme = darkScheme
                assertColorSchemesEqual(colorScheme, MaterialTheme.colorScheme)
                val gradientColors = defaultGradientColors(colorScheme)
                kotlin.test.assertEquals(gradientColors, LocalGradientColors.current)
                val backgroundTheme = defaultBackgroundTheme(colorScheme)
                kotlin.test.assertEquals(backgroundTheme, LocalBackgroundTheme.current)
                val tintTheme = dynamicTintThemeWithFallback(colorScheme)
                kotlin.test.assertEquals(tintTheme, LocalTintTheme.current)
            }
        }
    }

    @Test
    fun darkThemeFalse_dynamicColorTrue_androidThemeFalse() {
        composeTestRule.setContent {
            NtTheme(
                darkTheme = false,
                disableDynamicTheming = false,
            ) {
                val colorScheme = dynamicLightColorSchemeWithFallback()
                assertColorSchemesEqual(colorScheme, MaterialTheme.colorScheme)
                val gradientColors = dynamicGradientColorsWithFallback(colorScheme)
                kotlin.test.assertEquals(gradientColors, LocalGradientColors.current)
                val backgroundTheme = defaultBackgroundTheme(colorScheme)
                kotlin.test.assertEquals(backgroundTheme, LocalBackgroundTheme.current)
                val tintTheme = dynamicTintThemeWithFallback(colorScheme)
                kotlin.test.assertEquals(tintTheme, LocalTintTheme.current)
            }
        }
    }

    @Test
    fun darkThemeTrue_dynamicColorTrue_androidThemeFalse() {
        composeTestRule.setContent {
            NtTheme(
                darkTheme = true,
                disableDynamicTheming = false,
            ) {
                val colorScheme = dynamicDarkColorSchemeWithFallback()
                assertColorSchemesEqual(colorScheme, MaterialTheme.colorScheme)
                val gradientColors = dynamicGradientColorsWithFallback(colorScheme)
                kotlin.test.assertEquals(gradientColors, LocalGradientColors.current)
                val backgroundTheme = defaultBackgroundTheme(colorScheme)
                kotlin.test.assertEquals(backgroundTheme, LocalBackgroundTheme.current)
                val tintTheme = dynamicTintThemeWithFallback(colorScheme)
                kotlin.test.assertEquals(tintTheme, LocalTintTheme.current)
            }
        }
    }

    @Composable
    private fun dynamicLightColorSchemeWithFallback(): ColorScheme = when {
        SDK_INT >= VERSION_CODES.S -> dynamicLightColorScheme(LocalContext.current)
        else -> LightDefaultColorScheme
    }

    @Composable
    private fun dynamicDarkColorSchemeWithFallback(): ColorScheme = when {
        SDK_INT >= VERSION_CODES.S -> dynamicDarkColorScheme(LocalContext.current)
        else -> DarkDefaultColorScheme
    }

    private fun emptyGradientColors(colorScheme: ColorScheme): GradientColors =
        GradientColors(container = colorScheme.surfaceColorAtElevation(2.dp))

    private fun defaultGradientColors(colorScheme: ColorScheme): GradientColors = GradientColors(
        top = colorScheme.inverseOnSurface,
        bottom = colorScheme.primaryContainer,
        container = colorScheme.surface,
    )

    private fun dynamicGradientColorsWithFallback(colorScheme: ColorScheme): GradientColors = when {
        SDK_INT >= VERSION_CODES.S -> emptyGradientColors(colorScheme)
        else -> defaultGradientColors(colorScheme)
    }

    private fun defaultBackgroundTheme(colorScheme: ColorScheme): BackgroundTheme = BackgroundTheme(
        color = colorScheme.surface,
        tonalElevation = 2.dp,
    )

    private fun defaultTintTheme(): TintTheme = TintTheme()

    private fun dynamicTintThemeWithFallback(colorScheme: ColorScheme): TintTheme = when {
        SDK_INT >= VERSION_CODES.S -> TintTheme(colorScheme.primary)
        else -> TintTheme()
    }

    /**
     * Workaround for the fact that the NiA design system specify all color scheme values.
     */
    private fun assertColorSchemesEqual(
        expectedColorScheme: ColorScheme,
        actualColorScheme: ColorScheme,
    ) {
        kotlin.test.assertEquals(expectedColorScheme.primary, actualColorScheme.primary)
        kotlin.test.assertEquals(expectedColorScheme.onPrimary, actualColorScheme.onPrimary)
        kotlin.test.assertEquals(
            expectedColorScheme.primaryContainer,
            actualColorScheme.primaryContainer,
        )
        kotlin.test.assertEquals(
            expectedColorScheme.onPrimaryContainer,
            actualColorScheme.onPrimaryContainer,
        )
        kotlin.test.assertEquals(expectedColorScheme.secondary, actualColorScheme.secondary)
        kotlin.test.assertEquals(expectedColorScheme.onSecondary, actualColorScheme.onSecondary)
        kotlin.test.assertEquals(
            expectedColorScheme.secondaryContainer,
            actualColorScheme.secondaryContainer,
        )
        kotlin.test.assertEquals(
            expectedColorScheme.onSecondaryContainer,
            actualColorScheme.onSecondaryContainer,
        )
        kotlin.test.assertEquals(expectedColorScheme.tertiary, actualColorScheme.tertiary)
        kotlin.test.assertEquals(expectedColorScheme.onTertiary, actualColorScheme.onTertiary)
        kotlin.test.assertEquals(
            expectedColorScheme.tertiaryContainer,
            actualColorScheme.tertiaryContainer,
        )
        kotlin.test.assertEquals(
            expectedColorScheme.onTertiaryContainer,
            actualColorScheme.onTertiaryContainer,
        )
        kotlin.test.assertEquals(expectedColorScheme.error, actualColorScheme.error)
        kotlin.test.assertEquals(expectedColorScheme.onError, actualColorScheme.onError)
        kotlin.test.assertEquals(
            expectedColorScheme.errorContainer,
            actualColorScheme.errorContainer,
        )
        kotlin.test.assertEquals(
            expectedColorScheme.onErrorContainer,
            actualColorScheme.onErrorContainer,
        )
        kotlin.test.assertEquals(expectedColorScheme.background, actualColorScheme.background)
        kotlin.test.assertEquals(expectedColorScheme.onBackground, actualColorScheme.onBackground)
        kotlin.test.assertEquals(expectedColorScheme.surface, actualColorScheme.surface)
        kotlin.test.assertEquals(expectedColorScheme.onSurface, actualColorScheme.onSurface)
        kotlin.test.assertEquals(
            expectedColorScheme.surfaceVariant,
            actualColorScheme.surfaceVariant,
        )
        kotlin.test.assertEquals(
            expectedColorScheme.onSurfaceVariant,
            actualColorScheme.onSurfaceVariant,
        )
        kotlin.test.assertEquals(
            expectedColorScheme.inverseSurface,
            actualColorScheme.inverseSurface,
        )
        kotlin.test.assertEquals(
            expectedColorScheme.inverseOnSurface,
            actualColorScheme.inverseOnSurface,
        )
        kotlin.test.assertEquals(expectedColorScheme.outline, actualColorScheme.outline)
    }
}
