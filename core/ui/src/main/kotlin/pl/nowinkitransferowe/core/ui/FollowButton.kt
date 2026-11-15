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

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons

/**
 * The star that follows a club or player named [name]: filled in the primary colour while
 * [followed], an outline otherwise.
 */
@Composable
fun FollowButton(
    followed: Boolean,
    onFollowedChange: (Boolean) -> Unit,
    name: String,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = { onFollowedChange(!followed) }, modifier = modifier) {
        Icon(
            imageVector = if (followed) NtIcons.Star else NtIcons.StarBorder,
            contentDescription = stringResource(
                if (followed) R.string.core_ui_unfollow else R.string.core_ui_follow,
                name,
            ),
            tint = if (followed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
