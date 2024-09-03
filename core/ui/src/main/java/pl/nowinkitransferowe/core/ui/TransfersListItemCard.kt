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

package pl.nowinkitransferowe.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TransfersResourceTransferListLabel(
                        label = userTransferResource.season,
                        modifier = Modifier.weight(.12f),
                    )
                    TransfersResourceTransferListLabel(
                        label = dateFormatted(publishDate = userTransferResource.publishDate),
                        modifier = Modifier.weight(.14f),
                    )
                    Row(
                        modifier = Modifier.weight(.32f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TransfersResourceImage(
                            userTransferResource.clubFromImg,
                            pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_crest_placeholder,
                            16.dp,
                            0.dp,
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        TransfersResourceTransferListLabel(label = userTransferResource.clubFrom)
                    }
                    Row(
                        modifier = Modifier.weight(.32f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TransfersResourceImage(
                            userTransferResource.clubToImg,
                            pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_crest_placeholder,
                            16.dp,
                            0.dp,
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        TransfersResourceTransferListLabel(label = userTransferResource.clubTo)
                    }
                    TransfersResourceTransferListLabel(
                        label = shortPriceValueIfNoInformationOrOnLoan(userTransferResource.price),
                        modifier = Modifier.weight(.1f),
                    )
                }
            }
        }
    }
}

private fun shortPriceValueIfNoInformationOrOnLoan(price: String): String {
    return if (price == "wypożyczenie") {
        "wyp."
    } else if (price == "nie ujawniono") {
        "-"
    } else if (price == "za darmo") {
        "0"
    } else {
        price.split(" ")[0]
    }
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
                    onClick = {},
                )
            }
        }
    }
}
