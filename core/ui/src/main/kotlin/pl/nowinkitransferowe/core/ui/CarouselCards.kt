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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.nowinkitransferowe.core.designsystem.component.NtPlayerPhoto
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.cardContainer
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.UserTransfer

private val CarouselCardWidth = 168.dp

/** A transfer in a carousel: the player, the fee and the route from club to club. */
@Composable
fun TransferCarouselCard(
    transfer: UserTransfer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CarouselCard(onClick, modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NtPlayerPhoto(photoUrl = transfer.player.photoUrl, name = transfer.player.name, size = 32.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(transfer.player.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                FeeLabel(transfer.fee)
            }
        }
        ClubRoute(transfer.from, transfer.to)
    }
}

/** A rumour in a carousel: the player, the club he is linked with and the stage. */
@Composable
fun RumourCarouselCard(
    rumour: Rumour,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CarouselCard(onClick, modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NtPlayerPhoto(photoUrl = rumour.player.photoUrl, name = rumour.player.name, size = 32.dp)
            Column {
                Text(rumour.player.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    rumour.to.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        RumourStageIndicator(rumour.stage, rumour.trend)
    }
}

@Composable
private fun CarouselCard(onClick: () -> Unit, modifier: Modifier, content: @Composable () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.cardContainer),
        modifier = modifier.width(CarouselCardWidth),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
    }
}

/** "[crest] → [crest] Liverpool FC": where a player moves. */
@Composable
private fun ClubRoute(from: ClubRef?, to: ClubRef) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (from != null) ClubCrest(from, size = 20.dp)
        Icon(NtIcons.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        ClubCrest(to, size = 20.dp)
        Text(
            to.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
