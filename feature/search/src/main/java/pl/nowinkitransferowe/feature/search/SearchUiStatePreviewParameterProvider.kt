package pl.nowinkitransferowe.feature.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.nowinkitransferowe.core.ui.PreviewParameterData.newsResources
import pl.nowinkitransferowe.core.ui.TransfersPreviewParameterData.transfersResources

/**
 * This [PreviewParameterProvider](https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/PreviewParameterProvider)
 * provides list of [SearchResultUiState] for Composable previews.
 */
class SearchUiStatePreviewParameterProvider : PreviewParameterProvider<SearchResultUiState> {
    override val values: Sequence<SearchResultUiState> = sequenceOf(
        SearchResultUiState.Success(
            transferResources = transfersResources,
            newsResources = newsResources,
        ),
    )
}
