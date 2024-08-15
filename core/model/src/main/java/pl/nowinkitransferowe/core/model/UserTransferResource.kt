package pl.nowinkitransferowe.core.model


data class UserTransferResource(
    val id: String,
    val name: String,
    val footballerImg: String,
    val clubFrom: String,
    val clubFromImg: String,
    val clubTo: String,
    val clubToImg: String,
    val price: String,
    val url: String,
    val isSaved: Boolean,
    val hasBeenViewed: Boolean,
) {


    constructor(transferResource: TransferResource, userData: UserData) : this(
        id = transferResource.id,
        name = transferResource.name,
        footballerImg = transferResource.footballerImg,
        clubFrom = transferResource.clubFrom,
        clubFromImg = transferResource.clubFromImg,
        clubTo = transferResource.clubTo,
        clubToImg = transferResource.clubToImg,
        price = transferResource.price,
        url = transferResource.url,
        isSaved = transferResource.id in userData.bookmarkedTransferResources,
        hasBeenViewed = transferResource.id in userData.viewedTransferResources
    )
}


fun List<TransferResource>.mapToUserTransferResources(userData: UserData): List<UserTransferResource> =
    map { UserTransferResource(it, userData) }