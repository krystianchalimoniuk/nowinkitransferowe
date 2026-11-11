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
 * One page of the API v3 change list of a record type, in ascending [NetworkChange.version] order.
 *
 * The client keeps the largest version it has seen and asks again with `after` set to it while
 * [hasMore] is true.
 */
@Serializable
data class NetworkChanges(
    val changes: List<NetworkChange>,
    val latestVersion: Int,
    val hasMore: Boolean,
)

/**
 * A change of one record: [deleted] records are removed locally, the others are fetched by id.
 */
@Serializable
data class NetworkChange(
    val id: Int,
    val version: Int,
    val deleted: Boolean,
)
