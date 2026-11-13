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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme

/**
 * The top app bar of the sections: [title] on the left (the logo or [NtSectionTitle]) and the
 * [actions] on the right, as in the 4.0 mockups.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NtTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = title,
        actions = actions,
        colors = colors,
        modifier = modifier.testTag("ntTopAppBar"),
    )
}

/** An icon button for [NtTopAppBar], for example search or settings. */
@Composable
fun NtTopAppBarAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A section title like on the website: Barlow Condensed, italic, upper case. */
@Composable
fun NtSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic),
        maxLines = 1,
        modifier = modifier,
    )
}

@Preview("Top App Bar")
@Composable
private fun NtTopAppBarPreview() {
    NtTheme {
        NtTopAppBar(
            title = { NtSectionTitle("Transfery") },
            actions = {
                NtTopAppBarAction(NtIcons.Search, "Szukaj", onClick = {})
                NtTopAppBarAction(NtIcons.Settings, "Ustawienia", onClick = {})
            },
        )
    }
}
