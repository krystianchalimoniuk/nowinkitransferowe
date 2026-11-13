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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.nowinkitransferowe.core.designsystem.component.NtDivergingBarRow
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors

/** Money in and out of one row, in millions of euro; the balance is in minus out. */
data class SpendingRow(val label: String, val moneyIn: Double, val moneyOut: Double) {
    val balance: Double get() = moneyIn - moneyOut
}

/**
 * Spending as on the website: money in to the left of the axis, money out to the right, the
 * balance at the end, all rows on one scale.
 */
@Composable
fun SpendingBars(rows: List<SpendingRow>, modifier: Modifier = Modifier) {
    val colors = LocalNtColors.current
    val max = rows.maxOfOrNull { maxOf(it.moneyIn, it.moneyOut) } ?: 0.0
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Legend(colors.moneyIn, stringResource(R.string.core_ui_money_in))
            Legend(colors.moneyOut, stringResource(R.string.core_ui_money_out))
        }
        rows.forEach { row ->
            NtDivergingBarRow(
                label = row.label,
                left = row.moneyIn,
                right = row.moneyOut,
                max = max,
                leftColor = colors.moneyIn,
                rightColor = colors.moneyOut,
                trailing = (if (row.balance > 0) "+" else "") + millionsLabel(row.balance),
                trailingColor = if (row.balance >= 0) colors.success else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp)),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
