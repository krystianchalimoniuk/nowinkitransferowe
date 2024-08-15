package pl.nowinkitransferowe.core.model

/** An entity that holds the search result */
data class SearchResult(
    val transferResources: List<TransferResource> = emptyList(),
    val newsResources: List<NewsResource> = emptyList(),
)
