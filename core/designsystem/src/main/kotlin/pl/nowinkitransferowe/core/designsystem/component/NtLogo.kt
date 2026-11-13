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

package pl.nowinkitransferowe.core.designsystem.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import pl.nowinkitransferowe.core.designsystem.theme.BarlowCondensed
import pl.nowinkitransferowe.core.designsystem.theme.LogoGreen
import pl.nowinkitransferowe.core.designsystem.theme.LogoRed
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme

/**
 * The /N/ logo of the website as text: the name in Barlow Condensed between a green and a red
 * slash. The name takes the current content colour.
 */
@Composable
fun NtLogo(
    modifier: Modifier = Modifier,
    text: String = "NOWINKI TRANSFEROWE",
    color: Color = LocalContentColor.current,
    style: TextStyle = MaterialTheme.typography.titleLarge.copy(
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        letterSpacing = 0.3.sp,
    ),
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = LogoGreen)) { append("/") }
            withStyle(SpanStyle(color = color)) { append(text) }
            withStyle(SpanStyle(color = LogoRed)) { append("/") }
        },
        style = style,
        maxLines = 1,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun NtLogoPreview() {
    NtTheme { NtLogo() }
}
