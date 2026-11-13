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

package pl.nowinkitransferowe.feature.articledetails.impl

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pl.nowinkitransferowe.core.designsystem.component.NtBackground
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.ui.ArticleAuthor
import pl.nowinkitransferowe.core.ui.ArticleHeaderImage
import pl.nowinkitransferowe.core.ui.ArticleMetaData
import pl.nowinkitransferowe.core.ui.ArticleSource
import pl.nowinkitransferowe.core.ui.ArticleTags
import pl.nowinkitransferowe.core.ui.BookmarkButton
import pl.nowinkitransferowe.core.ui.DevicePreviews
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.TransfersResourceImage
import pl.nowinkitransferowe.core.ui.UserArticlePreviewParameterProvider
import java.lang.StringBuilder

@Composable
internal fun ArticleDetailsRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticleDetailsViewModel,
) {
    val detailsUiState by viewModel.detailsUiState.collectAsStateWithLifecycle()
    val darkThemeConfig by viewModel.darkThemeConfig.collectAsStateWithLifecycle()
    TrackScreenViewEvent(screenName = "News: ${viewModel.articleId}")
    ArticleDetailsScreen(
        detailsUiState = detailsUiState,
        showBackButton = showBackButton,
        onBackClick = onBackClick,
        onTopicClick = onTopicClick,
        onBookmarkChanged = viewModel::bookmarkArticle,
        darkThemeConfig = darkThemeConfig,
        modifier = modifier.testTag("news:${viewModel.articleId}"),
    )
}

@VisibleForTesting
@Composable
internal fun ArticleDetailsScreen(
    detailsUiState: ArticleDetailsUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onBookmarkChanged: (Int, Boolean) -> Unit,
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
                ArticleDetailsUiState.Loading ->
                    NtLoadingWheel(
                        modifier = modifier,
                        contentDesc = stringResource(id = R.string.feature_article_details_impl_loading),
                    )

                ArticleDetailsUiState.Error -> {
                    LaunchedEffect(Unit) {
                        onBackClick()
                    }
                }

                is ArticleDetailsUiState.Success -> {
                    ArticleDetailsToolbar(
                        showBackButton = showBackButton,
                        onBackClick = onBackClick,
                        userArticle = detailsUiState.userArticle,
                    )

                    ArticleDetailsBody(
                        userArticle = detailsUiState.userArticle,
                        onToggleBookmark = {
                            onBookmarkChanged(
                                detailsUiState.userArticle.id,
                                !detailsUiState.userArticle.isSaved,
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
private fun ArticleDetailsToolbar(
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = {},
    userArticle: UserArticle,
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
            // Keeps the share button aligned to the end of the Row.
            Spacer(modifier = Modifier.width(1.dp))
        }
        val context = LocalContext.current
        val shareIntent = getShareIntent(url = userArticle.url)
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
private fun ArticleDetailsBody(
    userArticle: UserArticle,
    onToggleBookmark: () -> Unit,
    onTopicClick: (String) -> Unit,
    darkThemeConfig: DarkThemeConfig,
    modifier: Modifier = Modifier,
) {
    Column(modifier = Modifier.testTag("content")) {
        Row {
            ArticleHeaderImage(userArticle.imageUrl, 280.dp)
        }
        Box(
            modifier = Modifier.padding(16.dp),
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    ArticleDetailsTitle(
                        userArticle.title,
                        modifier = Modifier.fillMaxWidth((.8f)),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    BookmarkButton(userArticle.isSaved, onToggleBookmark)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ArticleMetaData(
                        userArticle.publishedAt,
                        userArticle.topics.firstOrNull()?.name.orEmpty(),
                        textStyle = MaterialTheme.typography.labelMedium,
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                ArticleContent(
                    userArticle.contentHtml,
                    shouldUserDarkTheme = shouldUseDarkTheme(
                        darkThemeConfig = darkThemeConfig,
                    ),
                )
                Spacer(modifier = Modifier.height(32.dp))
                ArticleTags(
                    topics = userArticle.tags,
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
                            imageUrl = userArticle.author?.avatarUrl,
                            placeHolderRes = pl.nowinkitransferowe.core.designsystem.R.drawable.core_designsystem_ic_author_picture_placeholder,
                            size = 60.dp,
                            cornerShape = 32.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ArticleAuthor(
                            author = userArticle.author?.name.orEmpty(),
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
                        ArticleSource(source = userArticle.newsSource.orEmpty())
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ArticleDetailsTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        title,
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier,
    )
}

@Composable
private fun shouldUseDarkTheme(
    darkThemeConfig: DarkThemeConfig,
): Boolean = when (darkThemeConfig) {
    DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
    DarkThemeConfig.LIGHT -> false
    DarkThemeConfig.DARK -> true
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ColumnScope.ArticleContent(description: String, shouldUserDarkTheme: Boolean) {
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
            NtLoadingWheel(contentDesc = stringResource(id = R.string.feature_article_details_impl_loading))
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
fun ArticleDetailsScreenPopulated(
    @PreviewParameter(UserArticlePreviewParameterProvider::class)
    userArticles: List<UserArticle>,
) {
    NtTheme {
        NtBackground {
            ArticleDetailsScreen(
                detailsUiState = ArticleDetailsUiState.Success(userArticles[0]),
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
fun ArticleDetailsScreenLoading() {
    NtTheme {
        NtBackground {
            ArticleDetailsScreen(
                detailsUiState = ArticleDetailsUiState.Loading,
                showBackButton = true,
                onBackClick = {},
                onTopicClick = {},
                onBookmarkChanged = { _, _ -> },
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            )
        }
    }
}
