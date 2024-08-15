package pl.nowinkitransferowe.feature.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.nowinkitransferowe.core.data.repository.UserDataRepository
import pl.nowinkitransferowe.core.data.repository.UserNewsResourceRepository
import pl.nowinkitransferowe.core.data.util.DatabaseUpdatingMonitor
import pl.nowinkitransferowe.core.data.util.SyncManager
import pl.nowinkitransferowe.core.model.UserNewsResource
import pl.nowinkitransferowe.core.ui.NewsFeedUiState
import pl.nowinkitransferowe.feature.news.navigation.LINKED_NEWS_RESOURCE_ID
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    syncManager: SyncManager,
    private val userDataRepository: UserDataRepository,
    private val userNewsResourceRepository: UserNewsResourceRepository,
) : ViewModel() {


    val selectedNewsId: StateFlow<String?> =
        savedStateHandle.getStateFlow(LINKED_NEWS_RESOURCE_ID, null)

    private val _page: MutableStateFlow<Int> = MutableStateFlow(1)
    val page: StateFlow<Int> = _page.asStateFlow()
    val newsCount = userNewsResourceRepository.getCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )
    val isSyncing = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val feedState: StateFlow<NewsFeedUiState> =
        _page.flatMapLatest {
            userNewsResourceRepository.observeAllPages(INITIAL_PAGE_SIZE * it, 0)
        }.distinctUntilChanged()
            .map { updatedList ->
                NewsFeedUiState.Success(updatedList)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsFeedUiState.Loading,
            )

    fun updateNewsResourceSaved(newsResourceId: String, isChecked: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceBookmarked(newsResourceId, isChecked)
        }
    }

    fun setNewsResourceViewed(newsResourceId: String, viewed: Boolean) {
        viewModelScope.launch {
            userDataRepository.setNewsResourceViewed(newsResourceId, viewed)
        }
    }

    fun onNewsClick(newsId: String?) {
        savedStateHandle[LINKED_NEWS_RESOURCE_ID] = newsId
    }

    fun loadNextPage(page: Int, newsCount: Int) {
        if (page * INITIAL_PAGE_SIZE < newsCount) {
            viewModelScope.launch {
                _page.value += 1
            }
        }
    }

    companion object {
        const val INITIAL_PAGE_SIZE = 5
    }
}



