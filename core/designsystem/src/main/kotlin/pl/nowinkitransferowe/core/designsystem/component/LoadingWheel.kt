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

package pl.nowinkitransferowe.core.designsystem.component

import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme

/**
 * The loading indicator of the app: Material 3 Expressive's [LoadingIndicator], a shape that
 * morphs while it turns. Keeps the name and the "loadingWheel" test tag of the wheel it replaced.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NtLoadingWheel(
    contentDesc: String,
    modifier: Modifier = Modifier,
) {
    LoadingIndicator(
        modifier = modifier
            .semantics { contentDescription = contentDesc }
            .testTag("loadingWheel"),
    )
}

/** [NtLoadingWheel] in its own container, for loading on top of content. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NtOverlayLoadingWheel(
    contentDesc: String,
    modifier: Modifier = Modifier,
) {
    ContainedLoadingIndicator(
        modifier = modifier
            .semantics { contentDescription = contentDesc }
            .testTag("loadingWheel"),
    )
}

@ThemePreviews
@Composable
fun NtLoadingWheelPreview() {
    NtTheme {
        Surface {
            NtLoadingWheel(contentDesc = "LoadingWheel")
        }
    }
}

@ThemePreviews
@Composable
fun NtOverlayLoadingWheelPreview() {
    NtTheme {
        Surface {
            NtOverlayLoadingWheel(contentDesc = "LoadingWheel")
        }
    }
}
