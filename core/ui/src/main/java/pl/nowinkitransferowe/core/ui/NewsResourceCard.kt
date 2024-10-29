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

import android.content.ClipData
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaZoneId
import pl.nowinkitransferowe.core.designsystem.R.drawable
import pl.nowinkitransferowe.core.designsystem.component.NtIconToggleButton
import pl.nowinkitransferowe.core.designsystem.component.NtTopicTag
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserNewsResource
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewsResourceCardExpanded(
    modifier: Modifier = Modifier,
    userNewsResource: UserNewsResource,
    isBookmarked: Boolean,
    hasBeenViewed: Boolean,
    onToggleBookmark: () -> Unit,
    onClick: () -> Unit,
    selectedNewsId: String? = null,
    highlightSelectedNews: Boolean = false,
    onTopicClick: (String) -> Unit,
) {
    val clickActionLabel = stringResource(R.string.core_ui_card_tap_action)
//    val sharingLabel = stringResource(R.string.core_ui_feed_sharing)
//    val sharingContent = stringResource(
//        R.string.core_ui_feed_sharing_data,
//        userNewsResource.title,
//        userNewsResource.link,
//    )
//
//    val dragAndDropFlags = if (VERSION.SDK_INT >= VERSION_CODES.N) {
//        View.DRAG_FLAG_GLOBAL
//    } else {
//        0
//    }
    val isSelected = highlightSelectedNews && userNewsResource.id == selectedNewsId

    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.onSurface,
            )
        } else {
            BorderStroke(0.dp, Color.Transparent)
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.semantics {
            onClick(label = clickActionLabel, action = null)
        },
    ) {
        Column {
            Row {
                NewsResourceHeaderImage(userNewsResource.imageUrl)
            }
            Box(
                modifier = Modifier.padding(16.dp),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        NewsResourceTitle(
                            userNewsResource.title,
                            modifier = Modifier
                                .fillMaxWidth((.8f))
//                                .dragAndDropSource {
//                                    detectTapGestures(
//                                        onLongPress = {
//                                            startTransfer(
//                                                DragAndDropTransferData(
//                                                    ClipData.newPlainText(
//                                                        sharingLabel,
//                                                        sharingContent,
//                                                    ),
//                                                    flags = dragAndDropFlags,
//                                                ),
//                                            )
//                                        },
//                                    )
//                                },
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        BookmarkButton(isBookmarked, onToggleBookmark)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!hasBeenViewed) {
                            NotificationDot(
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(8.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                        NewsResourceMetaData(
                            userNewsResource.publishDate,
                            userNewsResource.category.combineNameWithEmoji(),
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    NewsResourceShortDescription(
                        userNewsResource.description,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    NewsResourceTopics(
                        topics = userNewsResource.topics,
                        onTopicClick = onTopicClick,
                    )
                }
            }
        }
    }
}

@Composable
fun NewsResourceTopics(
    topics: List<String>,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        // causes narrow chips
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (followableTopic in topics) {
            NtTopicTag(
                followed = false,
                onClick = { onTopicClick(followableTopic) },
                text = {
//                    val contentDescription = if () {
//                        stringResource(
//                            R.string.core_ui_topic_chip_content_description_when_followed,
//                            followableTopic.topic.name,
//                        )
//                    } else {
//                        stringResource(
//                            R.string.core_ui_topic_chip_content_description_when_not_followed,
//                            followableTopic.topic.name,
//                        )
//                    }
                    val contentDescription = stringResource(id = R.string.core_ui_topic_chip_content_description_when_followed, followableTopic)
                    Text(
                        text = followableTopic,
                        modifier = Modifier.semantics {
                            this.contentDescription = contentDescription
                        },
                    )
                },
            )
        }
    }
}

@Composable
fun NewsResourceHeaderImage(
    headerImageUrl: String?,
    height: Dp = 180.dp,
) {
    val imageBaseUrl = "http://nowinkitransferowe.pl/Images/"
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val imageLoader = rememberAsyncImagePainter(
        model = "$imageBaseUrl$headerImageUrl",
        onState = { state ->
            isLoading = state is AsyncImagePainter.State.Loading
            isError = state is AsyncImagePainter.State.Error
        },
    )
    val isLocalInspection = LocalInspectionMode.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            // Display a progress bar while loading
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp),
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            contentScale = ContentScale.Crop,
            painter = if (isError.not() && !isLocalInspection) {
                imageLoader
            } else {
                painterResource(drawable.core_designsystem_ic_placeholder_default)
            },
            // TODO b/226661685: Investigate using alt text of  image to populate content description
            // decorative image,
            contentDescription = null,
        )
    }
}

@Composable
fun NewsResourceTitle(
    newsResourceTitle: String,
    modifier: Modifier = Modifier,
) {
    Text(
        newsResourceTitle,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier,
        minLines = 2,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,

    )
}

@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NtIconToggleButton(
        checked = isBookmarked,
        onCheckedChange = { onClick() },
        modifier = modifier,
        icon = {
            Icon(
                imageVector = NtIcons.BookmarkBorder,
                contentDescription = stringResource(R.string.core_ui_bookmark),
            )
        },
        checkedIcon = {
            Icon(
                imageVector = NtIcons.Bookmark,
                contentDescription = stringResource(R.string.core_ui_unbookmark),
            )
        },
    )
}

@Composable
fun NotificationDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.core_ui_unread_resource_dot_content_description)
    Canvas(
        modifier = modifier
            .semantics { contentDescription = description },
        onDraw = {
            drawCircle(
                color,
                radius = size.minDimension / 2,
            )
        },
    )
}

@Composable
fun dateFormatted(publishDate: Instant, style: FormatStyle = FormatStyle.MEDIUM): String = DateTimeFormatter
    .ofLocalizedDate(style)
    .withLocale(Locale.getDefault())
    .withZone(LocalTimeZone.current.toJavaZoneId())
    .format(publishDate.toJavaInstant())

@Composable
fun NewsResourceMetaData(
    publishDate: Instant,
    resourceType: String,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
) {
    val formattedDate = dateFormatted(publishDate)
    Text(
        if (resourceType.isNotBlank()) {
            stringResource(R.string.core_ui_card_meta_data_text, formattedDate, resourceType)
        } else {
            formattedDate
        },
        style = textStyle,
    )
}

@Composable
fun NewsResourceShortDescription(
    newsResourceShortDescription: String,
    modifier: Modifier = Modifier,
) {
    Text(
        HtmlCompat.fromHtml(newsResourceShortDescription, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString(),
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 4,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun NewsResourceLongDescription(
    newsResourceLongDescription: String,
    modifier: Modifier = Modifier,
) {
    Text(
        HtmlCompat.fromHtml(newsResourceLongDescription, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString(),
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier,

    )
}

@Composable
fun NewsResourceAuthor(
    newsResourceAuthor: String,
    modifier: Modifier = Modifier,
) {
    Text(
        newsResourceAuthor,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun NewsResourceSrc(
    newsResourceSrc: String,
    modifier: Modifier = Modifier,
) {
    Text(newsResourceSrc, style = MaterialTheme.typography.bodyMedium, modifier = modifier)
}

fun trimDescription(text: String, limiter: Int): String {
    val words = text.split(" ")
    return if (words.size <= limiter) {
        text
    } else {
        words.take(20).joinToString(" ") + "..."
    }
}

@Preview("NewsResourceCardExpanded")
@Composable
private fun ExpandedNewsResourcePreview(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    CompositionLocalProvider(
        LocalInspectionMode provides true,
    ) {
        NtTheme {
            Surface {
                NewsResourceCardExpanded(
                    userNewsResource = userNewsResources[0],
                    isBookmarked = true,
                    hasBeenViewed = false,
                    onToggleBookmark = {},
                    onClick = {},
                    onTopicClick = {},
                )
            }
        }
    }
}
