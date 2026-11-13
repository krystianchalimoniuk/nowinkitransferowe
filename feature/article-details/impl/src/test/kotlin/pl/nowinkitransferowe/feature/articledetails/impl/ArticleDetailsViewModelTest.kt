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
import pl.nowinkitransferowe.core.testing.data.topicsTestData
import pl.nowinkitransferowe.core.testing.repository.TestArticleRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals

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

    private lateinit var viewModel: ArticleDetailsViewModel

    @Before
    fun setup() {
        userDataRepository.setUserData(emptyUserData)
        viewModel = ArticleDetailsViewModel(
            userDataRepository = userDataRepository,
            userArticleRepository = userArticleRepository,
            articleId = articlesTestData.first().id,
        )
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
}
