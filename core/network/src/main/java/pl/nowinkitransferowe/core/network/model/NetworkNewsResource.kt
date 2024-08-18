/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
