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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pl.nowinkitransferowe.core.designsystem.component.NtPill
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors
import pl.nowinkitransferowe.core.model.Fee
import pl.nowinkitransferowe.core.model.FeeKind
import java.util.Locale

/**
 * The fee of a transfer with the website's colour for its kind: an amount on the inverse
 * background, a free transfer green, a loan blue, anything else neutral.
 */
@Composable
fun FeeLabel(fee: Fee, modifier: Modifier = Modifier) {
    val colors = LocalNtColors.current
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (fee.kind) {
        FeeKind.AMOUNT -> scheme.onSurface to scheme.surface
        FeeKind.FREE -> colors.success to colors.onSuccess
        FeeKind.LOAN -> colors.loan to colors.onLoan
        FeeKind.UNDISCLOSED, FeeKind.UNKNOWN -> scheme.surfaceContainerHighest to scheme.onSurface
    }
    NtPill(text = fee.label, containerColor = container, contentColor = content, modifier = modifier)
}

/** "45,5 mln €", "-12 mln €": millions of euro as on the website, with a decimal comma. */
@Composable
fun millionsLabel(millions: Double): String = stringResource(R.string.core_ui_millions, formatMillions(millions))

internal fun formatMillions(millions: Double): String {
    val rounded = Math.round(millions * 10) / 10.0
    val text = if (rounded == Math.floor(rounded)) rounded.toLong().toString() else String.format(Locale.ROOT, "%.1f", rounded)
    return text.replace('.', ',')
}
