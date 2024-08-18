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

package pl.nowinkitransferowe.core.network

import pl.nowinkitransferowe.core.network.model.NetworkChangeList
import pl.nowinkitransferowe.core.network.model.NetworkNewsResource
import pl.nowinkitransferowe.core.network.model.NetworkTransferResource

interface NtNetworkDataSource {
    suspend fun getTransferResources(ids: String? = null): List<NetworkTransferResource>

    suspend fun getNewsResources(ids: String? = null): List<NetworkNewsResource>

    suspend fun getNewsResourceChangeList(after: Int? = null): List<NetworkChangeList>

    suspend fun getTransferResourceChangeList(after: Int? = null): List<NetworkChangeList>
}
