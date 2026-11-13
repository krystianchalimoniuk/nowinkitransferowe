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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * One row of a two-sided bar chart around a centre axis: [left] grows to the left, [right] to the
 * right, both relative to [max]. Used for money in (left) and out (right) of a club or a league,
 * with the balance in [trailing].
 */
@Composable
fun NtDivergingBarRow(
    label: String,
    left: Double,
    right: Double,
    max: Double,
    leftColor: Color,
    rightColor: Color,
    trailing: String,
    modifier: Modifier = Modifier,
    trailingColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(44.dp))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Bar(fraction = fraction(left, max), color = leftColor, towardsStart = true, modifier = Modifier.weight(1f))
            Box(
                Modifier
                    .width(2.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            )
            Bar(fraction = fraction(right, max), color = rightColor, towardsStart = false, modifier = Modifier.weight(1f))
        }
        Text(
            trailing,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = trailingColor,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(88.dp),
        )
    }
}

@Composable
private fun Bar(fraction: Float, color: Color, towardsStart: Boolean, modifier: Modifier) {
    Box(modifier, contentAlignment = if (towardsStart) Alignment.CenterEnd else Alignment.CenterStart) {
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(10.dp)
                .background(
                    color,
                    if (towardsStart) RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp) else RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp),
                ),
        )
    }
}

private fun fraction(value: Double, max: Double): Float = if (max <= 0.0) 0f else (value / max).toFloat().coerceIn(0f, 1f)
