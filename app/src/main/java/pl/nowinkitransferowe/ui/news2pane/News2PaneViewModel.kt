package pl.nowinkitransferowe.ui.news2pane

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import pl.nowinkitransferowe.feature.details.navigation.LINKED_NEWS_RESOURCE_ID
import javax.inject.Inject


@HiltViewModel
class News2PaneViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val selectedNewsId: StateFlow<String?> =
        savedStateHandle.getStateFlow(LINKED_NEWS_RESOURCE_ID, savedStateHandle[LINKED_NEWS_RESOURCE_ID])

    fun onNewsClick(newsId: String?) {
        savedStateHandle[LINKED_NEWS_RESOURCE_ID] = newsId
    }
}
