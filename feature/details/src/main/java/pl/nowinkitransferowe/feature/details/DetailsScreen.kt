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

package pl.nowinkitransferowe.feature.details

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pl.nowinkitransferowe.core.designsystem.component.NtBackground
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.network.BuildConfig
import pl.nowinkitransferowe.core.ui.BookmarkButton
import pl.nowinkitransferowe.core.ui.DevicePreviews
import pl.nowinkitransferowe.core.ui.NewsResourceAuthor
import pl.nowinkitransferowe.core.ui.NewsResourceHeaderImage
import pl.nowinkitransferowe.core.ui.NewsResourceMetaData
import pl.nowinkitransferowe.core.ui.NewsResourceSrc
import pl.nowinkitransferowe.core.ui.NewsResourceTopics
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransfersResourceImage
import pl.nowinkitransferowe.core.ui.UserNewsResourcePreviewParameterProvider
import java.lang.StringBuilder

@Composable
internal fun DetailsRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val detailsUiState by viewModel.detailsUiState.collectAsStateWithLifecycle()
    val darkThemeConfig by viewModel.darkThemeConfig.collectAsStateWithLifecycle()
    TrackScreenViewEvent(screenName = "News: ${viewModel.newsResourceId}")
    DetailsScreen(
        detailsUiState = detailsUiState,
        showBackButton = showBackButton,
        onBackClick = onBackClick,
        onTopicClick = onTopicClick,
        onBookmarkChanged = viewModel::bookmarkNews,
        darkThemeConfig = darkThemeConfig,
        modifier = modifier.testTag("news:${viewModel.newsResourceId}"),
    )
}

@VisibleForTesting
@Composable
internal fun DetailsScreen(
    detailsUiState: DetailsUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onBookmarkChanged: (String, Boolean) -> Unit,
    darkThemeConfig: DarkThemeConfig,
    modifier: Modifier = Modifier,
) {
    val state = rememberScrollState()
    TrackScrollJank(scrollableState = state, stateName = "details:screen")

    Box(
        modifier = modifier
            .verticalScroll(state = state)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            when (detailsUiState) {
                DetailsUiState.Loading ->
                    NtLoadingWheel(
                        modifier = modifier,
                        contentDesc = stringResource(id = R.string.feature_details_loading),
                    )

                DetailsUiState.Error -> {
                    LaunchedEffect(Unit) {
                        onBackClick()
                    }
                }

                is DetailsUiState.Success -> {
                    DetailsToolbar(
                        showBackButton = showBackButton,
                        onBackClick = onBackClick,
                        userNewsResource = detailsUiState.userNewsResource,
                    )

                    DetailsBody(
                        userNewsResource = detailsUiState.userNewsResource,
                        onToggleBookmark = {
                            onBookmarkChanged(
                                detailsUiState.userNewsResource.id,
                                !detailsUiState.userNewsResource.isSaved,
                            )
                        },
                        onTopicClick = onTopicClick,
                        darkThemeConfig = darkThemeConfig,
                    )
                }
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
    }
}

@Composable
private fun DetailsToolbar(
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = {},
    userNewsResource: UserNewsResource,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
    ) {
        if (showBackButton) {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = NtIcons.ArrowBack,
                    contentDescription = stringResource(
                        id = pl.nowinkitransferowe.core.ui.R.string.core_ui_back,
                    ),
                )
            }
        } else {
            // Keeps the NiaFilterChip aligned to the end of the Row.
            Spacer(modifier = Modifier.width(1.dp))
        }
        val context = LocalContext.current
        val shareIntent = getShareIntent(
            url = prepareUrl(
                id = userNewsResource.id,
                title = userNewsResource.title,
                category = userNewsResource.category.serializedName,
            ),
        )
        IconButton(
            onClick = {
                startActivity(
                    context,
                    shareIntent,
                    null,
                )
            },
        ) {
            Icon(
                imageVector = NtIcons.Share,
                contentDescription = stringResource(id = pl.nowinkitransferowe.core.ui.R.string.core_ui_share),
            )
        }
    }
}

@Composable
private fun getShareIntent(url: String): Intent {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }
    return Intent.createChooser(sendIntent, null)
}

@Composable
private fun prepareUrl(id: String, title: String, category: String) =
    "${BuildConfig.BASE_URL}$id/${
        category.replace(
            " ",
            "-",
        )
    }/${escapeTitle(title = title)}"

@Composable
private fun escapeTitle(title: String): String =
    title.lowercase()
        .replace(" ", "-")
        .replace("!", "-")
        .replace(":", "")
        .replace("?", "-")
        .replace(",", "")
        .replace("ą", "a")
        .replace("ć", "c")
        .replace("ę", "e")
        .replace("ł", "l")
        .replace("ń", "n")
        .replace("ó", "o")
        .replace("ś", "s")
        .replace("ż", "z")
        .replace("ź", "z")
        .replace("'", "")
        .replace("(", "")
        .replace(")", "")
        .replace("[", "")
        .replace("]", "")

@Composable
private fun DetailsBody(
    userNewsResource: UserNewsResource,
    onToggleBookmark: () -> Unit,
    onTopicClick: (String) -> Unit,
    darkThemeConfig: DarkThemeConfig,
    modifier: Modifier = Modifier,
) {
    Column(modifier = Modifier.testTag("content")) {
        Row {
            NewsResourceHeaderImage(userNewsResource.imageUrl, 280.dp)
        }
        Box(
            modifier = Modifier.padding(16.dp),
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    NewsResourceDetailsTitle(
                        userNewsResource.title,
                        modifier = Modifier.fillMaxWidth((.8f)),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    BookmarkButton(userNewsResource.isSaved, onToggleBookmark)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NewsResourceMetaData(
                        userNewsResource.publishDate,
                        userNewsResource.category.combineNameWithEmoji(),
                        textStyle = MaterialTheme.typography.labelMedium,
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                NewsResourceDetailsDescription(
                    userNewsResource.description,
                    shouldUserDarkTheme = shouldUseDarkTheme(
                        darkThemeConfig = darkThemeConfig,
                    ),
                )
                Spacer(modifier = Modifier.height(32.dp))
                NewsResourceTopics(
                    topics = userNewsResource.topics,
                    onTopicClick = onTopicClick,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TransfersResourceImage(
                            imageUrl = userNewsResource.authPic,
                            placeHolderRes = pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_author_picture_placeholder,
                            size = 60.dp,
                            cornerShape = 32.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        NewsResourceAuthor(
                            newsResourceAuthor = userNewsResource.author,
                            modifier = Modifier.width(80.dp),
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Text(
                            "Źródło:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = modifier,
                        )
                        NewsResourceSrc(newsResourceSrc = userNewsResource.src)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NewsResourceDetailsTitle(
    newsResourceTitle: String,
    modifier: Modifier = Modifier,
) {
    Text(
        newsResourceTitle,
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier,
    )
}

@Composable
private fun shouldUseDarkTheme(
    darkThemeConfig: DarkThemeConfig,
): Boolean =
    when (darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ColumnScope.NewsResourceDetailsDescription(description: String, shouldUserDarkTheme: Boolean) {
    val width = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
    val socialMediaJsScript = "<style>iframe {" +
        "        display: block;" +
        "        max-width:100%;" +
        "        }  </style>" + "<style>img{display: inline; height: auto; max-width: 100%;}</style>" +
        "<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');" +
        "</script>" +
        "<script>" + " <script async defer src=\"//platform.instagram.com/en_US/embeds.js\"></script>"
    val fontSize = "<body style=\"font-size:110%"
    val fontColor = ";color:white"
    val htmlStyle = StringBuilder().append(width).append(socialMediaJsScript).append(fontSize)
    if (shouldUserDarkTheme) {
        htmlStyle.append(fontColor)
    }
    htmlStyle.append("\">")
    var isLoading by remember { mutableStateOf(true) }
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            NtLoadingWheel(contentDesc = stringResource(id = R.string.feature_details_loading))
        }
    }
    AnimatedVisibility(
        visible = !isLoading,
        enter = slideInVertically() + fadeIn(),
        exit = fadeOut(),
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(false)
                    cookieManager.setAcceptThirdPartyCookies(this, false)
                    setBackgroundColor(Color.TRANSPARENT)
                    settings.javaScriptEnabled = true
                    settings.loadWithOverviewMode = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest,
                        ): Boolean {
                            val intent = Intent(Intent.ACTION_VIEW, request.url)
                            view.context.startActivity(intent)
                            return true
                        }
                    }
                    settings.setSupportZoom(true)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(
                    "https://twitter.com",
                    htmlStyle.toString() + description,
                    "text/html; charset=utf-8",
                    "UTF-8",
                    "",
                )
            },

        )
    }

    LaunchedEffect(Unit) {
        delay(200)
        isLoading = false
    }
}

@DevicePreviews
@Composable
fun DetailsScreenPopulated(
    @PreviewParameter(UserNewsResourcePreviewParameterProvider::class)
    userNewsResources: List<UserNewsResource>,
) {
    NtTheme {
        NtBackground {
            DetailsScreen(
                detailsUiState = DetailsUiState.Success(userNewsResources[0]),
                showBackButton = true,
                onBackClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            )
        }
    }
}

@DevicePreviews
@Composable
fun DetailsScreenLoading() {
    NtTheme {
        NtBackground {
            DetailsScreen(
                detailsUiState = DetailsUiState.Loading,
                showBackButton = true,
                onBackClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            )
        }
    }
}
