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

package pl.nowinkitransferowe.feature.details.transfers

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.common.result.Result
import pl.nowinkitransferowe.core.common.result.asResult
import pl.nowinkitransferowe.core.data.repository.TransferRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.util.ImageDownloader
import pl.nowinkitransferowe.core.model.UserTransferResource
import pl.nowinkitransferowe.core.model.mapToUserTransferResources
import pl.nowinkitransferowe.core.network.BuildConfig
import pl.nowinkitransferowe.feature.details.transfers.Util.dateFormatted
import pl.nowinkitransferowe.feature.details.transfers.Util.priceToFloat
import pl.nowinkitransferowe.feature.details.transfers.Util.shortcutDate
import pl.nowinkitransferowe.feature.details.transfers.navigation.DetailTransferRoute
import javax.inject.Inject

@HiltViewModel
class DetailsTransferViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    transferRepository: TransferRepository,
    private val imageDownloader: ImageDownloader,
) : ViewModel() {

    val transferResourceId: String = savedStateHandle.toRoute<DetailTransferRoute>().transferId
    val detailsTransferUiState =
        transferRepository.getTransferResource(transferResourceId)
            .flatMapLatest { transferRepository.getTransferResourceByName(it.name) }
            .combine(userDataRepository.userData) { transferResources, userData ->
                val userTransferResource = transferResources.mapToUserTransferResources(userData)
                Pair(userTransferResource, getChartDataPoint(userTransferResource))
            }.onEach {
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

    private fun setTransferResourceViewed(transferResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setTransferResourceViewed(transferResourceId, viewed)
        }
    }

    private suspend fun getChartDataPoint(userTransferResource: List<UserTransferResource>): List<DataPoint> {
        val dataPoints = mutableListOf<DataPoint>()
        userTransferResource.sortedBy { it.id.toInt() }.forEach {
            val date = shortcutDate(dateFormatted(it.publishDate))
            val price = priceToFloat(it.price)
            val bitmap = imageDownloader.loadImage("${BuildConfig.IMAGES_URL}${it.clubToImg}")
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
}

sealed interface DetailsTransferUiState {
    data class Success(
        val userTransferResource: List<UserTransferResource>,
        val dataPoints: List<DataPoint>,
    ) :
        DetailsTransferUiState

    data object Error : DetailsTransferUiState
    data object Loading : DetailsTransferUiState
}

data class DataPoint(val date: String, val price: Float, val bitmap: Bitmap?)
