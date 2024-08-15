package pl.nowinkitransferowe.feature.bookmarks

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import pl.nowinkitransferowe.core.data.repository.CompositeUserNewsResourceRepository
import pl.nowinkitransferowe.core.data.repository.CompositeUserTransferResourceRepository
import pl.nowinkitransferowe.core.testing.data.newsResourcesTestData
import pl.nowinkitransferowe.core.testing.data.transferResourceTestData
import pl.nowinkitransferowe.core.testing.repository.TestNewsRepository
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.core.ui.NewsFeedUiState
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import kotlin.test.assertIs
import kotlinx.coroutines.flow.collect

class BookmarksViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val newsRepository = TestNewsRepository()
    private val userNewsResourceRepository = CompositeUserNewsResourceRepository(
        newsRepository = newsRepository,
        userDataRepository = userDataRepository,
    )

    private val transferRepository = TestTransferRepository()
    private val userTransferResourceRepository = CompositeUserTransferResourceRepository(
        transferRepository = transferRepository,
        userDataRepository = userDataRepository
    )

    private lateinit var viewModel: BookmarksViewModel

    @Before
    fun setup() {
        viewModel = BookmarksViewModel(
            userDataRepository = userDataRepository,
            userNewsResourceRepository = userNewsResourceRepository,
            userTransferResourceRepository = userTransferResourceRepository
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(NewsFeedUiState.Loading, viewModel.newsFeedUiState.value)
        assertEquals(TransferFeedUiState.Loading, viewModel.transferFeedUiState.value)
    }

    @Test
    fun oneNews_showsInFeed() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.newsFeedUiState.collect() }

        newsRepository.sendNewsResources(newsResourcesTestData)
        userDataRepository.setNewsResourceBookmarked(newsResourcesTestData[0].id, true)
        val item = viewModel.newsFeedUiState.value
        assertIs<NewsFeedUiState.Success>(item)
        kotlin.test.assertEquals(item.feed.size, 1)

        collectJob.cancel()
    }

    @Test
    fun oneNews_whenRemoving_removesFromFeed() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.newsFeedUiState.collect() }
        // Set the news resources to be used by this test
        newsRepository.sendNewsResources(newsResourcesTestData)
        // Start with the resource saved
        userDataRepository.setNewsResourceBookmarked(newsResourcesTestData[0].id, true)
        // Use viewModel to remove saved resource
        viewModel.removeFromSavedNewsResources(newsResourcesTestData[0].id)
        // Verify list of saved resources is now empty
        val item = viewModel.newsFeedUiState.value
        assertIs<NewsFeedUiState.Success>(item)
        kotlin.test.assertEquals(item.feed.size, 0)

        collectJob.cancel()
    }

    @Test
    fun oneTransfer_showsInFeed() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.transferFeedUiState.collect() }

        transferRepository.sendTransferResources(transferResourceTestData)
        userDataRepository.setTransferResourceBookmarked(transferResourceTestData[0].id, true)
        val item = viewModel.transferFeedUiState.value
        assertIs<TransferFeedUiState.Success>(item)
        kotlin.test.assertEquals(item.feed.size, 1)

        collectJob.cancel()
    }

    @Test
    fun oneTransfer_whenRemoving_removesFromFeed() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.transferFeedUiState.collect() }
        // Set the news resources to be used by this test
        transferRepository.sendTransferResources(transferResourceTestData)
        // Start with the resource saved
        userDataRepository.setTransferResourceBookmarked(transferResourceTestData[0].id, true)
        // Use viewModel to remove saved resource
        viewModel.removeFromSavedTransferResources(transferResourceTestData[0].id)
        // Verify list of saved resources is now empty
        val item = viewModel.transferFeedUiState.value
        assertIs<TransferFeedUiState.Success>(item)
        kotlin.test.assertEquals(item.feed.size, 0)

        collectJob.cancel()
    }
}