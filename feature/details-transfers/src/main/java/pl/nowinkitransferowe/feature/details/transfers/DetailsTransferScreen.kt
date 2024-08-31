package pl.nowinkitransferowe.feature.details.transfers

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtBackground
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.ui.DevicePreviews
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransferListItemCard
import pl.nowinkitransferowe.core.ui.UserTransfersResourcePreviewParameterProvider
import androidx.compose.foundation.lazy.items

@Composable
fun DetailsTransferRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsTransferViewModel = hiltViewModel(),
) {
    val detailsTransferUiState by viewModel.detailsTransferUiState.collectAsStateWithLifecycle()
    TrackScreenViewEvent(screenName = "Transfer: ${viewModel.transferId}")
    DetailsTransferScreen(
        detailsTransferUiState = detailsTransferUiState,
        showBackButton = showBackButton,
        onBackClick = onBackClick,
        onBookmarkChanged = viewModel::bookmarkTransfer,
        modifier = modifier.testTag("transfer:${viewModel.transferId}"),
    )
}

@VisibleForTesting
@Composable
fun DetailsTransferScreen(
    detailsTransferUiState: DetailsTransferUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onBookmarkChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()
    TrackScrollJank(scrollableState = state, stateName = "details-transfers:screen")

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
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
                        state = state,
                        onToggleBookmark = {
                            onBookmarkChanged(
                                detailsTransferUiState.userTransferResource.first().id,
                                !detailsTransferUiState.userTransferResource.first().isSaved,
                            )
                        },
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
    state: LazyListState,
    onToggleBookmark: () -> Unit,
) {

    Column(modifier = Modifier.testTag("content")) {
        Box(
            modifier = Modifier.padding(16.dp),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .testTag("bookmarks:feed"),
                state = state,
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
                        ) {
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_season),
                                modifier = Modifier.weight(.166f),
                            )
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_date),
                                modifier = Modifier.weight(.166f),
                            )
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_club_from),
                                modifier = Modifier.weight(.25f),
                            )
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_club_to),
                                modifier = Modifier.weight(.25f),
                            )
                            TransfersResourceTransferListLabel(
                                label = stringResource(id = R.string.feature_details_transfers_list_label_price),
                                modifier = Modifier.weight(.166f),
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
                        hasBeenViewed = userTransferResource.hasBeenViewed,
                        isBookmarked = userTransferResource.isSaved,
                        onClick = {},
                        onToggleBookmark = {},
                    )
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        TransfersResourceTransferListLabel(label = stringResource(id = R.string.feature_details_transfers_list_label_total_transfer_amount))
                        TransfersResourceTransferListLabel(
                            label = calculateTransferSum(
                                userTransferResources.map { it.price },
                            ),
                        )

                    }
                }
            }
        }
    }
}

fun calculateTransferSum(prices: List<String>): String {
    var sum = 0.0
    prices.forEach { price ->
        if (price == "za darmo" || price == "nie ujawniono" || price == "wypożyczenie") {
            sum += 0.0
        } else {
            val value = price.split(" ")[0].replace(",", ".").toDoubleOrNull()
            if (value != null) {
                sum += value
            }
        }
    }
    return " $sum mln €"
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
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
        maxLines = 1,
        textAlign = TextAlign.Center,
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

@DevicePreviews
@Composable
fun DetailsScreenPopulated(
    @PreviewParameter(UserTransfersResourcePreviewParameterProvider::class)
    userTransferResources: List<UserTransferResource>,
) {
    NtTheme {
        NtBackground {
            DetailsTransferScreen(
                detailsTransferUiState = DetailsTransferUiState.Success(userTransferResources),
                showBackButton = true,
                onBackClick = {},
                onBookmarkChanged = { _, _ -> },
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
                onBookmarkChanged = { _, _ -> },
            )
        }
    }
}