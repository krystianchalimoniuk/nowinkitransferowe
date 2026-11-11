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

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Network representation of a player from `GET /api/v3/players`, with facts from Wikidata.
 *
 * [positionGroup] is one of `GK`, `DF`, `MF` or `FW`; unknown facts are null.
 */
@Serializable
data class NetworkPlayer(
    val id: Int,
    val slug: String,
    val name: String,
    val url: String,
    val photoUrl: String? = null,
    val fullName: String? = null,
    val birthDate: LocalDate? = null,
    val birthPlace: String? = null,
    val nationality: String? = null,
    val nationality2: String? = null,
    val positionGroup: String? = null,
    val position: String? = null,
    val heightCm: Int? = null,
)
