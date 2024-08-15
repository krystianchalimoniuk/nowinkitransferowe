package pl.nowinkitransferowe.core.model

import kotlinx.datetime.Instant

data class UserNewsResource internal constructor(
    val id: String,
    val title: String,
    val description: String,
    val category: NewsCategory,
    val isImportant: Boolean,
    val author: String,
    val photoSrc: String,
    val src: String,
    val authPic: String,
    val authTwitter: String,
    val publishDate: Instant,
    val link: String,
    val topics: List<String>,
    val imageUrl: String,
    val isSaved: Boolean,
    val hasBeenViewed: Boolean,
) {

    constructor(newsResource: NewsResource, userData: UserData) : this(
        id = newsResource.id,
        title = newsResource.title,
        description = newsResource.description,
        category = newsResource.category,
        isImportant = newsResource.isImportant,
        author = newsResource.author,
        photoSrc = newsResource.photoSrc,
        src = newsResource.src,
        authPic = newsResource.authPic,
        authTwitter = newsResource.authTwitter,
        publishDate = newsResource.publishDate,
        link = newsResource.link,
        topics = newsResource.topics,
        imageUrl = newsResource.imageUrl,
        isSaved = newsResource.id in userData.bookmarkedNewsResources,
        hasBeenViewed = newsResource.id in userData.viewedNewsResources
    )
}

fun List<NewsResource>.mapToUserNewsResources(userData: UserData): List<UserNewsResource> =
    map { UserNewsResource(it, userData) }