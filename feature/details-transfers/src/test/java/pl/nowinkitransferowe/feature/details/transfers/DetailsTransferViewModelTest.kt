package pl.nowinkitransferowe.feature.details.transfers

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.testing.data.transferResourceTestData
import pl.nowinkitransferowe.core.testing.repository.TestTransferRepository
import pl.nowinkitransferowe.core.testing.repository.TestUserDataRepository
import pl.nowinkitransferowe.core.testing.repository.emptyUserData
import pl.nowinkitransferowe.core.testing.util.MainDispatcherRule
import pl.nowinkitransferowe.core.testing.util.TestImageDownloader
import pl.nowinkitransferowe.feature.details.transfers.navigation.LINKED_TRANSFER_RESOURCE_ID
import kotlin.test.assertEquals

class DetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userDataRepository = TestUserDataRepository()
    private val transferRepository = TestTransferRepository()
    private val imageDownloader = TestImageDownloader()

    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: DetailsTransferViewModel

    @Before
    fun setup() {
        viewModel = DetailsTransferViewModel(
            savedStateHandle = savedStateHandle,
            userDataRepository = userDataRepository,
            transferRepository = transferRepository,
            imageDownloader = imageDownloader,
            )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(DetailsTransferUiState.Loading, viewModel.detailsTransferUiState.value)
    }

    @Test
    fun whenEntityIsNotExist_uiStateIsError() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.detailsTransferUiState.collect() }
        savedStateHandle[LINKED_TRANSFER_RESOURCE_ID] = "11"
        transferRepository.sendTransferResources(transferResourceTestData)
        assertEquals(
            expected = DetailsTransferUiState.Error,
            actual = viewModel.detailsTransferUiState.value,
        )
        collectJob.cancel()
    }

    @Test
    fun whenEntityExist_uiStateIsSuccess() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.detailsTransferUiState.collect() }
        savedStateHandle[LINKED_TRANSFER_RESOURCE_ID] = transferResourceTestData.first().id
        transferRepository.sendTransferResources(transferResourceTestData)
        userDataRepository.setDynamicColorPreference(false)
        val expected = DetailsTransferUiState.Success(
            userTransferResource = transferResourceTestData.map {
                UserTransferResource(
                    it,
                    userData = emptyUserData,
                ).copy(hasBeenViewed = true)
            }.take(1),
            dataPoints = arrayListOf(DataPoint(date = "paź 22", price = 0.0f, bitmap = null)),
        )
        assertEquals(expected = expected, actual = viewModel.detailsTransferUiState.value)
        collectJob.cancel()
    }

}
