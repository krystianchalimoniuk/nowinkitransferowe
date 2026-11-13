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

package pl.nowinkitransferowe.feature.bookmarks.impl

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.data.repository.CompositeUserArticleRepository
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferRepository
import pl.nowinkitransferowe.core.testing.data.articlesTestData
import pl.nowinkitransferowe.core.testing.data.rumoursTestData
import pl.nowinkitransferowe.core.testing.data.transfersTestData
import pl.nowinkitransferowe.core.testing.repository.TestArticleRepository
import pl.nowinkitransferowe.core.testing.repository.TestDictionaryRepository
import pl.nowinkitransferowe.core.testing.repository.TestRumourRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BookmarksViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val articleRepository = TestArticleRepository()
    private val transferRepository = TestTransferRepository()
    private val rumourRepository = TestRumourRepository()

    private lateinit var viewModel: BookmarksViewModel

    @Before
    fun setup() {
        viewModel = BookmarksViewModel(
            savedStateHandle = SavedStateHandle(),
            userDataRepository = userDataRepository,
            userArticleRepository = CompositeUserArticleRepository(
                articleRepository = articleRepository,
                dictionaryRepository = TestDictionaryRepository(),
                userDataRepository = userDataRepository,
            ),
            userTransferRepository = CompositeUserTransferRepository(
                transferRepository = transferRepository,
                userDataRepository = userDataRepository,
            ),
            rumourRepository = rumourRepository,
        )
        articleRepository.sendArticles(articlesTestData)
        rumourRepository.sendRumours(rumoursTestData)
        transferRepository.sendTransfers(transfersTestData)
    }

    private fun success() = assertIs<BookmarksUiState.Success>(viewModel.uiState.value)

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(BookmarksUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun bookmarks_showInTheirTabs() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        userDataRepository.setArticleBookmarked(articlesTestData[0].id, true)
        userDataRepository.setTransferBookmarked(transfersTestData[0].id, true)
        userDataRepository.setTransferBookmarked(transfersTestData[1].id, true)

        assertEquals(SavedTab.ARTICLES, success().tab)
        assertEquals(listOf(articlesTestData[0].id), success().articles.map { it.id })
        assertEquals(2, success().transfers.size)
    }

    @Test
    fun selectTab_changesTab() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        userDataRepository.setTransferBookmarked(transfersTestData[0].id, true)
        viewModel.selectTab(SavedTab.TRANSFERS)
        assertEquals(SavedTab.TRANSFERS, success().tab)
    }

    @Test
    fun removeArticleBookmark_removesItAndUndoBringsItBack() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        userDataRepository.setArticleBookmarked(articlesTestData[0].id, true)

        viewModel.removeArticleBookmark(articlesTestData[0].id)
        assertTrue(success().articles.isEmpty())
        assertTrue(viewModel.shouldDisplayUndoBookmark)

        viewModel.undoBookmarkRemoval()
        assertEquals(1, success().articles.size)
        assertEquals(false, viewModel.shouldDisplayUndoBookmark)
    }

    @Test
    fun removeRumourBookmark_removesItAndUndoBringsItBack() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        userDataRepository.setRumourBookmarked(rumoursTestData[0].id, true)
        assertEquals(listOf(rumoursTestData[0].id), success().rumours.map { it.id })

        viewModel.removeRumourBookmark(rumoursTestData[0].id)
        assertTrue(success().rumours.isEmpty())

        viewModel.undoBookmarkRemoval()
        assertEquals(1, success().rumours.size)
    }

    @Test
    fun removeTransferBookmark_removesIt() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        userDataRepository.setTransferBookmarked(transfersTestData[0].id, true)

        viewModel.removeTransferBookmark(transfersTestData[0].id)
        assertTrue(success().transfers.isEmpty())

        viewModel.clearUndoState()
        viewModel.undoBookmarkRemoval()
        assertTrue(success().transfers.isEmpty())
    }
}
