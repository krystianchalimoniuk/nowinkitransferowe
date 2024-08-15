package pl.nowinkitransferowe.core.model

import kotlinx.datetime.Instant

data class NewsResource(
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
    val link: String,
    val publishDate: Instant,
    val topics: List<String>,
    val imageUrl: String,

    )