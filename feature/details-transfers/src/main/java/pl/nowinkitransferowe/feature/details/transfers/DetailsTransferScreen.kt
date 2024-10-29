/*
 * Copyright 2024 The Android Open Source Project
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

package pl.nowinkitransferowe.feature.details.transfers

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtBackground
import pl.nowinkitransferowe.core.designsystem.component.NtGradientBackground
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.ui.DevicePreviews
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransferListItemCard
import pl.nowinkitransferowe.core.ui.UserTransfersResourcePreviewParameterProvider
import pl.nowinkitransferowe.core.ui.dateFormatted
import pl.nowinkitransferowe.feature.details.transfers.Util.calculateMaxTransferValue
import pl.nowinkitransferowe.feature.details.transfers.Util.calculateTransferSum
import pl.nowinkitransferowe.feature.details.transfers.Util.hasMoreThanOrEqualTwoCashTransfers
import pl.nowinkitransferowe.feature.details.transfers.Util.priceToFloat
import pl.nowinkitransferowe.feature.details.transfers.Util.shortcutDate
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DetailsTransferRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsTransferViewModel = hiltViewModel(),
) {
    val detailsTransferUiState by viewModel.detailsTransferUiState.collectAsStateWithLifecycle()
    TrackScreenViewEvent(screenName = "Transfer: ${viewModel.transferResourceId}")
    NtBackground {
        NtGradientBackground {
            DetailsTransferScreen(
                detailsTransferUiState = detailsTransferUiState,
                showBackButton = showBackButton,
                onBackClick = onBackClick,
                modifier = modifier.testTag("transfer:${viewModel.transferResourceId}"),
            )
        }
    }
}

@VisibleForTesting
@Composable
fun DetailsTransferScreen(
    detailsTransferUiState: DetailsTransferUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()
    TrackScrollJank(scrollableState = state, stateName = "details-transfers:screen")

    Box(
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            when (detailsTransferUiState) {
                DetailsTransferUiState.Loading ->
                    NtLoadingWheel(
                        modifier = modifier,
                        contentDesc = stringResource(id = R.string.feature_details_transfers_loading),
                    )

                DetailsTransferUiState.Error -> {
                    LaunchedEffect(Unit) {
                        onBackClick()
                    }
                }

                is DetailsTransferUiState.Success -> {
                    DetailsTransferToolbar(
                        showBackButton = showBackButton,
                        onBackClick = onBackClick,
                    )

                    DetailsTransferBody(
                        userTransferResources = detailsTransferUiState.userTransferResource,
                        dataPoints = detailsTransferUiState.dataPoints,
                        state = state,
                    )
                }
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
    }
}

@Composable
fun DetailsTransferBody(
    userTransferResources: List<UserTransferResource>,
    dataPoints: List<DataPoint>,
    state: LazyListState,
) {
    Column(modifier = Modifier.testTag("content")) {
        Box(
            modifier = Modifier.padding(8.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .testTag("details_transfers:feed"),
                state = state,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                item {
                    Column {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Image(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(RoundedCornerShape(80.dp)),
                                painter =
                                painterResource(pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_transfer_face),
                                contentDescription = "Footballer photo placeholder",
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            TransfersResourceFootballerName(
                                name = userTransferResources.first().name,
                                modifier = Modifier.widthIn(180.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_season),
                                modifier = Modifier.weight(.12f),
                            )
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_date),
                                modifier = Modifier.weight(.14f),
                            )
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_club_from),
                                modifier = Modifier.weight(.32f),
                            )
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_club_to),
                                modifier = Modifier.weight(.32f),
                            )
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_price),
                                modifier = Modifier.weight(.1f),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(
                    items = userTransferResources,
                    key = { it.id },
                    contentType = { "transferListItem" },
                ) { userTransferResource ->
                    TransferListItemCard(
                        userTransferResource,
                        onClick = {},
                    )
                }
                item {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 8.dp),
                        ) {
                            TransfersResourceTransferListLabel(label = stringResource(id = R.string.feature_details_transfers_list_label_total_transfer_amount))
                            TransfersResourceTransferListLabel(
                                label = calculateTransferSum(
                                    userTransferResources.map { it.price },
                                ),
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (hasMoreThanOrEqualTwoCashTransfers(userTransferResources.map { it.price })) {
                            TransfersResourceChartTitle(
                                label = stringResource(R.string.feature_details_transfers_transfers_value),
                                Modifier.padding(horizontal = 8.dp),
                            )
                            TransfersResourceMaxTransferValue(
                                label = stringResource(R.string.feature_details_transfers_max_transfer_value),
                                value = calculateMaxTransferValue(prices = userTransferResources.map { it.price }),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 25.dp, end = 16.dp),
                            )

                            Spacer(modifier = Modifier.height(60.dp))
                            LineChart(
                                data =
                                dataPoints,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .padding(horizontal = 8.dp),
                            )

                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransfersResourceFootballerName(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        HtmlCompat.fromHtml(name, HtmlCompat.FROM_HTML_MODE_COMPACT).toString(),
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier,
        maxLines = 1,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun TransfersResourceTransferListLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier,
        maxLines = 2,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun TransfersResourceChartTitle(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        label,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
        maxLines = 1,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun TransfersResourceMaxTransferValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Text(
        buildAnnotatedString {
            append(label)
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(value)
            }
        },
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
        maxLines = 2,
        textAlign = TextAlign.End,
    )
}

@Composable
private fun DetailsTransferToolbar(
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
    ) {
        if (showBackButton) {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = NtIcons.ArrowBack,
                    contentDescription = stringResource(
                        id = pl.nowinkitransferowe.core.ui.R.string.core_ui_back,
                    ),
                )
            }
        } else {
            // Keeps the NiaFilterChip aligned to the end of the Row.
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: List<DataPoint> = emptyList(),
) {
    val defaultBitmap =
        ImageBitmap.imageResource(id = pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_crest_placeholder)
            .asAndroidBitmap()
    val defaultScaledBitmap = Bitmap.createScaledBitmap(defaultBitmap, 80, 80, false)
    val spacingFromLeft = 80f
    val graphColor = MaterialTheme.colorScheme.onPrimaryContainer // color for your graph
    val transparentGraphColor = remember { graphColor.copy(alpha = 0.5f) }
    val upperValue = remember { (data.maxOfOrNull { it.price })?.roundToInt() ?: 0 }
    val lowerValue = remember { 0 }
    val density = LocalDensity.current
    val materialColor = MaterialTheme.colorScheme.inverseSurface
    // paint for the text shown in data values
    val textPaint = remember(density) {
        Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = materialColor.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = density.run { 14.sp.toPx() }
        }
    }

    Canvas(modifier = modifier) {
        val spacePerData = (size.width - spacingFromLeft) / data.size

        // loop through each index by step of 1
        // data shown horizontally
        (data.indices step 1).forEach { i ->
            val date = data[i].date
            val bitmap = data[i].bitmap ?: defaultScaledBitmap
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    date,
                    spacingFromLeft + i * spacePerData,
                    size.height,
                    textPaint,

                )
                drawImage(
                    image = bitmap.asImageBitmap(),
                    topLeft = Offset(
                        (spacingFromLeft + i * spacePerData) - (bitmap.width / 2),
                        size.height + 30,
                    ),
                )
            }
        }

        val priceStep = (upperValue - lowerValue) / 5f
        // data shown vertically
        (0..5).forEachIndexed { index, i ->
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    String.format(
                        Locale.forLanguageTag("pl"),
                        "%.1f",
                        (lowerValue + priceStep * i),
                    ),
                    30f,
                    size.height - spacingFromLeft - i * size.height / 5f,
                    textPaint,
                )
                if (index == 5) {
                    drawText(
                        "[mln €]",
                        30f,
                        0f - spacingFromLeft - 80,
                        textPaint,
                    )
                }
            }
        }

        // Vertical line
        drawLine(
            start = Offset(spacingFromLeft, size.height - spacingFromLeft),
            end = Offset(spacingFromLeft, 0f - spacingFromLeft - 50),
            color = materialColor,
            strokeWidth = 4f,
        )

        // Horizontal line
        drawLine(
            start = Offset(spacingFromLeft, size.height - spacingFromLeft),
            end = Offset(size.width - 40f, size.height - spacingFromLeft),
            color = materialColor,
            strokeWidth = 4f,
        )

        // Use this to show straight line path
        val straightLinePath = Path().apply {
            val height = size.height

            // loop through index only not value
            data.indices.forEach { i ->
                val info = data[i]
                val x1 = spacingFromLeft + i * spacePerData
                val y1 =
                    (upperValue - info.price) / upperValue * height - spacingFromLeft

                if (i == 0) {
                    moveTo(x1, y1)
                }
                lineTo(x1, y1)
                drawCircle(
                    color = materialColor,
                    radius = 5f,
                    center = Offset(x1, y1),
                ) // Uncomment it to see the end points
            }
        }

        // Use this to show curved path
        var medX: Float
        var medY: Float
        Path().apply {
            val height = size.height
            data.indices.forEach { i ->
                val nextInfo = data.getOrNull(i + 1) ?: data.last()

                val x1 = spacingFromLeft + i * spacePerData
                val y1 =
                    (upperValue - data[i].price) / upperValue * height - spacingFromLeft
                val x2 = spacingFromLeft + (i + 1) * spacePerData
                val y2 =
                    (upperValue - nextInfo.price) / upperValue * height - spacingFromLeft
                if (i == 0) {
                    moveTo(x1, y1)
                } else {
                    medX = (x1 + x2) / 2f
                    medY = (y1 + y2) / 2f
                    quadraticTo(x1 = x1, y1 = y1, x2 = medX, y2 = medY)
                }

                // drawCircle(color = Color.White, radius = 5f, center = Offset(x1,y1))
                // drawCircle(color = Color.Magenta, radius = 9f, center = Offset(medX,medY))
                // drawCircle(color = Color.Blue, radius = 7f, center = Offset(x2,y2))  //Uncomment these to see the control Points
            }
        }

        // Now draw path on canvas
        drawPath(
            path = straightLinePath,
            color = graphColor,
            style = Stroke(
                width = 1.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )

        // To show the background transparent gradient
        val fillPath =
            android.graphics.Path(straightLinePath.asAndroidPath()).asComposePath().apply {
                lineTo(size.width - spacePerData, size.height - spacingFromLeft)
                lineTo(spacingFromLeft, size.height - spacingFromLeft)
                close()
            }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    transparentGraphColor,
                    Color.Transparent,
                ),
                endY = size.height - spacingFromLeft,
            ),
        )
    }
}

@DevicePreviews
@Composable
fun DetailsScreenPopulated(
    @PreviewParameter(UserTransfersResourcePreviewParameterProvider::class)
    userTransferResources: List<UserTransferResource>,
) {
    NtTheme {
        NtBackground {
            DetailsTransferScreen(
                detailsTransferUiState = DetailsTransferUiState.Success(
                    userTransferResources,
                    userTransferResources.sortedBy { it.publishDate }.map {
                        DataPoint(
                            date = shortcutDate(
                                dateFormatted(
                                    publishDate = it.publishDate,
                                ),
                            ),
                            price = priceToFloat(it.price),
                            bitmap = null,
                        )
                    },
                ),
                showBackButton = true,
                onBackClick = {},
            )
        }
    }
}

@DevicePreviews
@Composable
fun DetailsScreenLoading() {
    NtTheme {
        NtBackground {
            DetailsTransferScreen(
                detailsTransferUiState = DetailsTransferUiState.Loading,
                showBackButton = true,
                onBackClick = {},
            )
        }
    }
}
