package pl.nowinkitransferowe.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import pl.nowinkitransferowe.core.model.NewsCategory
import pl.nowinkitransferowe.core.model.NewsResource

/**
 * Defines an NT news resource.
 */
@Entity(
    tableName = "news_resources",
)
data class NewsResourceEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: NewsCategory,
    val isImportant: Boolean,
    val time: String,
    val author: String,
    @ColumnInfo(name = "photo_src")
    val photoSrc: String,
    val src: String,
    @ColumnInfo(name = "publish_date")
    val publishDate: Instant,
    @ColumnInfo(name = "auth_twitter")
    val authTwitter: String,
    @ColumnInfo(name = "auth_picture")
    val authPic: String,
    val link: String,
    val topics: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
)

fun NewsResourceEntity.asExternalModel() = NewsResource(
    id = id,
    title = title,
    description = description,
    category = category,
    isImportant = isImportant,
    author = author,
    photoSrc = photoSrc,
    src = src,
    publishDate = publishDate,
    authPic = authPic,
    authTwitter = authTwitter,
    link = link,
    topics = topics.split(", "),
    imageUrl = imageUrl,
)
