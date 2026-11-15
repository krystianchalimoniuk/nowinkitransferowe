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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserArticleRepository
import pl.nowinkitransferowe.core.model.ArticleKind
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.repository.TestArticleRepository
import pl.nowinkitransferowe.core.testing.repository.TestClubRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestPlayerRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TopicViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val articleRepository = TestArticleRepository()
    private val dictionaryRepository = TestDictionaryRepository()
    private val clubRepository = TestClubRepository()

    private fun viewModel(topic: String? = null, tag: String? = null) = TopicViewModel(
        SavedStateHandle(),
        userDataRepository,
        CompositeUserArticleRepository(articleRepository, dictionaryRepository, userDataRepository),
        dictionaryRepository,
        clubRepository,
        TestPlayerRepository(),
        topic,
        tag,
    )

    @Before
    fun setUp() {
        userDataRepository.setUserData(emptyUserData)
        dictionaryRepository.sendTopics(topicsTestData)
        clubRepository.sendClubs(clubsTestData)
        articleRepository.sendArticles(
            articlesTestData.mapIndexed { i, article ->
                article.copy(topics = if (i < 2) listOf("serie-a") else listOf("ekstraklasa"), tags = if (i == 0) listOf("West Ham United") else emptyList())
            },
        )
    }

    @Test
    fun league_hasItsArticles_tabs_andCanBeFollowed() = runTest {
        val viewModel = viewModel(topic = "serie-a")
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = assertNotNull(viewModel.uiState.value)
        assertEquals("Serie A", state.title)
        assertEquals(TopicSubject.League("serie-a"), state.subject)
        assertEquals(articlesTestData.take(2).map { it.id }.toSet(), state.articles.orEmpty().map { it.id }.toSet())

        viewModel.selectTab(TopicTab.OFFICIAL)
        assertTrue(assertNotNull(viewModel.uiState.value).articles.orEmpty().all { it.kind == ArticleKind.OFFICIAL })

        viewModel.setFollowed(true)
        assertTrue(assertNotNull(viewModel.uiState.value).isFollowed)
        assertEquals(setOf("serie-a"), userDataRepository.userData.first().followedTopics)
    }

    @Test
    fun tagOfAClub_linksAndFollowsTheClub() = runTest {
        val viewModel = viewModel(tag = "West Ham United")
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = assertNotNull(viewModel.uiState.value)
        assertEquals(83, assertIs<TopicSubject.OfClub>(state.subject).club.id)
        assertEquals(listOf(articlesTestData[0].id), state.articles.orEmpty().map { it.id })

        viewModel.setFollowed(true)
        assertEquals(setOf(83), userDataRepository.userData.first().followedClubs)
    }

    @Test
    fun plainTag_cannotBeFollowed() = runTest {
        val viewModel = viewModel(tag = "Transfery")
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = assertNotNull(viewModel.uiState.value)
        assertEquals(TopicSubject.Tag, state.subject)
        assertFalse(state.isFollowable)
        assertTrue(state.articles.orEmpty().isEmpty())
    }
}
