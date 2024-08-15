package pl.nowinkitransferowe.feature.details

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
import pl.nowinkitransferowe.core.data.repository.NewsRepository
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.model.DarkThemeConfig
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.feature.details.navigation.LINKED_NEWS_RESOURCE_ID
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    newsRepository: NewsRepository,
) : ViewModel() {

    val newsId: String = savedStateHandle[LINKED_NEWS_RESOURCE_ID] ?: ""
    val detailsUiState = savedStateHandle.getStateFlow<String?>(
        key = LINKED_NEWS_RESOURCE_ID,
        null,
    )
        .flatMapLatest { newsResourceId ->
            if (newsResourceId == null) {
                flowOf()
            } else {
                newsRepository.getNewsResource(newsResourceId)
            }
        }.combine(userDataRepository.userData) { newsResource, userData ->
            UserNewsResource(newsResource, userData)
        }.onEach {
            if (!it.hasBeenViewed) {
                setNewsResourceViewed(it.id, true)
            }
        }.asResult()
        .map { result ->
            when (result) {
                is Result.Success -> {
                    DetailsUiState.Success(result.data)
                }

                is Result.Loading -> {
                    DetailsUiState.Loading
                }

                is Result.Error -> DetailsUiState.Error
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUiState.Loading)

    val darkThemeConfig = userDataRepository.userData.map { userData ->
        userData.darkThemeConfig
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DarkThemeConfig.FOLLOW_SYSTEM)


    fun bookmarkNews(newsResourceId: String, bookmarked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceBookmarked(newsResourceId, bookmarked)
        }
    }

    private fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }


}

private fun AnalyticsHelper.logNewsDeepLinkOpen(newsResourceId: String) =
    logEvent(
        AnalyticsEvent(
            type = "news_deep_link_opened",
            extras = listOf(
                AnalyticsEvent.Param(
                    key = LINKED_NEWS_RESOURCE_ID,
                    value = newsResourceId,
                ),
            ),
        ),
    )

sealed interface DetailsUiState {
    data class Success(val userNewsResource: UserNewsResource) : DetailsUiState
    data object Error : DetailsUiState
    data object Loading : DetailsUiState
}