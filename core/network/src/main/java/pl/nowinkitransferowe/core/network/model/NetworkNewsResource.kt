package pl.nowinkitransferowe.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.network.model.utill.NewsCategoryTypeSerializer

@Serializable
data class NetworkNewsResource(
    @SerialName("id")
    val id: Int,
    @SerialName("tytul")
    val title: String,
    @SerialName("description")
    val description: String,
    @Serializable(NewsCategoryTypeSerializer::class)
    @SerialName("category")
    val category: NewsCategory,
    @SerialName("gorace")
    val isImportant: String,
    @SerialName("time")
    val time: String,
    @SerialName("autor")
    val author: String,
    @SerialName("photo_source")
    val photoSrc: String,
    @SerialName("news_source")
    val newsSrc: String?,
    @SerialName("data")
    val date: String,
    @SerialName("twitter")
    val authorTwitter: String,
    @SerialName("author_picture")
    val authorPicture: String,
    @SerialName("link")
    val link: String,
    @SerialName("tags")
    val topics: String,
    @SerialName("picture")
    val picture: String,
)
