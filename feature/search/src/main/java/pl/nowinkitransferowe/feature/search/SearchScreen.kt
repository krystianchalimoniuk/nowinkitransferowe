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

package pl.nowinkitransferowe.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.nowinkitransferowe.core.designsystem.icon.NtIcons
import pl.nowinkitransferowe.core.designsystem.theme.NtTheme
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.ui.DevicePreviews
import pl.nowinkitransferowe.core.ui.NewsFeedUiState
import pl.nowinkitransferowe.core.ui.R.string
import pl.nowinkitransferowe.core.ui.TrackScreenViewEvent
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import pl.nowinkitransferowe.core.ui.newsFeed
import pl.nowinkitransferowe.core.ui.transferFeed
import java.net.URLDecoder
import kotlin.text.Charsets.UTF_8
import pl.nowinkitransferowe.feature.search.R as searchR

@Composable
internal fun SearchRoute(
    onBackClick: () -> Unit,
    onNewsClick: (String) -> Unit,
    onTransferClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val recentSearchQueriesUiState by searchViewModel.recentSearchQueriesUiState.collectAsStateWithLifecycle()
    val searchResultUiState by searchViewModel.searchResultUiState.collectAsStateWithLifecycle()
    val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
    val query = if (searchQuery == null) {
        ""
    } else {
        URLDecoder.decode(searchQuery, UTF_8.name())
    }
    SearchScreen(
        modifier = modifier,
        searchQuery = query,
        recentSearchesUiState = recentSearchQueriesUiState,
        searchResultUiState = searchResultUiState,
        onSearchQueryChanged = searchViewModel::onSearchQueryChanged,
        onSearchTriggered = searchViewModel::onSearchTriggered,
        onClearRecentSearches = searchViewModel::clearRecentSearches,
        onNewsResourcesCheckedChanged = searchViewModel::setNewsResourceBookmarked,
        onNewsResourceViewed = { searchViewModel.setNewsResourceViewed(it, true) },
        onTransferResourcesCheckedChanged = searchViewModel::setTransferResourceBookmarked,
        onTransferResourceViewed = { searchViewModel.setTransferResourceViewed(it, true) },
        onBackClick = onBackClick,
        onTopicClick = searchViewModel::onSearchQueryChanged,
        onNewsClick = onNewsClick,
        onTransferClick = onTransferClick,
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
    onNewsResourcesCheckedChanged: (String, Boolean) -> Unit = { _, _ -> },
    onNewsResourceViewed: (String) -> Unit = {},
    onTransferResourcesCheckedChanged: (String, Boolean) -> Unit = { _, _ -> },
    onTransferResourceViewed: (List<String>) -> Unit = {},
    onBackClick: () -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    onNewsClick: (String) -> Unit = {},
    onTransferClick: (String) -> Unit = {},
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
                        searchQuery = searchQuery,
                        transferResources = searchResultUiState.transferResources,
                        newsResources = searchResultUiState.newsResources,
                        onSearchTriggered = onSearchTriggered,
                        onTopicClick = onTopicClick,
                        onNewsClick = onNewsClick,
                        onTransferClick = onTransferClick,
                        onNewsResourcesCheckedChanged = onNewsResourcesCheckedChanged,
                        onNewsResourceViewed = onNewsResourceViewed,
                        onTransferResourcesCheckedChanged = onTransferResourcesCheckedChanged,
                        onTransferResourceViewed = onTransferResourceViewed,
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
            stringResource(id = searchR.string.feature_search_result_not_found, searchQuery)
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
            text = stringResource(id = searchR.string.feature_search_not_ready),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp),
        )
    }
}

@Composable
private fun SearchResultBody(
    searchQuery: String,
    transferResources: List<UserTransferResource>,
    newsResources: List<UserNewsResource>,
    onSearchTriggered: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onNewsClick: (String) -> Unit,
    onTransferClick: (String) -> Unit,
    onNewsResourcesCheckedChanged: (String, Boolean) -> Unit,
    onNewsResourceViewed: (String) -> Unit,
    onTransferResourcesCheckedChanged: (String, Boolean) -> Unit,
    onTransferResourceViewed: (List<String>) -> Unit,
) {
    val state = rememberLazyStaggeredGridState()
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(300.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 24.dp,
            modifier = Modifier
                .fillMaxSize()
                .testTag("search:newsResources"),
            state = state,
        ) {
            if (transferResources.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(id = searchR.string.feature_search_transfers))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                transferFeed(
                    feedState = TransferFeedUiState.Success(feed = transferResources),
                    onTransferResourcesCheckedChanged = onTransferResourcesCheckedChanged,
                    onTransferResourceViewed = onTransferResourceViewed,
                    onTransferClick = onTransferClick,
                )
            }

            if (newsResources.isNotEmpty()) {
                item(
                    span = StaggeredGridItemSpan.FullLine,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(id = searchR.string.feature_search_news))
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                newsFeed(
                    feedState = NewsFeedUiState.Success(feed = newsResources),
                    onNewsResourcesCheckedChanged = onNewsResourcesCheckedChanged,
                    onNewsResourceViewed = onNewsResourceViewed,
                    onTopicClick = onTopicClick,
                    onNewsSelected = {},
                    onNewsClick = onNewsClick,
                )
            }
        }
//        val itemsAvailable = transferResources.size + newsResources.size + 2
//        val scrollbarState = state.scrollbarState(
//            itemsAvailable = itemsAvailable,
//        )
//        state.DraggableScrollbar(
//            modifier = Modifier
//                .fillMaxHeight()
//                .windowInsetsPadding(WindowInsets.systemBars)
//                .padding(horizontal = 2.dp)
//                .align(Alignment.CenterEnd),
//            state = scrollbarState,
//            orientation = Orientation.Vertical,
//            onThumbMoved = state.rememberDraggableScroller(
//                itemsAvailable = itemsAvailable,
//            ),
//        )
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
                        append(stringResource(id = searchR.string.feature_search_recent_searches))
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
                            id = searchR.string.feature_search_clear_recent_searches_content_desc,
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
                    id = searchR.string.feature_search_title,
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
                            id = searchR.string.feature_search_clear_search_text_content_desc,
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
