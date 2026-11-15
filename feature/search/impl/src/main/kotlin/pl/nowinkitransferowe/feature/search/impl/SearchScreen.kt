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

package pl.nowinkitransferowe.feature.search.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.component.NtCrest
import pl.nowinkitransferowe.core.designsystem.component.NtPlayerPhoto
import pl.nowinkitransferowe.core.designsystem.component.NtSectionHeader
import pl.nowinkitransferowe.core.designsystem.component.NtTextButton
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.ui.ArticleListItem
import pl.nowinkitransferowe.core.ui.DevicePreviews
import pl.nowinkitransferowe.core.ui.R.string
import pl.nowinkitransferowe.core.ui.RumourListItem
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TransferListItem
import pl.nowinkitransferowe.feature.search.impl.R as searchR

@Composable
internal fun SearchRoute(
    onBackClick: () -> Unit,
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    onClubClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit,
    onRumourClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel,
) {
    val recentSearchQueriesUiState by searchViewModel.recentSearchQueriesUiState.collectAsStateWithLifecycle()
    val searchResultUiState by searchViewModel.searchResultUiState.collectAsStateWithLifecycle()
    val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
    val query = if (searchQuery == null) {
        ""
    } else {
        searchQuery.toString()
    }
    SearchScreen(
        modifier = modifier,
        searchQuery = query,
        recentSearchesUiState = recentSearchQueriesUiState,
        searchResultUiState = searchResultUiState,
        onSearchQueryChanged = searchViewModel::onSearchQueryChanged,
        onSearchTriggered = searchViewModel::onSearchTriggered,
        onClearRecentSearches = searchViewModel::clearRecentSearches,
        onArticleSavedChanged = searchViewModel::setArticleBookmarked,
        onArticleViewed = { searchViewModel.setArticleViewed(it, true) },
        onTransferSavedChanged = searchViewModel::setTransferBookmarked,
        onTransferViewed = { searchViewModel.setTransferViewed(it, true) },
        onBackClick = onBackClick,
        onTopicClick = searchViewModel::onSearchQueryChanged,
        onArticleClick = onArticleClick,
        onTransferClick = onTransferClick,
        onClubClick = onClubClick,
        onPlayerClick = onPlayerClick,
        onRumourClick = onRumourClick,
    )
}

@Composable
internal fun SearchScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    recentSearchesUiState: RecentSearchQueriesUiState = RecentSearchQueriesUiState.Loading,
    searchResultUiState: SearchResultUiState = SearchResultUiState.Loading,
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchTriggered: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    onArticleSavedChanged: (Int, Boolean) -> Unit = { _, _ -> },
    onArticleViewed: (Int) -> Unit = {},
    onTransferSavedChanged: (Int, Boolean) -> Unit = { _, _ -> },
    onTransferViewed: (List<Int>) -> Unit = {},
    onBackClick: () -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    onArticleClick: (Int) -> Unit = {},
    onTransferClick: (Int) -> Unit = {},
    onClubClick: (Int) -> Unit = {},
    onPlayerClick: (Int) -> Unit = {},
    onRumourClick: (Int) -> Unit = {},
) {
    TrackScreenViewEvent(screenName = "Search")
    Column(modifier = modifier) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        SearchToolbar(
            onBackClick = onBackClick,
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearchTriggered,
            searchQuery = searchQuery,
        )
        when (searchResultUiState) {
            SearchResultUiState.Loading,
            SearchResultUiState.LoadFailed,
            -> Unit

            SearchResultUiState.SearchNotReady -> SearchNotReadyBody()
            SearchResultUiState.EmptyQuery,
            -> {
                if (recentSearchesUiState is RecentSearchQueriesUiState.Success) {
                    RecentSearchesBody(
                        onClearRecentSearches = onClearRecentSearches,
                        onRecentSearchClicked = {
                            onSearchQueryChanged(it)
                            onSearchTriggered(it)
                        },
                        recentSearchQueries = recentSearchesUiState.recentQueries.map { it.query },
                    )
                }
            }

            is SearchResultUiState.Success -> {
                if (searchResultUiState.isEmpty()) {
                    EmptySearchResultBody(
                        searchQuery = searchQuery,
                    )
                    if (recentSearchesUiState is RecentSearchQueriesUiState.Success) {
                        RecentSearchesBody(
                            onClearRecentSearches = onClearRecentSearches,
                            onRecentSearchClicked = {
                                onSearchQueryChanged(it)
                                onSearchTriggered(it)
                            },
                            recentSearchQueries = recentSearchesUiState.recentQueries.map { it.query },
                        )
                    }
                } else {
                    SearchResultBody(
                        result = searchResultUiState,
                        onArticleClick = onArticleClick,
                        onTransferClick = onTransferClick,
                        onClubClick = onClubClick,
                        onPlayerClick = onPlayerClick,
                        onRumourClick = onRumourClick,
                        onArticleSavedChanged = onArticleSavedChanged,
                    )
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
}

@Composable
fun EmptySearchResultBody(
    searchQuery: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 48.dp),
    ) {
        val message =
            stringResource(id = searchR.string.feature_search_impl_result_not_found, searchQuery)
        val start = message.indexOf(searchQuery)
        Text(
            text = AnnotatedString(
                text = message,
                spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start = start,
                        end = start + searchQuery.length,
                    ),
                ),
            ),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp),
        )
    }
}

@Composable
private fun SearchNotReadyBody() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 48.dp),
    ) {
        Text(
            text = stringResource(id = searchR.string.feature_search_impl_not_ready),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp),
        )
    }
}

/**
 * Results in sections as on the website: clubs and players as chips that open their screens,
 * then active rumours, transfers and articles. Long sections show their first few until expanded.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchResultBody(
    result: SearchResultUiState.Success,
    onArticleClick: (Int) -> Unit,
    onTransferClick: (Int) -> Unit,
    onClubClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit,
    onRumourClick: (Int) -> Unit,
    onArticleSavedChanged: (Int, Boolean) -> Unit,
) {
    var expanded by rememberSaveable(result) { mutableStateOf(emptySet<String>()) }
    fun <T> LazyListScope.section(
        key: String,
        title: Int,
        items: List<T>,
        collapsedCount: Int,
        itemKey: (T) -> Any,
        content: @Composable (T) -> Unit,
    ) {
        if (items.isEmpty()) return
        item(key = "$key:header") {
            NtSectionHeader("${stringResource(title)} (${items.size})", Modifier.padding(horizontal = 16.dp).padding(top = 12.dp))
        }
        val shown = if (key in expanded) items else items.take(collapsedCount)
        items(shown, key = { "$key:${itemKey(it)}" }) { content(it) }
        if (items.size > shown.size) {
            item(key = "$key:more") {
                NtTextButton(onClick = { expanded = expanded + key }, modifier = Modifier.padding(horizontal = 8.dp).testTag("search:$key:more")) {
                    Text(stringResource(searchR.string.feature_search_impl_show_all, items.size))
                }
            }
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("search:results"),
    ) {
        if (result.clubs.isNotEmpty() || result.players.isNotEmpty()) {
            item(key = "entities") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    result.clubs.forEach { club ->
                        AssistChip(
                            onClick = { onClubClick(club.id) },
                            label = { Text(club.name) },
                            leadingIcon = { NtCrest(imageUrl = club.crestUrl, name = club.name, size = 20.dp) },
                            modifier = Modifier.testTag("search:club:${club.id}"),
                        )
                    }
                    result.players.forEach { player ->
                        AssistChip(
                            onClick = { onPlayerClick(player.id) },
                            label = { Text(player.name) },
                            leadingIcon = { NtPlayerPhoto(photoUrl = player.photoUrl, name = player.name, size = 20.dp) },
                            modifier = Modifier.testTag("search:player:${player.id}"),
                        )
                    }
                }
            }
        }
        section("rumours", searchR.string.feature_search_impl_rumours, result.rumours, collapsedCount = 3, itemKey = { it.id }) { rumour ->
            RumourListItem(rumour, onClick = { onRumourClick(rumour.id) }, modifier = Modifier.padding(horizontal = 4.dp))
        }
        section("transfers", searchR.string.feature_search_impl_transfers, result.transfers, collapsedCount = 5, itemKey = { it.id }) { transfer ->
            TransferListItem(transfer, onClick = { onTransferClick(transfer.id) }, modifier = Modifier.padding(horizontal = 4.dp))
        }
        section("articles", searchR.string.feature_search_impl_articles, result.articles, collapsedCount = 10, itemKey = { it.id }) { article ->
            ArticleListItem(
                article = article,
                onClick = { onArticleClick(article.id) },
                onToggleBookmark = { onArticleSavedChanged(article.id, !article.isSaved) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

@Composable
private fun RecentSearchesBody(
    recentSearchQueries: List<String>,
    onClearRecentSearches: () -> Unit,
    onRecentSearchClicked: (String) -> Unit,
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(id = searchR.string.feature_search_impl_recent_searches))
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (recentSearchQueries.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onClearRecentSearches()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Icon(
                        imageVector = NtIcons.Close,
                        contentDescription = stringResource(
                            id = searchR.string.feature_search_impl_clear_recent_searches_content_desc,
                        ),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            items(recentSearchQueries) { recentSearch ->
                Text(
                    text = recentSearch,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .clickable { onRecentSearchClicked(recentSearch) }
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SearchToolbar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = NtIcons.ArrowBack,
                contentDescription = stringResource(
                    id = string.core_ui_back,
                ),
            )
        }
        SearchTextField(
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearchTriggered,
            searchQuery = searchQuery,
        )
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val onSearchExplicitlyTriggered = {
        keyboardController?.hide()
        onSearchTriggered(searchQuery)
    }

    TextField(
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        leadingIcon = {
            Icon(
                imageVector = NtIcons.Search,
                contentDescription = stringResource(
                    id = searchR.string.feature_search_impl_title,
                ),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onSearchQueryChanged("")
                    },
                ) {
                    Icon(
                        imageVector = NtIcons.Close,
                        contentDescription = stringResource(
                            id = searchR.string.feature_search_impl_clear_search_text_content_desc,
                        ),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        onValueChange = {
            if ("\n" !in it) onSearchQueryChanged(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusRequester(focusRequester)
            .onKeyEvent {
                if (it.key == Key.Enter) {
                    if (searchQuery.isBlank()) return@onKeyEvent false
                    onSearchExplicitlyTriggered()
                    true
                } else {
                    false
                }
            }
            .testTag("searchTextField"),
        shape = RoundedCornerShape(32.dp),
        value = searchQuery,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (searchQuery.isBlank()) return@KeyboardActions
                onSearchExplicitlyTriggered()
            },
        ),
        maxLines = 1,
        singleLine = true,
    )
//    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//    }
}

@Preview
@Composable
private fun SearchToolbarPreview() {
    NtTheme {
        SearchToolbar(
            searchQuery = "",
            onBackClick = {},
            onSearchQueryChanged = {},
            onSearchTriggered = {},
        )
    }
}

@Preview
@Composable
private fun EmptySearchResultColumnPreview() {
    NtTheme {
        EmptySearchResultBody(
            searchQuery = "C++",
        )
    }
}

@Preview
@Composable
private fun RecentSearchesBodyPreview() {
    NtTheme {
        RecentSearchesBody(
            onClearRecentSearches = {},
            onRecentSearchClicked = {},
            recentSearchQueries = listOf("Real Madryt", "Ariel Borysiuk", "transfery"),
        )
    }
}

@Preview
@Composable
private fun SearchNotReadyBodyPreview() {
    NtTheme {
        SearchNotReadyBody()
    }
}

@DevicePreviews
@Composable
private fun SearchScreenPreview(
    @PreviewParameter(SearchUiStatePreviewParameterProvider::class)
    searchResultUiState: SearchResultUiState,
) {
    NtTheme {
        SearchScreen(searchResultUiState = searchResultUiState)
    }
}
