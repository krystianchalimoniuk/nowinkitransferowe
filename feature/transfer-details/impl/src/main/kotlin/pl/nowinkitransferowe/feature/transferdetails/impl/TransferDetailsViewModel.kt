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

package pl.nowinkitransferowe.feature.transferdetails.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.PlayerRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.model.Player
import pl.nowinkitransferowe.core.model.UserTransfer

@HiltViewModel(assistedFactory = TransferDetailsViewModel.Factory::class)
class TransferDetailsViewModel @AssistedInject constructor(
    private val userDataRepository: UserDataRepository,
    userTransferRepository: UserTransferRepository,
    playerRepository: PlayerRepository,
    @Assisted val transferId: Int,
) : ViewModel() {

    // Waits while the transfer is not in the database yet, for example a link opened before the
    // first sync.
    val uiState: StateFlow<TransferDetailsUiState> = userTransferRepository.observeTransfer(transferId)
        .filterNotNull()
        .onEach { if (!it.hasBeenViewed) setViewed(it.id) }
        .flatMapLatest { transfer ->
            combine(
                playerRepository.observePlayer(transfer.player.id),
                userTransferRepository.observeTransfersOfPlayer(transfer.player.id),
            ) { player, history ->
                TransferDetailsUiState.Success(
                    transfer = transfer,
                    player = player?.takeUnless { it.isStub },
                    history = history,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransferDetailsUiState.Loading)

    fun bookmarkTransfer(transferId: Int, bookmarked: Boolean) {
        viewModelScope.launch { userDataRepository.setTransferBookmarked(transferId, bookmarked) }
    }

    private fun setViewed(transferId: Int) {
        viewModelScope.launch { userDataRepository.setTransferViewed(transferId, true) }
    }

    @AssistedFactory
    interface Factory {
        fun create(transferId: Int): TransferDetailsViewModel
    }
}

sealed interface TransferDetailsUiState {
    data object Loading : TransferDetailsUiState

    /**
     * The [transfer], its [player] once synced, and every transfer of the player newest first,
     * this one included.
     */
    data class Success(
        val transfer: UserTransfer,
        val player: Player?,
        val history: List<UserTransfer>,
    ) : TransferDetailsUiState
}
