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

package pl.nowinkitransferowe.feature.details.transfers.impl

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.common.result.Result
import pl.nowinkitransferowe.core.common.result.asResult
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserTransferRepository
import pl.nowinkitransferowe.core.data.util.ImageDownloader
import pl.nowinkitransferowe.core.model.UserTransfer
import pl.nowinkitransferowe.core.model.crestUrl
import pl.nowinkitransferowe.feature.details.transfers.impl.Util.dateFormatted
import pl.nowinkitransferowe.feature.details.transfers.impl.Util.priceToFloat
import pl.nowinkitransferowe.feature.details.transfers.impl.Util.shortcutDate

@HiltViewModel(assistedFactory = DetailsTransferViewModel.Factory::class)
class DetailsTransferViewModel @AssistedInject constructor(
    private val userDataRepository: UserDataRepository,
    userTransferRepository: UserTransferRepository,
    private val imageDownloader: ImageDownloader,
    @Assisted val transferResourceId: Int,
) : ViewModel() {

    val detailsTransferUiState =
        // Every transfer of the player, newest first; waits while the transfer is not in the
        // database yet.
        userTransferRepository.observeTransfer(transferResourceId)
            .filterNotNull()
            .map { it.player.id }
            .distinctUntilChanged()
            .flatMapLatest(userTransferRepository::observeTransfersOfPlayer)
            .map { transfers -> Pair(transfers, getChartDataPoint(transfers)) }
            .onEach {
                it.first.onEach { item ->
                    if (!item.hasBeenViewed) {
                        setTransferResourceViewed(item.id, true)
                    }
                }
            }.asResult()
            .map { result ->
                when (result) {
                    is Result.Success -> {
                        DetailsTransferUiState.Success(result.data.first, result.data.second)
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

    private fun setTransferResourceViewed(transferId: Int, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTransferViewed(transferId, viewed)
        }
    }

    private suspend fun getChartDataPoint(userTransferResource: List<UserTransfer>): List<DataPoint> {
        val dataPoints = mutableListOf<DataPoint>()
        userTransferResource.sortedWith(compareBy<UserTransfer> { it.date }.thenBy { it.id }).forEach {
            val date = shortcutDate(dateFormatted(it.date))
            val price = priceToFloat(it.fee.label)
            val bitmap = it.to.crestUrl?.let { url -> imageDownloader.loadImage(url) }
            val scaledBitmap =
                bitmap?.let { it1 -> Bitmap.createScaledBitmap(it1, 80, 80, false) }
            dataPoints.add(
                DataPoint(
                    date = date,
                    price = price,
                    scaledBitmap,
                ),
            )
        }
        return dataPoints
    }

    @AssistedFactory
    interface Factory {
        fun create(transferResourceId: Int): DetailsTransferViewModel
    }
}

sealed interface DetailsTransferUiState {
    data class Success(
        val userTransferResource: List<UserTransfer>,
        val dataPoints: List<DataPoint>,
    ) : DetailsTransferUiState

    data object Error : DetailsTransferUiState
    data object Loading : DetailsTransferUiState
}

data class DataPoint(val date: String, val price: Float, val bitmap: Bitmap?)
