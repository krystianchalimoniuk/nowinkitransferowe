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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Progress through numbered stages as a Material 3 Expressive wavy line, for example the stage of
 * a rumour (1 speculation … 5 agreement), with [label] under it. The wave stands still: it marks
 * the progress, not a running task.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NtStageProgress(
    stage: Int,
    stages: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    label: @Composable () -> Unit = {},
) {
    val progress = (stage.toFloat() / stages).coerceIn(0f, 1f)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearWavyProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = trackColor,
            waveSpeed = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(stage.toFloat(), 0f..stages.toFloat(), stages - 1) },
        )
        label()
    }
}
