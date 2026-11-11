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
    val link: String,
    @SerialName("created_at")
    val publishDate: String,
)
