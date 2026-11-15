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

package pl.nowinkitransferowe.feature.topic.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.ClubRepository
import pl.nowinkitransferowe.core.data.repository.DictionaryRepository
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.UserArticleRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.model.Club
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.TopicKind
import pl.nowinkitransferowe.core.model.UserArticle

/** The tabs of the topic screen, like the home screen's. */
enum class TopicTab(val kinds: Set<ArticleKind>?) {
    ALL(null),
    OFFICIAL(setOf(ArticleKind.OFFICIAL)),
    RUMOURS(setOf(ArticleKind.RUMOUR)),
}

/** What the topic or tag is: a league, another topic, a club or player, or just a tag. */
sealed interface TopicSubject {
    data class League(val slug: String) : TopicSubject
    data object OtherTopic : TopicSubject
    data class OfClub(val club: Club) : TopicSubject
    data class OfPlayer(val player: Player) : TopicSubject
    data object Tag : TopicSubject
}

/**
 * The [title] of the topic or tag, what it is, whether it can be followed and is, and its
 * [articles] of the picked [tab], newest first; [hasMore] when more can be loaded.
 */
data class TopicUiState(
    val title: String,
    val subject: TopicSubject,
    val isFollowed: Boolean,
    val tab: TopicTab,
    val articles: List<UserArticle>?,
    val hasMore: Boolean,
) {
    val isFollowable: Boolean get() = subject !is TopicSubject.Tag
}

/**
 * The articles of a topic or tag. A topic can be followed for the league notifications; a tag
 * that names a club or player follows that club or player and links to its screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = TopicViewModel.Factory::class)
class TopicViewModel @AssistedInject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    userArticleRepository: UserArticleRepository,
    dictionaryRepository: DictionaryRepository,
    clubRepository: ClubRepository,
    playerRepository: PlayerRepository,
    @Assisted("topic") val topic: String?,
    @Assisted("tag") val tag: String?,
) : ViewModel() {

    private val tab = savedStateHandle.getStateFlow(TAB_KEY, TopicTab.ALL)
    private val page = MutableStateFlow(1)

    /** The club or player a tag names, looked up once. */
    private val tagSubject = MutableStateFlow<TopicSubject>(TopicSubject.Tag)

    init {
        if (tag != null) {
            viewModelScope.launch {
                tagSubject.value = clubRepository.getClubByName(tag)?.let(TopicSubject::OfClub)
                    ?: playerRepository.getPlayerByName(tag)?.let(TopicSubject::OfPlayer)
                    ?: TopicSubject.Tag
            }
        }
    }

    private val header = combine(dictionaryRepository.observeTopics(), tagSubject) { topics, tagSubject ->
        if (topic != null) {
            val found = topics.find { it.slug == topic }
            (found?.name ?: topic) to if (found?.kind == TopicKind.LEAGUE) TopicSubject.League(topic) else TopicSubject.OtherTopic
        } else {
            tag.orEmpty() to tagSubject
        }
    }

    private val articles = combine(tab, page, ::Pair).flatMapLatest { (tab, page) ->
        val limit = PAGE_SIZE * page + 1
        userArticleRepository.observeArticles(kinds = tab.kinds, topic = topic, tag = tag, limit = limit)
            .map { articles -> articles.take(limit - 1) to (articles.size == limit) }
    }

    val uiState: StateFlow<TopicUiState?> = combine(header, tab, articles, userDataRepository.userData) { (title, subject), tab, (articles, hasMore), userData ->
        TopicUiState(
            title = title,
            subject = subject,
            isFollowed = when (subject) {
                is TopicSubject.League, TopicSubject.OtherTopic -> topic in userData.followedTopics
                is TopicSubject.OfClub -> subject.club.id in userData.followedClubs
                is TopicSubject.OfPlayer -> subject.player.id in userData.followedPlayers
                TopicSubject.Tag -> false
            },
            tab = tab,
            articles = articles,
            hasMore = hasMore,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectTab(tab: TopicTab) {
        savedStateHandle[TAB_KEY] = tab
        page.value = 1
    }

    fun loadMore() {
        page.value++
    }

    fun setFollowed(followed: Boolean) {
        val subject = uiState.value?.subject ?: return
        viewModelScope.launch {
            when (subject) {
                is TopicSubject.League, TopicSubject.OtherTopic -> topic?.let { userDataRepository.setTopicFollowed(it, followed) }
                is TopicSubject.OfClub -> userDataRepository.setClubFollowed(subject.club.id, followed)
                is TopicSubject.OfPlayer -> userDataRepository.setPlayerFollowed(subject.player.id, followed)
                TopicSubject.Tag -> Unit
            }
        }
    }

    fun setArticleSaved(articleId: Int, saved: Boolean) {
        viewModelScope.launch { userDataRepository.setArticleBookmarked(articleId, saved) }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("topic") topic: String?, @Assisted("tag") tag: String?): TopicViewModel
    }

    private companion object {
        const val TAB_KEY = "tab"
        const val PAGE_SIZE = 20
    }
}
