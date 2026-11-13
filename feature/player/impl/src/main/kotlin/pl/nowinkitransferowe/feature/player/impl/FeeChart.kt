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

package pl.nowinkitransferowe.feature.player.impl

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.toJavaLocalDate
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.ui.ClubCrest
import pl.nowinkitransferowe.core.ui.millionsLabel
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * The fees of a player's transfers over time as on the website: a line with points placed by
 * date, free transfers and loans at 0, a grid up to a round maximum, the highest fee above its
 * point and, below the axis, the month and the new club's crest where they fit.
 */
@Composable
internal fun FeeChart(transfers: List<UserTransfer>, modifier: Modifier = Modifier) {
    val points = transfers.sortedWith(compareBy<UserTransfer> { it.date }.thenBy { it.id })
    val gridMax = niceMax(points.maxOf { it.fee.countedMillions })
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = MaterialTheme.typography.labelSmall
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val summary = points.joinToString("; ") { "${it.date.toJavaLocalDate().format(MONTH_YEAR)}: ${it.fee.label}" }

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(ChartHeight + AxisLabelsHeight)
            .semantics { contentDescription = summary }
            .testTag("player:fees-chart"),
    ) {
        val density = LocalDensity.current
        val width = maxWidth
        val plotWidth = width - YAxisWidth - EdgePadding * 2
        val firstDay = points.first().date.toEpochDays()
        val lastDay = points.last().date.toEpochDays()
        val xs: List<Dp> = points.map { transfer ->
            val fraction = if (lastDay == firstDay) 0.5f else (transfer.date.toEpochDays() - firstDay).toFloat() / (lastDay - firstDay)
            YAxisWidth + EdgePadding + plotWidth * fraction
        }
        fun y(millions: Double): Dp = TopPadding + (ChartHeight - TopPadding) * (1 - (millions / gridMax).toFloat())

        Canvas(Modifier.fillMaxWidth().height(ChartHeight)) {
            // The grid: 0 and four steps up to the round maximum
            (0..GRID_STEPS).forEach { step ->
                val gy = y(gridMax * step / GRID_STEPS).toPx()
                drawLine(gridColor, Offset(YAxisWidth.toPx(), gy), Offset(size.width, gy), strokeWidth = 1.dp.toPx())
            }
            val offsets = points.mapIndexed { index, transfer -> Offset(xs[index].toPx(), y(transfer.fee.countedMillions).toPx()) }
            val bottom = y(0.0).toPx()
            val area = Path().apply {
                moveTo(offsets.first().x, bottom)
                offsets.forEach { lineTo(it.x, it.y) }
                lineTo(offsets.last().x, bottom)
                close()
            }
            drawPath(area, lineColor.copy(alpha = 0.12f))
            val line = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                offsets.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(line, lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            offsets.forEach { drawCircle(lineColor, radius = 4.5.dp.toPx(), center = it) }
        }

        // The values of the grid lines on the left
        (0..GRID_STEPS).forEach { step ->
            Text(
                formatMillions(gridMax * step / GRID_STEPS),
                style = labelStyle,
                color = labelColor,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(YAxisWidth - 6.dp)
                    .offset(y = y(gridMax * step / GRID_STEPS) - with(density) { labelStyle.lineHeight.toDp() } / 2),
            )
        }

        // The highest fee above its point
        val top = points.indices.maxBy { points[it].fee.countedMillions }
        Text(
            millionsLabel(points[top].fee.countedMillions),
            style = labelStyle,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(LabelWidth)
                .offset(x = xs[top] - LabelWidth / 2, y = y(points[top].fee.countedMillions) - 20.dp),
        )

        // Months and crests below the axis, skipping the ones too close to the previous label
        var lastLabelX: Dp? = null
        points.forEachIndexed { index, transfer ->
            val x = xs[index]
            if (lastLabelX != null && x - lastLabelX!! < LabelWidth) return@forEachIndexed
            lastLabelX = x
            Column(
                Modifier
                    .width(LabelWidth)
                    .offset(x = x - LabelWidth / 2, y = ChartHeight + 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(transfer.date.toJavaLocalDate().format(SHORT_MONTH_YEAR), style = labelStyle, color = labelColor, maxLines = 1)
                Box(Modifier.height(4.dp))
                ClubCrest(transfer.to, size = 22.dp)
            }
        }
    }
}

/** A round top for the grid: four equal round steps, for example 0, 12.5, 25, 37.5 and 50. */
internal fun niceMax(max: Double): Double {
    if (max <= 0.0) return 1.0
    val rawStep = max / GRID_STEPS
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val step = ROUND_STEPS.first { it * magnitude >= rawStep * (1 - 1e-9) } * magnitude
    return step * GRID_STEPS
}

private val ROUND_STEPS = listOf(1.0, 1.25, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 7.5, 10.0)

/** "12,5", "50", "0,38": the grid values in millions, with a Polish decimal comma. */
private fun formatMillions(value: Double): String {
    val rounded = if (value == floor(value)) value.toLong().toString() else "%.2f".format(Locale.ROOT, value).trimEnd('0').trimEnd('.')
    return rounded.replace('.', ',')
}

private const val GRID_STEPS = 4
private val ChartHeight = 180.dp
private val AxisLabelsHeight = 48.dp
private val TopPadding = 24.dp
private val YAxisWidth = 40.dp
private val EdgePadding = 16.dp
private val LabelWidth = 52.dp

private val POLISH = Locale.forLanguageTag("pl")
private val MONTH_YEAR = DateTimeFormatter.ofPattern("LLL yyyy", POLISH)
private val SHORT_MONTH_YEAR = DateTimeFormatter.ofPattern("LLL yy", POLISH)
