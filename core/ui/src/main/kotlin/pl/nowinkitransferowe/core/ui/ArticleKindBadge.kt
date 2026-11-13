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
import pl.nowinkitransferowe.core.designsystem.component.NtBadge
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors
import pl.nowinkitransferowe.core.model.ArticleKind

/**
 * The kind of an article as on the website: green "Oficjalnie", amber "Plotka", or "Wideo".
 * Plain news has no badge.
 */
@Composable
fun ArticleKindBadge(kind: ArticleKind, modifier: Modifier = Modifier) {
    val colors = LocalNtColors.current
    when (kind) {
        ArticleKind.OFFICIAL -> NtBadge(stringResource(R.string.core_ui_kind_official), colors.success, colors.onSuccess, modifier)
        ArticleKind.RUMOUR -> NtBadge(stringResource(R.string.core_ui_kind_rumour), colors.rumourContainer, colors.rumour, modifier)
        ArticleKind.VIDEO -> NtBadge(
            stringResource(R.string.core_ui_kind_video),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            modifier,
        )
        ArticleKind.NEWS, ArticleKind.UNKNOWN -> Unit
    }
}
