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

package pl.nowinkitransferowe.core.model

import kotlinx.datetime.Instant

@ConsistentCopyVisibility
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
    val publishDate: Instant,
    val season: String,
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
        season = transferResource.season,
        publishDate = transferResource.publishDate,
        isSaved = transferResource.id in userData.bookmarkedTransferResources,
        hasBeenViewed = transferResource.id in userData.viewedTransferResources,
    )
}

fun List<TransferResource>.mapToUserTransferResources(userData: UserData): List<UserTransferResource> =
    map { UserTransferResource(it, userData) }
