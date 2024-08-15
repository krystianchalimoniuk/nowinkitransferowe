package pl.nowinkitransferowe.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserTransferResource

@Composable
fun TransferResourceCardExpanded(
    userTransferResource: UserTransferResource,
    isBookmarked: Boolean,
    hasBeenViewed: Boolean,
    onToggleBookmark: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickActionLabel = stringResource(R.string.core_ui_card_tap_action)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.semantics {
            onClick(label = clickActionLabel, action = null)
        },
    ) {
        Column {
            Box(
                modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(8.dp)
            ) {

                BookmarkButton(
                    isBookmarked = isBookmarked,
                    onClick = onToggleBookmark,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            Column(
                Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(42.dp)),
                    painter =
                    painterResource(pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_transfer_face),
                    contentDescription = "Footballer photo placeholder",
                )

                Spacer(modifier = Modifier.height(16.dp))
                TransfersResourceFootballerName(
                    name = userTransferResource.name, modifier = Modifier.widthIn(180.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(80.dp)
                    ) {
                        TransfersResourceImage(
                            userTransferResource.clubFromImg,
                            pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_crest_placeholder,
                            40.dp,
                            0.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TransfersResourceClubName(name = userTransferResource.clubFrom)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Image(
                            modifier = Modifier
                                .size(30.dp),
                            painter =
                            painterResource(pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_arrows_green_red),
                            contentDescription = "green - red arrow",
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(80.dp)
                    ) {
                        TransfersResourceImage(
                            userTransferResource.clubToImg,
                            pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_crest_placeholder,
                            40.dp,
                            0.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TransfersResourceClubName(name = userTransferResource.clubTo)
                    }

                }
                Spacer(modifier = Modifier.height(48.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransfersResourceFootballerPrice(price = userTransferResource.price)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (!hasBeenViewed) {
                        NotificationDot(
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .size(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

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
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier,
        maxLines = 1,
        textAlign = TextAlign.Center
    )
}

@Composable
fun TransfersResourceClubName(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        name,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier,
        textAlign = TextAlign.Center,
        minLines = 2,
    )
}

@Composable
fun TransfersResourceFootballerPrice(
    price: String,
    modifier: Modifier = Modifier,
) {
    Text(price, style = MaterialTheme.typography.titleSmall, modifier = modifier)
}

@Composable
fun TransfersResourceImage(
    imageUrl: String?,
    @DrawableRes placeHolderRes: Int,
    size: Dp,
    cornerShape: Dp,
) {
    val imageBaseUrl = "http://nowinkitransferowe.pl/Images/"
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val imageLoader = rememberAsyncImagePainter(
        model = "$imageBaseUrl$imageUrl",
        onState = { state ->
            isLoading = state is AsyncImagePainter.State.Loading
            isError = state is AsyncImagePainter.State.Error
        },
    )
    val isLocalInspection = LocalInspectionMode.current
    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center

    ) {
        if (isLoading) {
            // Display a progress bar while loading
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(size),
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        Image(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerShape)),
            painter = if (isError.not() && !isLocalInspection) {
                imageLoader
            } else {
                painterResource(placeHolderRes)
            },
            contentDescription = null,
        )
    }

}

@Preview("TransfersResourceCardExpanded")
@Composable
private fun ExpandedNewsResourcePreview(
    @PreviewParameter(UserTransfersResourcePreviewParameterProvider::class)
    userTransferResources: List<UserTransferResource>,
) {
    CompositionLocalProvider(
        LocalInspectionMode provides true,
    ) {
        NtTheme {
            Surface {
                TransferResourceCardExpanded(
                    userTransferResource = userTransferResources[1],
                    isBookmarked = true,
                    hasBeenViewed = false,
                    onToggleBookmark = {},
                    onClick = {},
                )
            }
        }
    }
}