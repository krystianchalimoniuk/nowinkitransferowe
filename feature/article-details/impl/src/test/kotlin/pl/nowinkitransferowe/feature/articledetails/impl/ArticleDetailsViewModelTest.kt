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

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.nowinkitransferowe.core.data.repository.CompositeUserArticleRepository
import pl.nowinkitransferowe.core.model.mapToUserArticles
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.clubsTestData
import pl.nowinkitransferowe.core.testing.data.playersTestData
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.repository.TestArticleRepository
import pl.nowinkitransferowe.core.testing.repository.TestClubRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestPlayerRepository
import pl.nowinkitransferowe.core.testing.repository.TestRumourRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
class ArticleDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val articleRepository = TestArticleRepository()

    private val userArticleRepository = CompositeUserArticleRepository(
        articleRepository = articleRepository,
        dictionaryRepository = TestDictionaryRepository(),
        userDataRepository = userDataRepository,
    )

    private val clubRepository = TestClubRepository()
    private val playerRepository = TestPlayerRepository()
    private val rumourRepository = TestRumourRepository()

    private lateinit var viewModel: ArticleDetailsViewModel

    @Before
    fun setup() {
        userDataRepository.setUserData(emptyUserData)
        viewModel = ArticleDetailsViewModel(
            userDataRepository = userDataRepository,
            userArticleRepository = userArticleRepository,
            clubRepository = clubRepository,
            playerRepository = playerRepository,
            rumourRepository = rumourRepository,
            articleId = articlesTestData.first().id,
        )
        rumourRepository.sendRumours(emptyList())
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(ArticleDetailsUiState.Loading, viewModel.detailsUiState.value)
    }

    @Test
    fun stateStaysLoadingUntilTheArticleIsSynced() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.detailsUiState.collect() }
        articleRepository.sendArticles(emptyList())

        assertEquals(ArticleDetailsUiState.Loading, viewModel.detailsUiState.value)
    }

    @Test
    fun whenEntityExist_uiStateIsSuccessAndTheArticleIsMarkedAsViewed() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.detailsUiState.collect() }
        articleRepository.sendArticles(articlesTestData)

        val article = articlesTestData.first()
        val expected = ArticleDetailsUiState.Success(
            userArticle = listOf(article).mapToUserArticles(
                topicsTestData,
                emptyUserData.copy(viewedArticles = setOf(article.id)),
            ).single(),
        )
        assertEquals(expected = expected, actual = viewModel.detailsUiState.value)
    }

    @Test
    fun whenUpdateArticleSavedIsCalled_bookmarkStateIsUpdated() = runTest {
        viewModel.bookmarkArticle(123, true)

        assertEquals(setOf(123), userDataRepository.userData.first().bookmarkedArticles)

        viewModel.bookmarkArticle(123, false)

        assertEquals(emptySet(), userDataRepository.userData.first().bookmarkedArticles)
    }

    @Test
    fun clubsPlayersAndTheRumourOfTheArticleAreShown() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.detailsUiState.collect() }
        val article = articlesTestData.first().copy(clubIds = listOf(86, 83, 999), playerIds = listOf(10281))
        articleRepository.sendArticles(listOf(article))
        clubRepository.sendClubs(clubsTestData)
        playerRepository.sendPlayers(playersTestData)
        rumourRepository.sendRumours(rumoursTestData.map { if (it.id == 4) it.copy(latestArticleId = article.id) else it })

        val state = assertIs<ArticleDetailsUiState.Success>(viewModel.detailsUiState.value)
        // In the article's order; the unknown club 999 is left out.
        assertEquals(listOf(86, 83), state.clubs.map { it.id })
        assertEquals(listOf(10281), state.players.map { it.id })
        assertEquals(4, state.rumour?.id)
    }
}
