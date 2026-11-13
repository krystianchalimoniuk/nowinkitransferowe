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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * A club crest or a player photo: the image when there is one, otherwise (and while it loads)
 * the initials of [name] on a neutral background. Small sizes are round; from 48 dp the shape is
 * a rounded square, as in the mockups.
 */
@Composable
fun NtCrest(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    shape: Shape = if (size >= 48.dp) RoundedCornerShape(30) else CircleShape,
    fillShape: Boolean = false,
) {
    var showInitials by remember(imageUrl) { mutableStateOf(true) }
    val inspection = LocalInspectionMode.current
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (showInitials) {
            Text(
                text = initials(name),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.3f).sp,
                    lineHeight = (size.value * 0.36f).sp,
                ),
                maxLines = 1,
            )
        }
        if (imageUrl != null && !inspection) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = if (fillShape) ContentScale.Crop else ContentScale.Fit,
                alignment = if (fillShape) Alignment.TopCenter else Alignment.Center,
                onSuccess = { showInitials = false },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (fillShape) 0.dp else size * 0.08f),
            )
        }
    }
}

/**
 * A player photo in the same shape as the initials shown without one: round at small sizes and a
 * rounded square from 48 dp. Unlike a crest, the photo fills the shape, cropped from the top so the
 * face stays in view.
 */
@Composable
fun NtPlayerPhoto(
    photoUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    NtCrest(imageUrl = photoUrl, name = name, modifier = modifier, size = size, fillShape = true)
}

/** "Real Madryt" -> "RM", "Gavi" -> "GAV". */
internal fun initials(name: String): String {
    val words = name.split(' ', '-').filter { it.isNotBlank() && it.first().isLetter() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words.single().take(3)
        else -> words.take(2).joinToString("") { it.first().toString() }
    }.uppercase()
}
