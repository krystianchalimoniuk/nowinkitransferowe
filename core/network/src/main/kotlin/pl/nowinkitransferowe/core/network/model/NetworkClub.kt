/*
 * Copyright 2026 The Android Open Source Project
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

import kotlinx.serialization.Serializable

/**
 * Network representation of a club from `GET /api/v3/clubs`, with facts from Wikidata.
 *
 * [country] is a lowercase ISO 3166-1 alpha-2 code or `gb-eng`, `gb-sct`, `gb-wls`, `gb-nir`;
 * [topic] is the slug of the league the club plays in, when it is one of the topics.
 */
@Serializable
data class NetworkClub(
    val id: Int,
    val slug: String,
    val name: String,
    val url: String,
    val crestUrl: String? = null,
    val fullName: String? = null,
    val country: String? = null,
    val city: String? = null,
    val founded: Int? = null,
    val stadium: String? = null,
    val stadiumCapacity: Int? = null,
    val website: String? = null,
    val topic: String? = null,
)
