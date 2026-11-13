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

package pl.nowinkitransferowe.feature.articles.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.RumourRepository
import pl.nowinkitransferowe.core.data.repository.UserArticleRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.Rumour
import pl.nowinkitransferowe.core.model.RumourStatus
import pl.nowinkitransferowe.core.model.Topic
import pl.nowinkitransferowe.core.model.UserArticle
import pl.nowinkitransferowe.core.model.UserTransfer
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

/** The tabs of the home screen, by the kind of the articles. */
enum class HomeTab(val kinds: Set<ArticleKind>?) {
    ALL(null),
    OFFICIAL(setOf(ArticleKind.OFFICIAL)),
    RUMOURS(setOf(ArticleKind.RUMOUR)),
}

sealed interface HomeUiState {
    data object Loading : HomeUiState

    /**
     * [featured] is the first article the editors picked, or the newest one; [latest] are the
     * other articles. The carousels show only without a topic, since transfers and rumours have
     * no topics.
     */
    data class Success(
        val tab: HomeTab,
        val topic: String?,
        val topics: List<Topic>,
        val featured: UserArticle?,
        val latest: List<UserArticle>,
        val transfers: List<UserTransfer>,
        val rumours: List<Rumour>,
        val hasMore: Boolean,
    ) : HomeUiState {
        val showTransfers: Boolean get() = topic == null && tab != HomeTab.RUMOURS && transfers.isNotEmpty()
        val showRumours: Boolean get() = topic == null && tab != HomeTab.OFFICIAL && rumours.isNotEmpty()
    }
}

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    syncManager: SyncManager,
    private val userDataRepository: UserDataRepository,
    userArticleRepository: UserArticleRepository,
    userTransferRepository: UserTransferRepository,
    rumourRepository: RumourRepository,
    dictionaryRepository: DictionaryRepository,
) : ViewModel() {

    internal var clock: Clock = Clock.System

    val selectedArticleId: StateFlow<Int?> = savedStateHandle.getStateFlow(SELECTED_ARTICLE_ID_KEY, null)

    private val tab = savedStateHandle.getStateFlow(TAB_KEY, HomeTab.ALL)
    private val topic = savedStateHandle.getStateFlow<String?>(TOPIC_KEY, null)
    private val page = MutableStateFlow(1)

    val isSyncing = syncManager.isSyncing.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val carousels: Flow<Pair<List<UserTransfer>, List<Rumour>>> = combine(
        userTransferRepository.observeTransfers(limit = CAROUSEL_SIZE),
        rumourRepository.observeRumours(RumourStatus.ACTIVE, updatedSince = clock.now() - RUMOUR_FRESH_FOR)
            .map { it.take(CAROUSEL_SIZE) },
    ) { transfers, rumours -> transfers to rumours }

    val uiState: StateFlow<HomeUiState> = combine(tab, topic, page, ::Triple)
        .flatMapLatest { (tab, topic, page) ->
            val limit = PAGE_SIZE * page + 1
            combine(
                userArticleRepository.observeArticles(kinds = tab.kinds, topic = topic, limit = limit),
                dictionaryRepository.observeTopics(),
                carousels,
            ) { articles, topics, (transfers, rumours) ->
                val now = clock.now()
                val featured = articles.firstOrNull { it.isFeatured(now) } ?: articles.firstOrNull()
                HomeUiState.Success(
                    tab = tab,
                    topic = topic,
                    topics = topics,
                    featured = featured,
                    latest = articles - setOfNotNull(featured),
                    transfers = transfers,
                    rumours = rumours,
                    hasMore = articles.size == limit,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    fun selectTab(tab: HomeTab) {
        savedStateHandle[TAB_KEY] = tab
        page.value = 1
    }

    fun selectTopic(topic: String?) {
        savedStateHandle[TOPIC_KEY] = topic
        page.value = 1
    }

    fun loadNextPage() {
        val state = uiState.value
        if (state is HomeUiState.Success && state.hasMore) page.value += 1
    }

    fun updateArticleSaved(articleId: Int, isChecked: Boolean) {
        viewModelScope.launch { userDataRepository.setArticleBookmarked(articleId, isChecked) }
    }

    fun setArticleViewed(articleId: Int, viewed: Boolean) {
        viewModelScope.launch { userDataRepository.setArticleViewed(articleId, viewed) }
    }

    fun onArticleClick(articleId: Int?) {
        savedStateHandle[SELECTED_ARTICLE_ID_KEY] = articleId
    }

    companion object {
        const val PAGE_SIZE = 10
        const val CAROUSEL_SIZE = 10

        /** Rumours updated in the last 45 days, like the rumours sections on the website. */
        val RUMOUR_FRESH_FOR = 45.days
        private const val SELECTED_ARTICLE_ID_KEY = "selectedArticleId"
        private const val TAB_KEY = "tab"
        private const val TOPIC_KEY = "topic"
    }
}
