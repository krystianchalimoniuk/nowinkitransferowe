package pl.nowinkitransferowe.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkTransferResource(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("footballer_photo")
    val footballerImg: String,
    @SerialName("club_from")
    val clubFrom: String,
    @SerialName("club_from_photo")
    val clubFromImg: String,
    @SerialName("club_to")
    val clubTo: String,
    @SerialName("club_to_photo")
    val clubToImg: String,
    @SerialName("price")
    val price: String,
    @SerialName("link")
    val link: String
)
