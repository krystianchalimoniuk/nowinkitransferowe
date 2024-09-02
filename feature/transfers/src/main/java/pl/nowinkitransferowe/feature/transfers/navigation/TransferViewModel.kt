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

package pl.nowinkitransferowe.feature.transfers.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.analytics.AnalyticsEvent
import pl.nowinkitransferowe.core.analytics.AnalyticsHelper
import pl.nowinkitransferowe.core.data.repository.TransferResourceQuery
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferResourceRepository
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.core.ui.TransferFeedUiState
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    syncManager: SyncManager,
    private val analyticsHelper: AnalyticsHelper,
    userTransferResourceRepository: UserTransferResourceRepository,
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    private val _page: MutableStateFlow<Int> = MutableStateFlow(1)
    val page: StateFlow<Int> = _page.asStateFlow()


    val unreadTransfers: StateFlow<List<String>> =
        userTransferResourceRepository.observeAll()
            .map { transferResources ->
                transferResources.filter { !it.hasBeenViewed }.map { it.id }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = arrayListOf(),
            )

    val selectedTransferId: StateFlow<String?> =
        savedStateHandle.getStateFlow(LINKED_TRANSFER_RESOURCE_ID, null)

    val transfersCount = userTransferResourceRepository.getCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )

    val deepLinkedTransferResource = savedStateHandle.getStateFlow<String?>(
        key = LINKED_TRANSFER_RESOURCE_ID,
        null,
    )
        .flatMapLatest { transfersResourceId ->
            if (transfersResourceId == null) {
                flowOf(emptyList())
            } else {
                userTransferResourceRepository.observeAll(
                    TransferResourceQuery(
                        filterTransferIds = setOf(transfersResourceId),
                    ),
                )
            }
        }
        .map { it.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val feedUiState: StateFlow<TransferFeedUiState> = _page.flatMapLatest {
        userTransferResourceRepository.observeAllPages(
            PAGE_SIZE * it,
            0,
        )
    }.map { updatedList ->
        TransferFeedUiState.Success(updatedList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransferFeedUiState.Loading,
    )

    val isSyncing = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun updateTransferResourceSaved(transferResourceId: String, isChecked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTransferResourceBookmarked(transferResourceId, isChecked)
        }
    }

    fun setTransferResourcesViewed(transferResourceIds: List<String>, viewed: Boolean) {
        viewModelScope.launch {
            transferResourceIds.forEach {
                userDataRepository.setTransferResourceViewed(it, viewed)
            }
        }
    }

    fun loadNextPage(page: Int, transferCount: Int) {
        if (page * PAGE_SIZE < transferCount) {
            viewModelScope.launch {
                _page.value += 1
            }
        }
    }

    fun onTransferClick(transferId: String?) {
        savedStateHandle[LINKED_TRANSFER_RESOURCE_ID] = transferId
    }

    fun onDeepLinkOpened(transferResourceId: String) {
        if (transferResourceId == deepLinkedTransferResource.value?.id) {
            savedStateHandle[LINKED_TRANSFER_RESOURCE_ID] = null
        }
        analyticsHelper.logTransferDeepLinkOpen(transferResourceId = transferResourceId)
        viewModelScope.launch {
            userDataRepository.setTransferResourceViewed(
                transferResourceId = transferResourceId,
                viewed = true,
            )
        }
    }

    companion object {
        const val PAGE_SIZE = 5
    }
}

private fun AnalyticsHelper.logTransferDeepLinkOpen(transferResourceId: String) =
    logEvent(
        AnalyticsEvent(
            type = "transfer_deep_link_opened",
            extras = listOf(
                AnalyticsEvent.Param(
                    key = LINKED_TRANSFER_RESOURCE_ID,
                    value = transferResourceId,
                ),
            ),
        ),
    )
