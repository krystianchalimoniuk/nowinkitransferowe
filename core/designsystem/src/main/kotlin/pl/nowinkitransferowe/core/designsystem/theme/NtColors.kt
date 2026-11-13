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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colours with a meaning on the website: an official transfer, a loan, money in and out, a
 * rumour. They stay the same with dynamic colours, so a free transfer is always green.
 */
@Immutable
data class NtColors(
    val success: Color,
    val onSuccess: Color,
    val loan: Color,
    val onLoan: Color,
    val moneyIn: Color,
    val moneyOut: Color,
    val rumour: Color,
    val rumourContainer: Color,
    val onRumourContainer: Color,
)

internal val LightNtColors = NtColors(
    success = Color(0xFF16803F),
    onSuccess = Color(0xFFFFFFFF),
    loan = Color(0xFF2453B8),
    onLoan = Color(0xFFFFFFFF),
    moneyIn = Color(0xFF2DA35A),
    moneyOut = Color(0xFFA8161F),
    rumour = Color(0xFF7A4A00),
    rumourContainer = Color(0xFFFFEFCF),
    onRumourContainer = Color(0xFF2A1700),
)

internal val DarkNtColors = NtColors(
    success = Color(0xFF5CC983),
    onSuccess = Color(0xFF0B2914),
    loan = Color(0xFF9DB8FF),
    onLoan = Color(0xFF0B0D0F),
    moneyIn = Color(0xFF3AAA64),
    moneyOut = Color(0xFFC9363E),
    rumour = Color(0xFFF7C25C),
    rumourContainer = Color(0xFF3A2A0C),
    onRumourContainer = Color(0xFFFFDEA6),
)

val LocalNtColors = staticCompositionLocalOf { LightNtColors }
