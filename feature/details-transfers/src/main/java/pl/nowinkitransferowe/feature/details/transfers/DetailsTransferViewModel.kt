package pl.nowinkitransferowe.feature.details.transfers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.analytics.AnalyticsEvent
import pl.nowinkitransferowe.core.analytics.AnalyticsHelper
import pl.nowinkitransferowe.core.common.result.Result
import pl.nowinkitransferowe.core.common.result.asResult
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.model.mapToUserTransferResources
import pl.nowinkitransferowe.feature.details.transfers.navigation.LINKED_TRANSFER_RESOURCE_ID
import javax.inject.Inject

@HiltViewModel
class DetailsTransferViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    transferRepository: TransferRepository,
) : ViewModel() {

    val transferId: String = savedStateHandle[LINKED_TRANSFER_RESOURCE_ID] ?: ""
    val detailsTransferUiState = savedStateHandle.getStateFlow<String?>(
        key = LINKED_TRANSFER_RESOURCE_ID,
        null,
    )
        .flatMapLatest { transferResourceId ->
            if (transferResourceId == null) {
                flowOf()
            } else {
                transferRepository.getTransferResource(transferResourceId)
            }
        }.flatMapLatest { transferRepository.getTransferResourceByName(it.name) }
        .combine(userDataRepository.userData) { transferResources, userData ->
            transferResources.mapToUserTransferResources(userData)
        }.onEach {
            it.onEach { item ->
                if (!item.hasBeenViewed) {
                    setTransferResourceViewed(item.id, true)
                }
            }
        }.asResult()
        .map { result ->
            when (result) {
                is Result.Success -> {
                    DetailsTransferUiState.Success(result.data)
                }

                is Result.Loading -> {
                    DetailsTransferUiState.Loading
                }

                is Result.Error -> DetailsTransferUiState.Error
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DetailsTransferUiState.Loading,
        )


    fun bookmarkTransfer(transferResourceId: String, bookmarked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTransferResourceBookmarked(transferResourceId, bookmarked)
        }
    }

    private fun setTransferResourceViewed(transferResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTransferResourceViewed(transferResourceId, viewed)
        }
    }
}

private fun AnalyticsHelper.logTransferDeepLinkOpen(newsResourceId: String) =
    logEvent(
        AnalyticsEvent(
            type = "transfer_deep_link_opened",
            extras = listOf(
                AnalyticsEvent.Param(
                    key = LINKED_TRANSFER_RESOURCE_ID,
                    value = newsResourceId,
                ),
            ),
        ),
    )

sealed interface DetailsTransferUiState {
    data class Success(val userTransferResource: List<UserTransferResource>) :
        DetailsTransferUiState

    data object Error : DetailsTransferUiState
    data object Loading : DetailsTransferUiState
}
