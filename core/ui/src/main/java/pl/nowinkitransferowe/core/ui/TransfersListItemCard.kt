package pl.nowinkitransferowe.core.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserTransferResource

@Composable
fun TransferListItemCard(
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.semantics {
            onClick(label = clickActionLabel, action = null)
        },
    ) {
        Column {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Absolute.SpaceAround,
                ) {
                    TransfersResourceTransferListLabel(
                        label = userTransferResource.season,
                        modifier = Modifier.weight(.166f),
                    )
                    TransfersResourceTransferListLabel(
                        label = dateFormatted(publishDate = userTransferResource.publishDate),
                        modifier = Modifier.weight(.166f),
                    )
                    Row(
                        modifier = Modifier.weight(.25f),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TransfersResourceImage(
                            userTransferResource.clubFromImg,
                            pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_crest_placeholder,
                            16.dp,
                            0.dp,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TransfersResourceTransferListLabel(label = userTransferResource.clubFrom)
                    }
                    Row(
                        modifier = Modifier.weight(.25f),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TransfersResourceImage(
                            userTransferResource.clubToImg,
                            pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_crest_placeholder,
                            16.dp,
                            0.dp,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TransfersResourceTransferListLabel(label = userTransferResource.clubTo)
                    }
                    TransfersResourceTransferListLabel(
                        label = userTransferResource.price,
                        modifier = Modifier.weight(.166f),
                    )

                }
            }
        }
    }
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

@Preview("TransfersListItemPreview")
@Composable
private fun TransferListItemPreview(
    @PreviewParameter(UserTransfersResourcePreviewParameterProvider::class)
    userTransferResources: List<UserTransferResource>,
) {
    CompositionLocalProvider(
        LocalInspectionMode provides true,
    ) {
        NtTheme {
            Surface {
                TransferListItemCard(
                    userTransferResource = userTransferResources[0],
                    isBookmarked = true,
                    hasBeenViewed = false,
                    onToggleBookmark = {},
                    onClick = {},
                )
            }
        }
    }
}
