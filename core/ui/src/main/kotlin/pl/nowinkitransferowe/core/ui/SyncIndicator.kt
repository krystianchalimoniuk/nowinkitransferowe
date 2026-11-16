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

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import pl.nowinkitransferowe.core.designsystem.component.NtOverlayLoadingWheel

/**
 * Shows that a list is loading or syncing without hiding what is already there: the loading wheel
 * only while the list has nothing to show, a thin bar at the top while a sync brings more.
 *
 * The first sync comes in batches, so the lists fill up long before it ends.
 */
@Composable
fun BoxScope.SyncIndicator(
    isLoading: Boolean,
    isSyncing: Boolean,
    hasContent: Boolean,
    contentDesc: String,
) {
    when {
        isLoading || (isSyncing && !hasContent) -> NtOverlayLoadingWheel(
            contentDesc = contentDesc,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
        )

        isSyncing -> LinearProgressIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(3.dp)
                .testTag("sync:progress"),
        )
    }
}
