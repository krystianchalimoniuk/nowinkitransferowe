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

package pl.nowinkitransferowe.feature.articledetails.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.common.WebContent
import pl.nowinkitransferowe.core.designsystem.component.NtAuthorAvatar
import pl.nowinkitransferowe.core.designsystem.component.NtCrest
import pl.nowinkitransferowe.core.designsystem.component.NtLoadingWheel
import pl.nowinkitransferowe.core.designsystem.component.NtPlayerPhoto
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.LocalNtColors
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.ui.ArticleImage
import pl.nowinkitransferowe.core.ui.ArticleKindBadge
import pl.nowinkitransferowe.core.ui.ArticleTags
import pl.nowinkitransferowe.core.ui.BookmarkButton
import pl.nowinkitransferowe.core.ui.ClubCrest
import pl.nowinkitransferowe.core.ui.RumourStageIndicator
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TrackScrollJank
import pl.nowinkitransferowe.core.ui.articleOverline

@Composable
internal fun ArticleDetailsRoute(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onArticleTopicClick: (String) -> Unit = {},
    onArticleLinkClick: (Int) -> Unit,
    onRumourClick: (Rumour) -> Unit,
    onClubClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticleDetailsViewModel,
) {
    val detailsUiState by viewModel.detailsUiState.collectAsStateWithLifecycle()
    val darkThemeConfig by viewModel.darkThemeConfig.collectAsStateWithLifecycle()
    TrackScreenViewEvent(screenName = "Article: ${viewModel.articleId}")
    ArticleDetailsScreen(
        detailsUiState = detailsUiState,
        showBackButton = showBackButton,
        onBackClick = onBackClick,
        onTopicClick = onTopicClick,
        onArticleTopicClick = onArticleTopicClick,
        onBookmarkChanged = viewModel::bookmarkArticle,
        onArticleLinkClick = onArticleLinkClick,
        onRumourClick = onRumourClick,
        onClubClick = onClubClick,
        onPlayerClick = onPlayerClick,
        darkThemeConfig = darkThemeConfig,
        modifier = modifier.testTag("news:${viewModel.articleId}"),
    )
}

/**
 * An article as on the website: the image, kind, topics and date, the title, lead and author,
 * the rumour it moved, the clubs and players it is about, the content in a WebView with the
 * website stylesheet, and its tags.
 */
@VisibleForTesting
@Composable
internal fun ArticleDetailsScreen(
    detailsUiState: ArticleDetailsUiState,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onArticleTopicClick: (String) -> Unit = {},
    onBookmarkChanged: (Int, Boolean) -> Unit,
    darkThemeConfig: DarkThemeConfig,
    modifier: Modifier = Modifier,
    onArticleLinkClick: (Int) -> Unit = {},
    onRumourClick: (Rumour) -> Unit = {},
    onClubClick: (Int) -> Unit = {},
    onPlayerClick: (Int) -> Unit = {},
) {
    val scrollState = rememberScrollState()
    TrackScrollJank(scrollableState = scrollState, stateName = "details:screen")
    // The actions hide while reading down and come back when scrolling up
    val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = FloatingToolbarExitDirection.Bottom)
    val context = LocalContext.current

    when (detailsUiState) {
        ArticleDetailsUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NtLoadingWheel(contentDesc = stringResource(R.string.feature_article_details_impl_loading))
        }

        is ArticleDetailsUiState.Success -> Box(
            modifier
                .fillMaxSize()
                .nestedScroll(toolbarScrollBehavior),
        ) {
            val article = detailsUiState.userArticle
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth(),
                ) {
                    ArticleToolbar(showBackButton = showBackButton, onBackClick = onBackClick)
                    ArticleHeader(article)
                    detailsUiState.rumour?.let { rumour ->
                        RumourBox(rumour, onClick = { onRumourClick(rumour) }, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                    AboutChips(detailsUiState.clubs, detailsUiState.players, onClubClick = onClubClick, onPlayerClick = onPlayerClick)
                    ArticleContent(
                        contentHtml = article.contentHtml,
                        darkTheme = when (darkThemeConfig) {
                            DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                            DarkThemeConfig.LIGHT -> false
                            DarkThemeConfig.DARK -> true
                        },
                        onArticleLinkClick = onArticleLinkClick,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    // The topics first, which open their topic screen, then the tags
                    ArticleTags(
                        topics = article.topics.map { it.name },
                        onTopicClick = { name -> article.topics.find { it.name == name }?.let { onArticleTopicClick(it.slug) } },
                        highlighted = true,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp).testTag("article:topics"),
                    )
                    ArticleTags(
                        topics = article.tags,
                        onTopicClick = onTopicClick,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    // Room for the floating toolbar over the end of the article
                    Spacer(Modifier.height(FloatingToolbarRoom))
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
                }
            }
            ArticleActions(
                article = article,
                scrollBehavior = toolbarScrollBehavior,
                onShare = { shareArticle(context, article) },
                onToggleBookmark = { onBookmarkChanged(article.id, !article.isSaved) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .offset(y = -FloatingToolbarDefaults.ScreenOffset),
            )
        }
    }
}

@Composable
private fun ArticleToolbar(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(4.dp).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(NtIcons.ArrowBack, contentDescription = stringResource(pl.nowinkitransferowe.core.ui.R.string.core_ui_back))
            }
        }
    }
}

/**
 * Material 3 Expressive's floating toolbar at the bottom of the article: save and share within
 * thumb reach. It slides away while reading down and returns when scrolling up.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ArticleActions(
    article: UserArticle,
    scrollBehavior: FloatingToolbarScrollBehavior,
    onShare: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        scrollBehavior = scrollBehavior,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        modifier = modifier.testTag("article:actions"),
    ) {
        BookmarkButton(article.isSaved, onToggleBookmark)
        IconButton(onClick = onShare) {
            Icon(NtIcons.Share, contentDescription = stringResource(R.string.feature_article_details_impl_share))
        }
    }
}

/** The floating toolbar with its offset from the bottom edge. */
private val FloatingToolbarRoom = 96.dp

@Composable
private fun ArticleHeader(article: UserArticle) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val image = article.imageUrl ?: article.thumbnailUrl
        // Without an image the website shows its /N/ placeholder in the same frame
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            ArticleImage(image, Modifier.fillMaxSize())
        }
        article.imageSource?.let {
            Text(
                stringResource(R.string.feature_article_details_impl_photo, it),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArticleKindBadge(article.kind)
            Text(articleOverline(article), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(article.title.uppercase(), style = MaterialTheme.typography.headlineLarge)
        if (article.lead.isNotBlank()) {
            Text(article.lead, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            article.author?.let { author ->
                NtAuthorAvatar(name = author.name, size = 28.dp)
                Text(author.name, style = MaterialTheme.typography.labelLarge)
            }
            article.newsSource?.let {
                Text(
                    stringResource(R.string.feature_article_details_impl_source, it),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** The rumour this article moved, as the amber box on the website. */
@Composable
private fun RumourBox(rumour: Rumour, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalNtColors.current
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.rumourContainer, contentColor = colors.onRumourContainer),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.feature_article_details_impl_rumour).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.rumour,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(rumour.player.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f, fill = false))
                rumour.from?.let { ClubCrest(it, size = 20.dp) }
                Icon(NtIcons.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                ClubCrest(rumour.to, size = 20.dp)
                Text(rumour.to.name, style = MaterialTheme.typography.labelMedium)
            }
            RumourStageIndicator(rumour.stage, rumour.trend)
        }
    }
}

/** Clubs and players the article is about; until their screens exist they open on the website. */
@Composable
private fun AboutChips(clubs: List<Club>, players: List<Player>, onClubClick: (Int) -> Unit, onPlayerClick: (Int) -> Unit) {
    if (clubs.isEmpty() && players.isEmpty()) return
    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            stringResource(R.string.feature_article_details_impl_in_article),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            clubs.forEach { club ->
                AssistChip(
                    onClick = { onClubClick(club.id) },
                    label = { Text(club.name) },
                    leadingIcon = { NtCrest(imageUrl = club.crestUrl, name = club.name, size = 18.dp) },
                )
            }
            players.forEach { player ->
                AssistChip(
                    onClick = { onPlayerClick(player.id) },
                    label = { Text(player.name) },
                    leadingIcon = { NtPlayerPhoto(photoUrl = player.photoUrl, name = player.name, size = 18.dp) },
                )
            }
        }
    }
}

/**
 * The article content in a WebView with the website stylesheet. Links to other articles open
 * them in the app; other links open in the browser. Third-party cookies stay off.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ArticleContent(
    contentHtml: String,
    darkTheme: Boolean,
    onArticleLinkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current || !WebContent.enabled) return
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .testTag("article:content"),
        factory = { context ->
            WebView(context).apply {
                val webView = this
                CookieManager.getInstance().apply {
                    setAcceptCookie(false)
                    setAcceptThirdPartyCookies(webView, false)
                }
                setBackgroundColor(Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val articleId = articleIdOf(request.url)
                        if (articleId != null) {
                            onArticleLinkClick(articleId)
                        } else {
                            view.context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
                        }
                        return true
                    }
                }
            }
        },
        update = { webView ->
            val html = articleHtml(contentHtml, darkTheme)
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL(WEBSITE_URL, html, "text/html", "UTF-8", null)
            }
        },
    )
}

private fun shareArticle(context: Context, article: UserArticle) {
    val send = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, article.url)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(send, null))
}
