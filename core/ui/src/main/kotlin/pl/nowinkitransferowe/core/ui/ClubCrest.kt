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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.nowinkitransferowe.core.designsystem.component.NtCrest
import pl.nowinkitransferowe.core.designsystem.component.NtFlag
import pl.nowinkitransferowe.core.model.ClubRef
import pl.nowinkitransferowe.core.model.crestUrl

/**
 * The crest of the club on one side of a transfer or a rumour: initials when there is no crest, an
 * empty circle when there is no club (a free agent or a retired player).
 */
@Composable
fun ClubCrest(club: ClubRef, modifier: Modifier = Modifier, size: Dp = 36.dp) {
    NtCrest(
        imageUrl = club.crestUrl,
        name = if (club is ClubRef.Club) club.name else "",
        size = size,
        modifier = modifier,
    )
}

/** The flags of the website, by ISO 3166-1 alpha-2 code or gb-eng, gb-sct, gb-wls, gb-nir. */
private const val FLAGS_URL = "https://nowinkitransferowe.pl/static/flags/"

/** The flag of the country with [code], as in the club and player data. */
@Composable
fun CountryFlag(code: String, contentDescription: String?, modifier: Modifier = Modifier) {
    NtFlag(imageUrl = "$FLAGS_URL${code.lowercase()}.svg", contentDescription = contentDescription, modifier = modifier)
}
