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

package pl.nowinkitransferowe.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import pl.nowinkitransferowe.core.designsystem.R

/**
 * Barlow Condensed, the headline font of the website. Display styles use the italic, like the
 * section titles and the logo there; screens set upper case where the website does.
 */
val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
    Font(R.font.barlow_condensed_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.barlow_condensed_extrabold, FontWeight.ExtraBold),
    Font(R.font.barlow_condensed_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
)

/** Inter, the text font of the website, from one variable font file. */
val Inter = FontFamily(
    listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold).map { weight ->
        Font(R.font.inter, weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))
    },
)

private val displayStyle = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight.ExtraBold,
    fontStyle = FontStyle.Italic,
    letterSpacing = 0.sp,
)

private val headlineStyle = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = 0.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

private val textStyle = TextStyle(fontFamily = Inter)

/**
 * Material 3 type scale with the website fonts: display and headline in Barlow Condensed, the
 * rest in Inter. Sizes follow the Material scale; headlines are tighter, as the condensed font
 * is.
 */
internal val NtTypography = Typography(
    displayLarge = displayStyle.copy(fontSize = 57.sp, lineHeight = 60.sp),
    displayMedium = displayStyle.copy(fontSize = 45.sp, lineHeight = 48.sp),
    displaySmall = displayStyle.copy(fontSize = 36.sp, lineHeight = 38.sp),
    headlineLarge = headlineStyle.copy(fontSize = 32.sp, lineHeight = 34.sp),
    headlineMedium = headlineStyle.copy(fontSize = 28.sp, lineHeight = 30.sp),
    headlineSmall = headlineStyle.copy(fontSize = 24.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleLarge = textStyle.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = textStyle.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
    titleSmall = textStyle.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
    bodyLarge = textStyle.copy(fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = textStyle.copy(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = textStyle.copy(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = textStyle.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
    labelMedium = textStyle.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp),
    labelSmall = textStyle.copy(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp),
)
