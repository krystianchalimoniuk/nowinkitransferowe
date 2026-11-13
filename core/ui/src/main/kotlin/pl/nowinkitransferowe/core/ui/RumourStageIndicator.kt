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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.nowinkitransferowe.core.designsystem.component.NtStageProgress
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors
import pl.nowinkitransferowe.core.model.RumourStage
import pl.nowinkitransferowe.core.model.RumourTrend

/**
 * The stage of a rumour as a wavy line with its name, "3/5" and an arrow when it moved up or down
 * since the previous stage.
 */
@Composable
fun RumourStageIndicator(stage: RumourStage, trend: RumourTrend, modifier: Modifier = Modifier) {
    val stages = RumourStage.entries.size
    NtStageProgress(
        stage = stage.level,
        stages = stages,
        color = LocalNtColors.current.rumour,
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            when (trend) {
                RumourTrend.UP -> Icon(
                    NtIcons.ArrowUpward,
                    contentDescription = stringResource(R.string.core_ui_stage_up),
                    tint = LocalNtColors.current.success,
                    modifier = Modifier.size(14.dp),
                )
                RumourTrend.DOWN -> Icon(
                    NtIcons.ArrowDownward,
                    contentDescription = stringResource(R.string.core_ui_stage_down),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                RumourTrend.SAME, RumourTrend.NEW -> Unit
            }
            Text(stageName(stage), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "· " + stringResource(R.string.core_ui_stage_of, stage.level, stages),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun stageName(stage: RumourStage): String = stringResource(
    when (stage) {
        RumourStage.SPECULATION -> R.string.core_ui_stage_speculation
        RumourStage.INTEREST -> R.string.core_ui_stage_interest
        RumourStage.TALKS -> R.string.core_ui_stage_talks
        RumourStage.CLOSE -> R.string.core_ui_stage_close
        RumourStage.AGREEMENT -> R.string.core_ui_stage_agreement
    },
)
