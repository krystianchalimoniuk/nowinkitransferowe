package pl.nowinkitransferowe.core.model

data class UserSearchResult(
    val transferResources: List<UserTransferResource> = emptyList(),
    val newsResources: List<UserNewsResource> = emptyList(),
)
